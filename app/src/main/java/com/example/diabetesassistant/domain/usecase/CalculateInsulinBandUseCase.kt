package com.example.diabetesassistant.domain.usecase

import com.example.diabetesassistant.data.model.InsulinType
import com.example.diabetesassistant.domain.algorithm.InsulinBandCalculator
import com.example.diabetesassistant.domain.algorithm.InsulinBandResult
import javax.inject.Inject

/**
 * Step 6 胰岛素色带计算用例。
 */
class CalculateInsulinBandUseCase @Inject constructor(
    private val insulinBandCalculator: InsulinBandCalculator,
) {
    /**
     * 计算胰岛素色带。
     */
    operator fun invoke(insulinType: InsulinType, totalCarbIntake: Float): InsulinBandResult {
        return insulinBandCalculator.calculate(insulinType, totalCarbIntake)
    }
}