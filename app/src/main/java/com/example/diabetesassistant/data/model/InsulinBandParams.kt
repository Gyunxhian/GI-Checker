package com.example.diabetesassistant.data.model

/**
 * 胰岛素色带参数模型。
 */
data class InsulinBandParams(
    val insulinType: InsulinType,
    val carbIntakeGram: Float,
    val carbRatio: Float = 12f,
)

/**
 * 胰岛素类型。
 */
enum class InsulinType {
    RAPID,
    SHORT,
    INTERMEDIATE,
    LONG,
}

/**
 * 胰岛素范围信息。
 */
data class InsulinRange(
    val minIU: Float,
    val maxIU: Float,
    val onset: String,
    val peak: String,
    val duration: String,
)

/**
 * 胰岛素范围常量。
 */
object InsulinRangeConstants {
    /**
     * 速效胰岛素。
     */
    val RAPID: InsulinRange = InsulinRange(
        minIU = 1f,
        maxIU = 20f,
        onset = "10-15min",
        peak = "1-2h",
        duration = "3-5h",
    )

    /**
     * 短效胰岛素。
     */
    val SHORT: InsulinRange = InsulinRange(
        minIU = 2f,
        maxIU = 25f,
        onset = "30-60min",
        peak = "2-3h",
        duration = "5-8h",
    )

    /**
     * 中效胰岛素。
     */
    val INTERMEDIATE: InsulinRange = InsulinRange(
        minIU = 4f,
        maxIU = 30f,
        onset = "1-2h",
        peak = "4-8h",
        duration = "12-18h",
    )

    /**
     * 长效胰岛素。
     */
    val LONG: InsulinRange = InsulinRange(
        minIU = 6f,
        maxIU = 40f,
        onset = "1-2h",
        peak = "无明显峰",
        duration = "20-24h",
    )

    /**
     * 根据类型获取范围。
     */
    fun byType(type: InsulinType): InsulinRange {
        return when (type) {
            InsulinType.RAPID -> RAPID
            InsulinType.SHORT -> SHORT
            InsulinType.INTERMEDIATE -> INTERMEDIATE
            InsulinType.LONG -> LONG
        }
    }
}
