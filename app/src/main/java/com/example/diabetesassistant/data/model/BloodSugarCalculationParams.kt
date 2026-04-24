package com.example.diabetesassistant.data.model

/**
 * 血糖计算参数模型。
 */
data class BloodSugarCalculationParams(
    val bloodSugarStart: Float = 5.0f,
    val meatRatio: Float,
    val intakePercent: Float,
    val conversionK: Float = 0.05f,
)
