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
        val prompt = """
            你是一位专业的糖尿病营养与代谢专家。请细致分析用户提供的餐盘图片（可能包含一道或多道菜品、汤品与主食）。
            请严格输出合法的 JSON，禁止包含任何思考标签或 Markdown 文本。
            
            输出规范与要求：
            1. 图中可能包含硬币或餐盘边缘作为尺寸参照物，请据此合理估算每道菜与食材的真实重量（克）。
            2. 所有菜品名称与食材名称必须全部使用标准【中文】。
            3. 每个食材项必须包含：
               - name: 中文食材名（例如：“西红柿”、“鸡蛋”、“米饭”、“瘦猪肉”、“西兰花”）
               - name_en: 对应 USDA 营养库英文检索词（例如：“tomato”、“egg”、“rice”、“pork”、“broccoli”）
               - category: 分类（仅限："荤" 或 "素"）
               - weight_g: 估算克重数值
               - cooking_method: 烹饪方式（仅限中文："炒", "蒸", "煮", "红烧", "爆炒", "油炸", "凉拌", "烤", "煲汤", "白灼", "生食"）
               - gi_value: 升糖指数预估值（0-100 数值）
            4. 最多识别 6 道菜品或主食组合。
            5. 输出格式：
               {"dishes":[{"dish_name":"中文菜名","ingredients":[{"name":"西红柿","name_en":"tomato","category":"素","weight_g":150,"cooking_method":"炒","gi_value":30}]}],"coin_detected":true}
        """.trimIndent()

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
                val queryKey = ingredient.nameEn?.takeIf { it.isNotBlank() } ?: ingredient.name
                val carbRes = nutritionRepository.getCarbsPer100g(queryKey)
                val carbDisplay = if (carbRes is NetworkResult.Success) "${carbRes.data}g/100g" else "查询失败"
                Log.i(TAG, "[Step 1 USDA结果] 食材 '${ingredient.name}' (检索词: '$queryKey') -> $carbDisplay")
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
}
