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
import com.google.gson.Gson
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

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
     * 执行完整餐食分析流程。
     */
    suspend operator fun invoke(
        imageUrl: String,
        meatRatio: Float,
        intakePercent: Float,
        insulinType: InsulinType,
        bloodSugarStart: Float = AlgorithmConstants.DEFAULT_BLOOD_SUGAR_START,
    ): NetworkResult<AnalyzeFoodResult> {
        val prompt = """
            你是一个食品营养分析专家。请分析图片中的菜品，输出 JSON。
            要求：
            1. 图片中有一枚硬币作为尺寸参照，据此估算每道菜和每种原料的大致重量
            2. 每道菜列出所有主要原料，标注原料名称、荤/素分类、估算重量(g)、烹饪方式
            3. 为每种原料估算 GI（升糖指数）值，范围 0~100
            4. 最多识别 5 道菜
        """.trimIndent()

        return when (val recognized = aiRepository.recognizeDishJson(imageUrl, prompt)) {
            is NetworkResult.Success -> {
                val parsed = try {
                    gson.fromJson(recognized.data, DishRecognitionResult::class.java)
                } catch (_: Exception) {
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

    private suspend fun analyzeParsedResult(
        recognitionResult: DishRecognitionResult,
        meatRatio: Float,
        intakePercent: Float,
        insulinType: InsulinType,
        bloodSugarStart: Float,
    ): NetworkResult<AnalyzeFoodResult> = coroutineScope {
        val ingredients = recognitionResult.dishes.flatMap { it.ingredients }
        if (ingredients.isEmpty()) {
            return@coroutineScope NetworkResult.Error(message = "No ingredients found from AI recognition.")
        }

        val baseCarbDeferred = ingredients.map { ingredient ->
            async {
                ingredient.name to nutritionRepository.getCarbsPer100g(ingredient.name)
            }
        }.awaitAll()

        val baseCarbMap = mutableMapOf<String, Float>()
        for ((name, result) in baseCarbDeferred) {
            when (result) {
                is NetworkResult.Success -> baseCarbMap[name] = result.data
                is NetworkResult.Error -> return@coroutineScope result
            }
        }

        val correctionMap = ingredients
            .map { it.cookingMethod }
            .distinct()
            .associateWith { method ->
                cookingMethodDao.getByMethod(method)?.correctionValue ?: 0f
            }

        val details = carbAndGiCalculator.calculateIngredientCarbs(
            ingredients = ingredients,
            baseCarbsPer100g = baseCarbMap,
            cookingCorrectionsPer100g = correctionMap,
        )
        val summary = carbAndGiCalculator.summarizeByCategory(details)
        val totalCarbIntake = carbAndGiCalculator.calculateTotalCarbIntake(
            summary = summary,
            meatRatio = meatRatio,
            intakePercent = intakePercent,
        )
        val weightedGi = carbAndGiCalculator.calculateWeightedGi(details)
        val prediction = bloodSugarPredictor.predict(
            bloodSugarStart = bloodSugarStart,
            totalCarbIntake = totalCarbIntake,
            giAvg = weightedGi,
        )
        val insulinBand = insulinBandCalculator.calculate(
            insulinType = insulinType,
            totalCarbIntake = totalCarbIntake,
        )
        val focusPeriods = focusPeriodAnalyzer.analyze(prediction.points)

        val reminders = if (focusPeriods.isEmpty()) {
            emptyList()
        } else {
            when (val reminderResult = aiRepository.generateReminderText(focusPeriodAnalyzer.buildReminderPrompt(focusPeriods))) {
                is NetworkResult.Success -> reminderResult.data
                    .lineSequence()
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .toList()
                is NetworkResult.Error -> return@coroutineScope reminderResult
            }
        }

        NetworkResult.Success(
            AnalyzeFoodResult(
                totalCarbIntake = totalCarbIntake,
                weightedGi = weightedGi,
                bloodSugarPrediction = prediction,
                insulinBand = insulinBand,
                focusPeriods = focusPeriods,
                reminders = reminders,
            ),
        )
    }
}
