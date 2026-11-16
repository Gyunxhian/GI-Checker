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
    val meatRatio: Float = 0.5f,
    val intakePercent: Float = 0.6f,
    val insulinType: String = "RAPID",
    val bloodSugarStart: Float = 5.0f,
    val recognitionResult: DishRecognitionResult? = null,
    val curvePoints: List<BloodSugarPoint>? = null,
    val insulinBand: InsulinBandResult? = null,
    val focusPeriods: List<FocusPeriod>? = null,
    val reminders: List<String>? = null,
    val ingredientCarbsMap: Map<String, Float>? = null,
)
