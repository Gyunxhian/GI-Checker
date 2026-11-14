package com.example.diabetesassistant.domain.algorithm

import com.example.diabetesassistant.data.model.Ingredient
import javax.inject.Inject
import kotlin.math.max

/**
 * 碳水与 GI 计算器。
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
            val combined = baseCarbs + correction
            val totalCarbs = combined * ingredient.weightG / 100f
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
     * Step 2：按荤素分类汇总。
     */
    fun summarizeByCategory(details: List<IngredientCarbDetail>): MealCarbSummary {
        val veg = details.filter {
            it.category.equals("vegetable", ignoreCase = true) ||
            it.category.contains("素")
        }
        val meat = details.filter {
            it.category.equals("meat", ignoreCase = true) ||
            it.category.contains("荤")
        }
        val unknown = details.filter { d ->
            !d.category.equals("vegetable", ignoreCase = true) &&
            !d.category.contains("素") &&
            !d.category.equals("meat", ignoreCase = true) &&
            !d.category.contains("荤")
        }

        val vegWeight = (veg.sumOf { it.weightG.toDouble() } + unknown.sumOf { it.weightG.toDouble() }).toFloat()
        val vegCarbs = (veg.sumOf { it.totalCarbsG.toDouble() } + unknown.sumOf { it.totalCarbsG.toDouble() }).toFloat()
        val meatWeight = meat.sumOf { it.weightG.toDouble() }.toFloat()
        val meatCarbs = meat.sumOf { it.totalCarbsG.toDouble() }.toFloat()

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
            vegetable = vegSummary,
            meat = meatSummary,
            totalWeightG = vegWeight + meatWeight,
            totalCarbsG = vegCarbs + meatCarbs,
        )
    }

    /**
     * Step 3：计算总碳水摄入。
     */
    fun calculateTotalCarbIntake(
        summary: MealCarbSummary,
        meatRatio: Float,
        intakePercent: Float,
    ): Float {
        val ratio = meatRatio.coerceIn(0f, 1f)
        val intake = intakePercent.coerceIn(0f, 1f)
        val carbsPerGram =
            summary.vegetable.carbsPerGram * (1f - ratio) + summary.meat.carbsPerGram * ratio
        return carbsPerGram * intake * summary.totalWeightG
    }

    /**
     * Step 4：计算加权平均 GI。
     */
    fun calculateWeightedGi(details: List<IngredientCarbDetail>): Float {
        val totalCarbs = details.sumOf { max(it.totalCarbsG, 0f).toDouble() }.toFloat()
        if (totalCarbs <= 0f) return 0f
        val weightedSum = details.sumOf {
            (max(it.totalCarbsG, 0f) / totalCarbs * it.giValue).toDouble()
        }.toFloat()
        return weightedSum
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
