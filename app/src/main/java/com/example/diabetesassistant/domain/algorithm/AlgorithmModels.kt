package com.example.diabetesassistant.domain.algorithm

import com.example.diabetesassistant.data.model.FoodCategory
import com.example.diabetesassistant.data.model.InsulinType

/**
 * 原料碳水详情。
 */
data class IngredientCarbDetail(
    val name: String,
    val category: String,
    val weightG: Float,
    val cookingMethod: String,
    val giValue: Float,
    val baseCarbsPer100g: Float,
    val cookingCorrectionPer100g: Float,
    val combinedCarbsPer100g: Float,
    val totalCarbsG: Float,
) {
    val foodCategory: FoodCategory
        get() = FoodCategory.from(category, name)
}

/**
 * 分类分组摘要。
 */
data class CategoryCarbSummary(
    val totalCarbsG: Float,
    val totalWeightG: Float,
    val carbsPerGram: Float,
)

/**
 * 菜品总碳水三元组分摘要（211 控糖模型：主食、蔬菜、荤菜）。
 */
data class MealCarbSummary(
    val staple: CategoryCarbSummary = CategoryCarbSummary(0f, 0f, 0f),
    val vegetable: CategoryCarbSummary = CategoryCarbSummary(0f, 0f, 0f),
    val meat: CategoryCarbSummary = CategoryCarbSummary(0f, 0f, 0f),
    val totalWeightG: Float = 0f,
    val totalCarbsG: Float = 0f,
)

/**
 * Gamma 参数。
 */
data class GammaParams(
    val a: Float,
    val b: Float,
)

/**
 * 血糖曲线点。
 */
data class BloodSugarPoint(
    val minute: Int,
    val bloodSugar: Float,
)

/**
 * 血糖预测结果。
 */
data class BloodSugarPrediction(
    val points: List<BloodSugarPoint>,
    val gammaParams: GammaParams,
    val peakMinute: Int,
    val peakBloodSugar: Float,
)

/**
 * 胰岛素色带分段。
 */
data class InsulinBandSegment(
    val minIU: Float,
    val maxIU: Float,
    val widthRatio: Float,
)

/**
 * 胰岛素色带结果。
 */
data class InsulinBandResult(
    val insulinType: InsulinType,
    val minIU: Float,
    val maxIU: Float,
    val baseIU: Float,
    val blue: InsulinBandSegment,
    val green: InsulinBandSegment,
    val red: InsulinBandSegment,
)

/**
 * 重点关注等级。
 */
enum class FocusLevel {
    HIGH,
    ELEVATED,
    LOW,
}

/**
 * 重点关注时段。
 */
data class FocusPeriod(
    val timeLabel: String,
    val hourAfterMeal: Float,
    val bloodSugar: Float,
    val level: FocusLevel,
)
