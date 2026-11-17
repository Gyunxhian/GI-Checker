package com.example.diabetesassistant.domain.usecase

import com.example.diabetesassistant.data.local.dao.CookingMethodDao
import com.example.diabetesassistant.data.model.DishRecognitionResult
import com.example.diabetesassistant.data.model.InsulinType
import com.example.diabetesassistant.data.remote.NetworkResult
import com.example.diabetesassistant.data.repository.AiRepository
import com.example.diabetesassistant.data.repository.NutritionRepository
import com.example.diabetesassistant.domain.algorithm.AlgorithmConstants
import com.example.diabetesassistant.domain.algorithm.BloodSugarPrediction
import com.example.diabetesassistant.domain.algorithm.BloodSugarPredictor
import com.example.diabetesassistant.domain.algorithm.CarbAndGiCalculator
import com.example.diabetesassistant.domain.algorithm.FocusPeriod
import com.example.diabetesassistant.domain.algorithm.FocusPeriodAnalyzer
import com.example.diabetesassistant.domain.algorithm.InsulinBandCalculator
import com.example.diabetesassistant.domain.algorithm.InsulinBandResult
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

private const val TAG = "Diabetes|AnalyzeFood"

/**
 * 餐食分析结果。
 */
data class AnalyzeFoodResult(
    val totalCarbIntake: Float,
    val weightedGi: Float,
    val bloodSugarPrediction: BloodSugarPrediction,
    val insulinBand: InsulinBandResult,
    val focusPeriods: List<FocusPeriod>,
    val reminders: List<String>,
    val recognitionResult: DishRecognitionResult? = null,
    val ingredientCarbsMap: Map<String, Float> = emptyMap(),
)

/**
 * Step 0~7 全流程分析用例。
 */
