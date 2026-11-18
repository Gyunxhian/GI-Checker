package com.example.diabetesassistant.domain.algorithm

import com.example.diabetesassistant.data.model.FoodCategory
import com.example.diabetesassistant.data.model.Ingredient
import javax.inject.Inject
import kotlin.math.max

/**
 * 碳水与 GI 计算器（211 控糖三元组分模型）。
 */
class CarbAndGiCalculator @Inject constructor() {
    /**
     * Step 1：计算每种原料综合碳水与总碳水。
     */
    fun calculateIngredientCarbs(
        ingredients: List<Ingredient>,
        baseCarbsPer100g: Map<String, Float>,
        cookingCorrectionsPer100g: Map<String, Float>,
    ): List<IngredientCarbDetail> {
        return ingredients.map { ingredient ->
            val baseCarbs = baseCarbsPer100g[ingredient.name] ?: 0f
            val correction = cookingCorrectionsPer100g[ingredient.cookingMethod] ?: 0f
            val combined = maxOf(0f, baseCarbs + correction)
            val totalCarbs = combined * maxOf(0f, ingredient.weightG) / 100f
            IngredientCarbDetail(
                name = ingredient.name,
                category = ingredient.category,
                weightG = ingredient.weightG,
                cookingMethod = ingredient.cookingMethod,
                giValue = ingredient.giValue,
                baseCarbsPer100g = baseCarbs,
                cookingCorrectionPer100g = correction,
                combinedCarbsPer100g = combined,
                totalCarbsG = totalCarbs,
            )
        }
    }

    /**
     * Step 2：按三元组分（主食 Staple、蔬菜 Vegetable、荤菜 Meat）分类汇总。
     */
    fun summarizeByCategory(details: List<IngredientCarbDetail>): MealCarbSummary {
        val staple = details.filter { FoodCategory.from(it.category, it.name) == FoodCategory.STAPLE }
        val veg = details.filter { FoodCategory.from(it.category, it.name) == FoodCategory.VEGETABLE }
        val meat = details.filter { FoodCategory.from(it.category, it.name) == FoodCategory.MEAT }

        val stapleWeight = maxOf(0f, staple.sumOf { it.weightG.toDouble() }.toFloat())
        val stapleCarbs = maxOf(0f, staple.sumOf { it.totalCarbsG.toDouble() }.toFloat())

        val vegWeight = maxOf(0f, veg.sumOf { it.weightG.toDouble() }.toFloat())
        val vegCarbs = maxOf(0f, veg.sumOf { it.totalCarbsG.toDouble() }.toFloat())

        val meatWeight = maxOf(0f, meat.sumOf { it.weightG.toDouble() }.toFloat())
        val meatCarbs = maxOf(0f, meat.sumOf { it.totalCarbsG.toDouble() }.toFloat())

        val stapleSummary = CategoryCarbSummary(
            totalCarbsG = stapleCarbs,
            totalWeightG = stapleWeight,
            carbsPerGram = if (stapleWeight > 0f) stapleCarbs / stapleWeight else 0f,
        )
        val vegSummary = CategoryCarbSummary(
            totalCarbsG = vegCarbs,
            totalWeightG = vegWeight,
            carbsPerGram = if (vegWeight > 0f) vegCarbs / vegWeight else 0f,
        )
        val meatSummary = CategoryCarbSummary(
            totalCarbsG = meatCarbs,
            totalWeightG = meatWeight,
            carbsPerGram = if (meatWeight > 0f) meatCarbs / meatWeight else 0f,
        )

        return MealCarbSummary(
            staple = stapleSummary,
            vegetable = vegSummary,
            meat = meatSummary,
            totalWeightG = stapleWeight + vegWeight + meatWeight,
            totalCarbsG = stapleCarbs + vegCarbs + meatCarbs,
        )
    }

    /**
     * Step 3：计算总碳水摄入（211 三元组分严格线性质量守恒公式）。
     * totalCarbIntake = C_staple * I_staple + C_veg * I_veg + C_meat * I_meat
     */
    fun calculateTotalCarbIntake(
        summary: MealCarbSummary,
        stapleIntake: Float = 1.0f,
        vegIntake: Float = 1.0f,
        meatIntake: Float = 1.0f,
    ): Float {
        val s = stapleIntake.coerceIn(0f, 1f)
        val v = vegIntake.coerceIn(0f, 1f)
        val m = meatIntake.coerceIn(0f, 1f)
        val sCarbs = maxOf(0f, summary.staple.totalCarbsG)
        val vCarbs = maxOf(0f, summary.vegetable.totalCarbsG)
        val mCarbs = maxOf(0f, summary.meat.totalCarbsG)
        val total = sCarbs * s + vCarbs * v + mCarbs * m
        return if (total.isNaN()) 0f else total
    }

    /**
     * 向后兼容的 Step 3 重载（旧二元荤素比）。
     */
    fun calculateTotalCarbIntake(
        summary: MealCarbSummary,
        meatRatio: Float,
        intakePercent: Float,
    ): Float {
        val ratio = meatRatio.coerceIn(0f, 1f)
        val intake = intakePercent.coerceIn(0f, 1f)
        val vegFactor = (2f * (1f - ratio)).coerceIn(0f, 1f)
        val meatFactor = (2f * ratio).coerceIn(0f, 1f)
        val netCarbs = summary.staple.totalCarbsG + summary.vegetable.totalCarbsG * vegFactor + summary.meat.totalCarbsG * meatFactor
        return netCarbs * intake
    }

    /**
     * Step 4：计算动态加权平均 GI。
     * weightedGi = sum(C_i * I_i * GI_i) / totalCarbIntake
     * 若 totalCarbIntake <= 0，回退至餐品平均有效 GI，若无则回退至 55f。
     */
    fun calculateWeightedGi(
        details: List<IngredientCarbDetail>,
        stapleIntake: Float = 1.0f,
        vegIntake: Float = 1.0f,
        meatIntake: Float = 1.0f,
    ): Float {
        val s = stapleIntake.coerceIn(0f, 1f)
        val v = vegIntake.coerceIn(0f, 1f)
        val m = meatIntake.coerceIn(0f, 1f)

        var effectiveTotalCarbs = 0f
        var weightedSum = 0f

        for (d in details) {
            val intake = when (FoodCategory.from(d.category, d.name)) {
                FoodCategory.STAPLE -> s
                FoodCategory.VEGETABLE -> v
                FoodCategory.MEAT -> m
            }
            val netCarb = maxOf(0f, d.totalCarbsG) * intake
            effectiveTotalCarbs += netCarb
            weightedSum += netCarb * maxOf(0f, d.giValue)
        }

        if (effectiveTotalCarbs > 0f) {
            val gi = weightedSum / effectiveTotalCarbs
            return if (gi.isNaN()) 55f else gi.coerceIn(0f, 100f)
        }

        // Fallback when totalCarbIntake <= 0
        val positiveGis = details.map { it.giValue }.filter { it > 0f }
        return when {
            positiveGis.isNotEmpty() -> positiveGis.average().toFloat().coerceIn(0f, 100f)
            details.isNotEmpty() -> 0f
            else -> 55f
        }
    }

    /**
     * Step 4：GI 映射到 Gamma 参数。
     */
    fun mapGiToGammaParams(giAvg: Float): GammaParams {
        val gi = giAvg.coerceIn(0f, 100f)
        val a = 3.0f - (gi / 100f) * 1.5f
        val b = 25f - (gi / 100f) * 5f
        return GammaParams(a = a, b = b)
    }
}
