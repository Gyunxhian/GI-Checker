package com.example.diabetesassistant.domain.usecase

import com.example.diabetesassistant.domain.algorithm.BloodSugarPrediction
import com.example.diabetesassistant.domain.algorithm.BloodSugarPredictor
import com.example.diabetesassistant.domain.algorithm.CarbAndGiCalculator
import com.example.diabetesassistant.domain.algorithm.MealCarbSummary
import javax.inject.Inject

/**
 * Step 3~5 联动计算用例（支持 211 三元模型与旧二元模型）。
 */
class CalculateBloodSugarUseCase @Inject constructor(
    private val carbAndGiCalculator: CarbAndGiCalculator,
    private val bloodSugarPredictor: BloodSugarPredictor,
) {
    /**
     * 计算总碳水与血糖曲线（211 三元组分模型）。
     */
    operator fun invoke(
        summary: MealCarbSummary,
        stapleIntake: Float = 1.0f,
        vegIntake: Float = 1.0f,
        meatIntake: Float = 1.0f,
        weightedGi: Float,
        bloodSugarStart: Float,
    ): BloodSugarPrediction {
        val totalCarbIntake = carbAndGiCalculator.calculateTotalCarbIntake(
            summary = summary,
            stapleIntake = stapleIntake,
            vegIntake = vegIntake,
            meatIntake = meatIntake,
        )
        return bloodSugarPredictor.predict(
            bloodSugarStart = bloodSugarStart,
            totalCarbIntake = totalCarbIntake,
            giAvg = weightedGi,
        )
    }

    /**
     * 向后兼容旧二元签名。
     */
    operator fun invoke(
        summary: MealCarbSummary,
        meatRatio: Float,
        intakePercent: Float,
        weightedGi: Float,
        bloodSugarStart: Float,
    ): BloodSugarPrediction {
        val totalCarbIntake = carbAndGiCalculator.calculateTotalCarbIntake(
            summary = summary,
            meatRatio = meatRatio,
            intakePercent = intakePercent,
        )
        return bloodSugarPredictor.predict(
            bloodSugarStart = bloodSugarStart,
            totalCarbIntake = totalCarbIntake,
            giAvg = weightedGi,
        )
    }
}