class AnalyzeFoodUseCase @Inject constructor(
    private val nutritionRepository: NutritionRepository,
    private val aiRepository: AiRepository,
    private val cookingMethodDao: CookingMethodDao,
    private val carbAndGiCalculator: CarbAndGiCalculator,
    private val bloodSugarPredictor: BloodSugarPredictor,
    private val insulinBandCalculator: InsulinBandCalculator,
    private val focusPeriodAnalyzer: FocusPeriodAnalyzer,
) {
    private val gson = Gson()

    /**
     * 执行完整餐食分析流程（单图重载）。
     */
    suspend operator fun invoke(
        imageUrl: String,
        meatRatio: Float,
        intakePercent: Float,
        insulinType: InsulinType,
        bloodSugarStart: Float = AlgorithmConstants.DEFAULT_BLOOD_SUGAR_START,
    ): NetworkResult<AnalyzeFoodResult> = invoke(
        imageUrls = listOf(imageUrl),
        meatRatio = meatRatio,
        intakePercent = intakePercent,
        insulinType = insulinType,
        bloodSugarStart = bloodSugarStart,
    )

    /**
     * 执行完整餐食分析流程（多图整餐整合）。
     */
    suspend operator fun invoke(
        imageUrls: List<String>,
        meatRatio: Float,
        intakePercent: Float,
        insulinType: InsulinType,
        bloodSugarStart: Float = AlgorithmConstants.DEFAULT_BLOOD_SUGAR_START,
    ): NetworkResult<AnalyzeFoodResult> {
        val prompt = FOOD_ANALYSIS_PROMPT

        Log.d(TAG, "===== Step 0: AI图像识别 (${imageUrls.size} 张图片) =====")
        return when (val recognized = aiRepository.recognizeDishesJson(imageUrls, prompt)) {
            is NetworkResult.Success -> {
                val parsed = try {
                    gson.fromJson(recognized.data, DishRecognitionResult::class.java)
                } catch (_: Exception) {
                    Log.e(TAG, "JSON解析失败: ${recognized.data}")
                    null
                }
                if (parsed == null) {
                    NetworkResult.Error(message = "Failed to parse dish recognition JSON.")
                } else {
                    analyzeParsedResult(
                        recognitionResult = parsed,
                        meatRatio = meatRatio,
                        intakePercent = intakePercent,
                        insulinType = insulinType,
                        bloodSugarStart = bloodSugarStart,
                    )
                }
            }
            is NetworkResult.Error -> recognized
        }
    }

    suspend fun recalculate(
        recognitionResult: DishRecognitionResult,
        meatRatio: Float,
        intakePercent: Float,
        insulinType: InsulinType,
        bloodSugarStart: Float,
    ): NetworkResult<AnalyzeFoodResult> {
        return analyzeParsedResult(
            recognitionResult = recognitionResult,
            meatRatio = meatRatio,
            intakePercent = intakePercent,
            insulinType = insulinType,
            bloodSugarStart = bloodSugarStart,
        )
    }

    suspend fun analyzeParsedResult(
        recognitionResult: DishRecognitionResult,
        meatRatio: Float,
        intakePercent: Float,
        insulinType: InsulinType,
        bloodSugarStart: Float,
    ): NetworkResult<AnalyzeFoodResult> = coroutineScope {
        val ingredients = recognitionResult.dishes.flatMap { it.ingredients }
        Log.i(TAG, "===== Step 1~7 代谢计算开始 =====")
        Log.i(TAG, "[Step 0 识别食材(${ingredients.size}种)]: ${ingredients.joinToString { "${it.name}(${it.weightG}g, cat=${it.category}, cook=${it.cookingMethod}, gi=${it.giValue})" }}")
        if (ingredients.isEmpty()) {
            return@coroutineScope NetworkResult.Error(message = "No ingredients found from AI recognition.")
        }

        Log.i(TAG, "[Step 1 USDA请求] 查询食材基础碳水 (${ingredients.size} 个并行请求)...")
        val baseCarbDeferred = ingredients.map { ingredient ->
            async {
                val carbRes = nutritionRepository.getCarbsPer100g(ingredient.name, ingredient.nameEn)
                val carbDisplay = if (carbRes is NetworkResult.Success) "${carbRes.data}g/100g" else "查询失败"
                Log.i(TAG, "[Step 1 成分结果] 食材 '${ingredient.name}' (英文: '${ingredient.nameEn}') -> $carbDisplay")
                ingredient.name to carbRes
            }
        }.awaitAll()

        val baseCarbMap = mutableMapOf<String, Float>()
        for ((name, result) in baseCarbDeferred) {
            when (result) {
                is NetworkResult.Success -> baseCarbMap[name] = result.data
                is NetworkResult.Error -> return@coroutineScope result
            }
        }
        Log.i(TAG, "[Step 1 完成] USDA 碳水汇总表: $baseCarbMap")

        val correctionMap = ingredients
            .map { it.cookingMethod }
            .filter { method -> !method.isNullOrBlank() }
            .distinct()
            .associateWith { method ->
                cookingMethodDao.getByMethod(method)?.correctionValue ?: 0f
            }
        Log.i(TAG, "[Step 2 烹饪修正] 各方式修正系数: $correctionMap")

        val details = carbAndGiCalculator.calculateIngredientCarbs(
            ingredients = ingredients,
            baseCarbsPer100g = baseCarbMap,
            cookingCorrectionsPer100g = correctionMap,
        )
        val summary = carbAndGiCalculator.summarizeByCategory(details)
        Log.i(TAG, "[Step 2 荤素汇总] 素: ${summary.vegetable.carbsPerGram}g/g, 荤: ${summary.meat.carbsPerGram}g/g")

        val totalCarbIntake = carbAndGiCalculator.calculateTotalCarbIntake(
            summary = summary,
            meatRatio = meatRatio,
            intakePercent = intakePercent,
        )
        Log.i(TAG, "[Step 3 综合计算] 实际摄入碳水: ${totalCarbIntake}g (荤素比: $meatRatio, 摄入比: $intakePercent)")

        val weightedGi = carbAndGiCalculator.calculateWeightedGi(details)
        Log.i(TAG, "[Step 4 加权GI] 加权GI: $weightedGi")

        val prediction = bloodSugarPredictor.predict(
            bloodSugarStart = bloodSugarStart,
            totalCarbIntake = totalCarbIntake,
            giAvg = weightedGi,
        )
        val peak = prediction.points.maxByOrNull { it.bloodSugar }
        Log.i(TAG, "[Step 5 血糖预测完成] 峰值: ${"%.2f".format(peak?.bloodSugar ?: 0f)}mmol/L @ ${peak?.minute ?: 0}min")

        val insulinBand = insulinBandCalculator.calculate(
            insulinType = insulinType,
            totalCarbIntake = totalCarbIntake,
        )
        Log.i(TAG, "[Step 6 胰岛素色带] base: ${insulinBand.baseIU}IU, 蓝: ${insulinBand.blue.minIU}-${insulinBand.blue.maxIU}, 绿: ${insulinBand.green.minIU}-${insulinBand.green.maxIU}, 红: ${insulinBand.red.minIU}-${insulinBand.red.maxIU}")

        val focusPeriods = focusPeriodAnalyzer.analyze(prediction.points)
        Log.i(TAG, "[Step 7 重点时段] ${focusPeriods.size} 个关键时段")

        val reminders = if (focusPeriods.isEmpty()) {
            emptyList()
        } else {
            Log.i(TAG, "[Step 7 生成智能提醒] 发送请求...")
            try {
                when (val reminderResult = aiRepository.generateReminderText(focusPeriodAnalyzer.buildReminderPrompt(focusPeriods))) {
                    is NetworkResult.Success -> {
                        val lines = reminderResult.data
                            .lineSequence()
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                            .toList()
                        Log.i(TAG, "[Step 7 提醒成功] -> $lines")
                        lines
                    }
                    is NetworkResult.Error -> {
                        Log.w(TAG, "提醒生成失败(非致命): ${reminderResult.message}")
                        emptyList()
                    }
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.w(TAG, "提醒生成异常(非致命): ${e.message}")
                emptyList()
            }
        }

        Log.i(TAG, "===== 分析流水线全部完成 =====")
        NetworkResult.Success(
            AnalyzeFoodResult(
                totalCarbIntake = totalCarbIntake,
                weightedGi = weightedGi,
                bloodSugarPrediction = prediction,
                insulinBand = insulinBand,
                focusPeriods = focusPeriods,
                reminders = reminders,
                recognitionResult = recognitionResult,
                ingredientCarbsMap = baseCarbMap,
            ),
        )
    }

    companion object {
        val FOOD_ANALYSIS_PROMPT = """
            你是一位专业的糖尿病营养与代谢专家。请直接分析用户提供的餐盘图片中的所有菜品、汤品、甜品与主食。
            【重要原则】：无需寻找或参考硬币等外置标尺，请直接根据画面中常见餐具、碗盘、蒸笼容器的视觉比例与饱满度，直接判断估算分量。
            思考过程必须极度精简（50字内迅速确定菜品与分量），思考完成后立即输出合法的标准 JSON，严禁输出任何 Markdown 格式包裹（如 ```json）或说明文字。
            
            输出规范与要求：
            1. 分量判断：直接依据餐具视觉比例，合理估算每道菜与食材的净重（克）。
            2. 语言规范：所有菜品名称与食材名称必须全部使用标准【中文】。
            3. 每个食材项必须包含：
               - name: 中文食材名（例如：“西红柿”、“鸡蛋”、“米饭”、“瘦猪肉”、“西兰花”、“冰粉”、“红糖”、“紫菜”）
               - name_en: 对应 USDA/营养库英文检索词（例如：“tomato”、“whole egg”、“cooked rice”、“pork”、“broccoli”、“bingfen”、“brown sugar”、“cooked seaweed”）
               - category: 分类（仅限："荤" 或 "素"）
               - weight_g: 估算克重数值（整数或浮点数）
               - cooking_method: 烹饪方式（仅限中文："炒", "蒸", "煮", "红烧", "爆炒", "油炸", "凉拌", "烤", "煲汤", "白灼", "生食"）
               - gi_value: 升糖指数预估值（0-100 数值）
            4. 特殊食材精准辨识与检索规范（极重要，严格执行）：
               - 【冰粉（手搓冰粉、红糖冰粉）辨识规范（极高优先级）】：
                 若画面中为透明或晶莹剔透如果汁冻的胶状凝胶块、泡在红糖水或深浅色甜汤汁中、或带有碎冰块/勺子/小料（葡萄干、山楂碎、花生碎等），这是典型的夏日甜品【冰粉】（主料由植物假酸浆籽果胶或魔芋卡拉胶制成，【99%绝大多数都是水分】！纯冰粉本身几乎无碳水，每100g碳水仅1.5g，升糖指数低）。
                 必须准确命名菜品为“红糖冰粉”或“冰粉”，绝不可误判为咸味凉粉！
                 食材必须拆分为：
                   1) name: "冰粉", name_en: "bingfen", category: "素", weight_g: 150~200, cooking_method: "生食", gi_value: 15
                   2) name: "红糖", name_en: "brown sugar", category: "素", weight_g: 10~15, cooking_method: "煮", gi_value: 65
               - 【凉粉/凉皮（咸辣小吃）】：
                 仅当画面中为带有辣椒红油、蒜泥、生抽、黄瓜丝等咸辣佐料的块状/条状凝胶时，才属于咸食正餐“凉粉”或“凉皮”，含水量达90%，碳水密度约8-10%，食材 name 设为“凉粉”，name_en 设为“liangfen”或“starch jelly”，严禁使用生淀粉（corn starch）词汇。
               - 【禽蛋类食材】：
                 鸡蛋、蛋花汤、荷包蛋等，鲜鸡蛋碳水仅 ~1g/100g，name: "鸡蛋", name_en: "whole egg" 或 "fresh egg"，严禁使用易被误识别为挂面的词。
               - 【汤品干货泡发食材（紫菜/海带、裙带菜、木耳、虾皮等）】：
                 必须按【泡发前干重当量】估算（例如一碗普通紫菜蛋花汤中的紫菜干重通常仅 2-5g，切勿按泡水膨胀后重量估算为几十克干紫菜）；name_en 使用 "cooked seaweed" 或 "seaweed soup"。
               - 【熟食主食类（米饭、面条、馒头等）】：
                 必须按熟重估算，name_en 明确使用熟主食词（如 "cooked rice", "cooked noodles", "steamed bun"）。
            5. 最多识别 6 道菜品或主食组合。
            6. 输出格式：
               {"dishes":[{"dish_name":"中文菜名","ingredients":[{"name":"西红柿","name_en":"tomato","category":"素","weight_g":150,"cooking_method":"炒","gi_value":30}]}]}
        """.trimIndent()
    }
}
