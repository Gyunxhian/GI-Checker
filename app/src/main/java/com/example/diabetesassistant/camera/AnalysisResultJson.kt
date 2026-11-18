package com.example.diabetesassistant.camera

import com.example.diabetesassistant.data.model.DishRecognitionResult
import com.example.diabetesassistant.domain.algorithm.BloodSugarPoint
import com.example.diabetesassistant.domain.algorithm.FocusPeriod
import com.example.diabetesassistant.domain.algorithm.InsulinBandResult

/**
 * 完整餐食分析计算快照数据模型（用于真实持久化到 Room 及多页面反序列化）。
 */
data class AnalysisResultJson(
    val totalCarbIntake: Float = 0f,
    val weightedGi: Float = 0f,
    val meatRatio: Float? = null,
    val intakePercent: Float? = null,
    val stapleIntake: Float? = null,
    val vegIntake: Float? = null,
    val meatIntake: Float? = null,
    val insulinType: String = "RAPID",
    val bloodSugarStart: Float = 5.0f,
    val recognitionResult: DishRecognitionResult? = null,
    val curvePoints: List<BloodSugarPoint>? = null,
    val insulinBand: InsulinBandResult? = null,
    val focusPeriods: List<FocusPeriod>? = null,
    val reminders: List<String>? = null,
    val ingredientCarbsMap: Map<String, Float>? = null,
) {
    val effectiveMeatRatio: Float
        get() = meatRatio?.coerceIn(0f, 1f) ?: 0.5f

    val effectiveIntakePercent: Float
        get() = intakePercent?.coerceIn(0f, 1f) ?: 1.0f

    val effectiveStapleIntake: Float
        get() = stapleIntake?.coerceIn(0f, 1f) ?: effectiveIntakePercent

    val effectiveVegIntake: Float
        get() = vegIntake?.coerceIn(0f, 1f)
            ?: ((2f * (1f - effectiveMeatRatio)).coerceIn(0f, 1f) * effectiveIntakePercent).coerceIn(0f, 1f)

    val effectiveMeatIntake: Float
        get() = meatIntake?.coerceIn(0f, 1f)
            ?: ((2f * effectiveMeatRatio).coerceIn(0f, 1f) * effectiveIntakePercent).coerceIn(0f, 1f)
}
