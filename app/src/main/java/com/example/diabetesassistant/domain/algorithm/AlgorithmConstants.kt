package com.example.diabetesassistant.domain.algorithm

/**
 * 核心算法常量。
 */
object AlgorithmConstants {
    /** 血糖换算系数。 */
    const val BLOOD_SUGAR_CONVERSION_K: Float = 0.05f

    /** 简化碳水系数。 */
    const val CARB_RATIO: Float = 12f

    /** 高血糖阈值。 */
    const val HIGH_BLOOD_SUGAR_THRESHOLD: Float = 10.0f

    /** 偏高血糖阈值。 */
    const val ELEVATED_BLOOD_SUGAR_THRESHOLD: Float = 7.8f

    /** 低血糖阈值。 */
    const val LOW_BLOOD_SUGAR_THRESHOLD: Float = 3.9f

    /** 默认起始血糖。 */
    const val DEFAULT_BLOOD_SUGAR_START: Float = 5.0f

    /** 血糖曲线分钟数。 */
    const val BLOOD_SUGAR_POINT_COUNT: Int = 240
}
