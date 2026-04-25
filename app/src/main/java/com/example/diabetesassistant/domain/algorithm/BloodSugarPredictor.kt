package com.example.diabetesassistant.domain.algorithm

import javax.inject.Inject
import kotlin.math.exp
import kotlin.math.pow

/**
 * 血糖预测器。
 */
class BloodSugarPredictor @Inject constructor(
    private val carbAndGiCalculator: CarbAndGiCalculator,
) {
    /**
     * Step 5：生成血糖波动预测曲线。
     */
    fun predict(
        bloodSugarStart: Float,
        totalCarbIntake: Float,
        giAvg: Float,
        pointCount: Int = AlgorithmConstants.BLOOD_SUGAR_POINT_COUNT,
    ): BloodSugarPrediction {
        val gamma = carbAndGiCalculator.mapGiToGammaParams(giAvg)
        val points = (0 until pointCount).map { minute ->
            val absorption = calculateAbsorptionAt(minute.toFloat(), gamma)
            val bg = bloodSugarStart +
                totalCarbIntake * (giAvg / 100f) * AlgorithmConstants.BLOOD_SUGAR_CONVERSION_K * absorption
            BloodSugarPoint(minute = minute, bloodSugar = bg)
        }
        val peak = points.maxByOrNull { it.bloodSugar } ?: BloodSugarPoint(0, bloodSugarStart)
        return BloodSugarPrediction(
            points = points,
            gammaParams = gamma,
            peakMinute = peak.minute,
            peakBloodSugar = peak.bloodSugar,
        )
    }

    /**
     * Gamma 归一化吸收值。
     */
    fun calculateAbsorptionAt(minute: Float, gammaParams: GammaParams): Float {
        if (minute <= 0f) return 0f
        val a = gammaParams.a
        val b = gammaParams.b
        val numerator = minute.pow(a) * exp(-minute / b)
        val maxValue = (a * b).pow(a) * exp(-a)
        if (maxValue <= 0f) return 0f
        return numerator / maxValue
    }
}