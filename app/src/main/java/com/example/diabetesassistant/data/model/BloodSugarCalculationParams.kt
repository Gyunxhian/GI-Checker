package com.example.diabetesassistant.data.model

/**
 * 血糖计算参数模型（支持 211 三元组分摄入与向后兼容旧二元比例）。
 */
data class BloodSugarCalculationParams(
    val bloodSugarStart: Float = 5.0f,
    val stapleIntake: Float = 1.0f,
    val vegIntake: Float = 1.0f,
    val meatIntake: Float = 1.0f,
    val meatRatio: Float = 0.5f,
    val intakePercent: Float = 1.0f,
    val conversionK: Float = 0.05f,
)
