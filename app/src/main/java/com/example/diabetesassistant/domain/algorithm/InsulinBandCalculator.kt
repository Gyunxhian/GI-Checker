package com.example.diabetesassistant.domain.algorithm

import com.example.diabetesassistant.data.model.InsulinRangeConstants
import com.example.diabetesassistant.data.model.InsulinType
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

/**
 * 胰岛素色带计算器。
 */
class InsulinBandCalculator @Inject constructor() {
    /**
     * Step 6：计算三段胰岛素色带。
     */
    fun calculate(insulinType: InsulinType, totalCarbIntake: Float): InsulinBandResult {
        val range = InsulinRangeConstants.byType(insulinType)
        val minIU = range.minIU
        val maxIU = range.maxIU
        val baseIU = (totalCarbIntake / AlgorithmConstants.CARB_RATIO).coerceIn(minIU, maxIU)

        val blueEnd = max(minIU, min(maxIU, baseIU * 0.7f))
        val greenEnd = max(blueEnd, min(maxIU, baseIU * 1.3f))
        val totalSpan = (maxIU - minIU).coerceAtLeast(1e-6f)

        val blue = InsulinBandSegment(
            minIU = minIU,
            maxIU = blueEnd,
            widthRatio = (blueEnd - minIU) / totalSpan,
        )
        val green = InsulinBandSegment(
            minIU = blueEnd,
            maxIU = greenEnd,
            widthRatio = (greenEnd - blueEnd) / totalSpan,
        )
        val red = InsulinBandSegment(
            minIU = greenEnd,
            maxIU = maxIU,
            widthRatio = (maxIU - greenEnd) / totalSpan,
        )

        return InsulinBandResult(
            insulinType = insulinType,
            minIU = minIU,
            maxIU = maxIU,
            baseIU = baseIU,
            blue = blue,
            green = green,
            red = red,
        )
    }
}