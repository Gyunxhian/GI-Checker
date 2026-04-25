package com.example.diabetesassistant.domain.usecase

import com.example.diabetesassistant.domain.algorithm.BloodSugarPrediction
import com.example.diabetesassistant.domain.algorithm.BloodSugarPredictor
import com.example.diabetesassistant.domain.algorithm.CarbAndGiCalculator
import com.example.diabetesassistant.domain.algorithm.MealCarbSummary
import javax.inject.Inject

/**
 * Step 3~5 联动计算用例。
 */
class CalculateBloodSugarUseCase @Inject constructor(
    private val carbAndGiCalculator: CarbAndGiCalculator,
    private val bloodSugarPredictor: BloodSugarPredictor,
) {
    /**
     * 计算总碳水与血糖曲线。
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
