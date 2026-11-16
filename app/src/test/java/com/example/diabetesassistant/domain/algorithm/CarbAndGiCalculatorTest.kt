package com.example.diabetesassistant.domain.algorithm

import com.example.diabetesassistant.data.model.Ingredient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [CarbAndGiCalculator].
 * Verifies direct net carb summation, meat weight invariance, ratio boundaries,
 * and the reference meal scenario (~30g - 35g).
 */
class CarbAndGiCalculatorTest {

    private val calculator = CarbAndGiCalculator()

    @Test
    fun calculateIngredientCarbs_computesBasePlusCorrectionTimesWeight() {
        val ingredients = listOf(
            Ingredient("番茄", "素", 150f, "炒", 15f),
            Ingredient("鸡蛋", "荤", 100f, "炒", 0f),
            Ingredient("白米饭", "素", 200f, "蒸", 83f),
        )
        val baseCarbsMap = mapOf(
            "番茄" to 3.9f,
            "鸡蛋" to 1.1f,
            "白米饭" to 28.6f,
        )
        val cookingCorrections = mapOf(
            "炒" to 2.0f,
            "蒸" to 0.0f,
        )

        val details = calculator.calculateIngredientCarbs(ingredients, baseCarbsMap, cookingCorrections)

        assertEquals(3, details.size)

        // 番茄: (3.9 + 2.0) * 150 / 100 = 8.85g
        assertEquals(5.9f, details[0].combinedCarbsPer100g, 0.0001f)
        assertEquals(8.85f, details[0].totalCarbsG, 0.0001f)

        // 鸡蛋: (1.1 + 2.0) * 100 / 100 = 3.1f
        assertEquals(3.1f, details[1].combinedCarbsPer100g, 0.0001f)
        assertEquals(3.1f, details[1].totalCarbsG, 0.0001f)

        // 白米饭: (28.6 + 0.0) * 200 / 100 = 57.2g
        assertEquals(28.6f, details[2].combinedCarbsPer100g, 0.0001f)
        assertEquals(57.2f, details[2].totalCarbsG, 0.0001f)
    }

    @Test
    fun summarizeByCategory_groupsVegMeatAndUnknownCategoriesCorrectly() {
        val details = listOf(
            IngredientCarbDetail("黄瓜", "素", 100f, "生吃", 15f, 2.9f, 0f, 2.9f, 2.9f),
            IngredientCarbDetail("牛肉", "荤", 150f, "炖", 0f, 0.5f, 0f, 0.5f, 0.75f),
            IngredientCarbDetail("老干妈", "调料", 20f, "调味", 30f, 10f, 0f, 10f, 2.0f), // unknown falls into veg category
        )

        val summary = calculator.summarizeByCategory(details)

        // Veg = 100g + 20g = 120g, Carbs = 2.9 + 2.0 = 4.9g
        assertEquals(120f, summary.vegetable.totalWeightG, 0.0001f)
        assertEquals(4.9f, summary.vegetable.totalCarbsG, 0.0001f)

        // Meat = 150g, Carbs = 0.75g
        assertEquals(150f, summary.meat.totalWeightG, 0.0001f)
        assertEquals(0.75f, summary.meat.totalCarbsG, 0.0001f)

        // Total
        assertEquals(270f, summary.totalWeightG, 0.0001f)
        assertEquals(5.65f, summary.totalCarbsG, 0.0001f)
    }

    @Test
    fun calculateTotalCarbIntake_directNetCarbSummation_balancedRatio() {
        val summary = MealCarbSummary(
            vegetable = CategoryCarbSummary(totalCarbsG = 20.0f, totalWeightG = 200f, carbsPerGram = 0.1f),
            meat = CategoryCarbSummary(totalCarbsG = 5.0f, totalWeightG = 100f, carbsPerGram = 0.05f),
            totalWeightG = 300f,
            totalCarbsG = 25.0f,
        )

        // At meatRatio = 0.5f, vegFactor = 1.0f, meatFactor = 1.0f
        // intakePercent = 1.0f -> expected = 20.0 + 5.0 = 25.0g
        val fullIntake = calculator.calculateTotalCarbIntake(summary, meatRatio = 0.5f, intakePercent = 1.0f)
        assertEquals(25.0f, fullIntake, 0.0001f)

        // intakePercent = 0.7f -> expected = 25.0 * 0.7 = 17.5g
        val partialIntake = calculator.calculateTotalCarbIntake(summary, meatRatio = 0.5f, intakePercent = 0.7f)
        assertEquals(17.5f, partialIntake, 0.0001f)
    }

    @Test
    fun calculateTotalCarbIntake_meatWeightInvariance_addingMeatDoesNotIncreaseVegCarbs() {
        // Base dish: 100g tomato stir-fry = 5.9g total carbs
        val vegDetail = IngredientCarbDetail(
            name = "番茄",
            category = "素",
            weightG = 100f,
            cookingMethod = "炒",
            giValue = 15f,
            baseCarbsPer100g = 3.9f,
            cookingCorrectionPer100g = 2.0f,
            combinedCarbsPer100g = 5.9f,
            totalCarbsG = 5.9f,
        )

        // Case 1: No meat added (0g meat)
        val summaryNoMeat = calculator.summarizeByCategory(listOf(vegDetail))
        val intakeNoMeat = calculator.calculateTotalCarbIntake(summaryNoMeat, meatRatio = 0.5f, intakePercent = 1.0f)

        // Case 2: Add 200g lean pork (0g carbs)
        val meat200g = IngredientCarbDetail(
            name = "瘦猪肉",
            category = "荤",
            weightG = 200f,
            cookingMethod = "炒",
            giValue = 0f,
            baseCarbsPer100g = 0f,
            cookingCorrectionPer100g = 0f,
            combinedCarbsPer100g = 0f,
            totalCarbsG = 0f,
        )
        val summary200gMeat = calculator.summarizeByCategory(listOf(vegDetail, meat200g))
        val intake200gMeat = calculator.calculateTotalCarbIntake(summary200gMeat, meatRatio = 0.5f, intakePercent = 1.0f)

        // Case 3: Add 500g pork belly stew (0g carbs)
        val meat500g = IngredientCarbDetail(
            name = "五花肉",
            category = "荤",
            weightG = 500f,
            cookingMethod = "炖",
            giValue = 0f,
            baseCarbsPer100g = 0f,
            cookingCorrectionPer100g = 0f,
            combinedCarbsPer100g = 0f,
            totalCarbsG = 0f,
        )
        val summary500gMeat = calculator.summarizeByCategory(listOf(vegDetail, meat500g))
        val intake500gMeat = calculator.calculateTotalCarbIntake(summary500gMeat, meatRatio = 0.5f, intakePercent = 1.0f)

        // Critical verification: In the old flawed implementation, adding 500g meat would inflate
        // the calculated carbs to over 15g! With direct summation, it strictly remains 5.9g.
        assertEquals(5.9f, intakeNoMeat, 0.001f)
        assertEquals(5.9f, intake200gMeat, 0.001f)
        assertEquals(5.9f, intake500gMeat, 0.001f)
    }

    @Test
    fun calculateTotalCarbIntake_extremeMeatRatios_handledCorrectly() {
        val summary = MealCarbSummary(
            vegetable = CategoryCarbSummary(totalCarbsG = 25.0f, totalWeightG = 200f, carbsPerGram = 25f / 200f),
            meat = CategoryCarbSummary(totalCarbsG = 10.0f, totalWeightG = 150f, carbsPerGram = 10f / 150f),
            totalWeightG = 350f,
            totalCarbsG = 35.0f,
        )

        // meatRatio = 0.0f -> vegFactor = 1.0f, meatFactor = 0.0f -> only veg carbs (25.0g)
        val onlyVeg = calculator.calculateTotalCarbIntake(summary, meatRatio = 0.0f, intakePercent = 1.0f)
        assertEquals(25.0f, onlyVeg, 0.0001f)

        // meatRatio = 1.0f -> vegFactor = 0.0f, meatFactor = 1.0f -> only meat carbs (10.0g)
        val onlyMeat = calculator.calculateTotalCarbIntake(summary, meatRatio = 1.0f, intakePercent = 1.0f)
        assertEquals(10.0f, onlyMeat, 0.0001f)

        // meatRatio = 0.5f -> vegFactor = 1.0f, meatFactor = 1.0f -> balanced (35.0g)
        val balanced = calculator.calculateTotalCarbIntake(summary, meatRatio = 0.5f, intakePercent = 1.0f)
        assertEquals(35.0f, balanced, 0.0001f)

        // meatRatio = 0.25f -> vegFactor = 1.0f, meatFactor = 0.5f -> 25.0 + 5.0 = 30.0g
        val moreVeg = calculator.calculateTotalCarbIntake(summary, meatRatio = 0.25f, intakePercent = 1.0f)
        assertEquals(30.0f, moreVeg, 0.0001f)

        // meatRatio = 0.75f -> vegFactor = 0.5f, meatFactor = 1.0f -> 12.5 + 10.0 = 22.5g
        val moreMeat = calculator.calculateTotalCarbIntake(summary, meatRatio = 0.75f, intakePercent = 1.0f)
        assertEquals(22.5f, moreMeat, 0.0001f)
    }

    @Test
    fun calculateTotalCarbIntake_ratioAndIntakeClamping() {
        val summary = MealCarbSummary(
            vegetable = CategoryCarbSummary(totalCarbsG = 20.0f, totalWeightG = 200f, carbsPerGram = 0.1f),
            meat = CategoryCarbSummary(totalCarbsG = 10.0f, totalWeightG = 100f, carbsPerGram = 0.1f),
            totalWeightG = 300f,
            totalCarbsG = 30.0f,
        )

        // Negative ratio clamped to 0f
        val underflowRatio = calculator.calculateTotalCarbIntake(summary, meatRatio = -0.5f, intakePercent = 1.0f)
        assertEquals(20.0f, underflowRatio, 0.0001f)

        // Overflow ratio clamped to 1f
        val overflowRatio = calculator.calculateTotalCarbIntake(summary, meatRatio = 1.5f, intakePercent = 1.0f)
        assertEquals(10.0f, overflowRatio, 0.0001f)

        // Negative intake clamped to 0f
        val negativeIntake = calculator.calculateTotalCarbIntake(summary, meatRatio = 0.5f, intakePercent = -0.2f)
        assertEquals(0.0f, negativeIntake, 0.0001f)

        // Overflow intake clamped to 1f
        val overflowIntake = calculator.calculateTotalCarbIntake(summary, meatRatio = 0.5f, intakePercent = 1.8f)
        assertEquals(30.0f, overflowIntake, 0.0001f)
    }

    @Test
    fun calculateTotalCarbIntake_pureVegetableMeal_retainsFullCarbsAtDefaultRatio() {
        // Pure vegetarian dish: broccoli + rice, 0g meat
        val summary = MealCarbSummary(
            vegetable = CategoryCarbSummary(totalCarbsG = 45.0f, totalWeightG = 300f, carbsPerGram = 0.15f),
            meat = CategoryCarbSummary(totalCarbsG = 0f, totalWeightG = 0f, carbsPerGram = 0f),
            totalWeightG = 300f,
            totalCarbsG = 45.0f,
        )

        // At default balanced slider (meatRatio = 0.5f), vegFactor = 1.0f
        val intake = calculator.calculateTotalCarbIntake(summary, meatRatio = 0.5f, intakePercent = 1.0f)
        assertEquals(45.0f, intake, 0.0001f)
    }

    @Test
    fun calculateTotalCarbIntake_pureMeatMeal_retainsFullCarbsAtDefaultRatio() {
        // Pure meat dish with slight marinade carbs: 0g veg
        val summary = MealCarbSummary(
            vegetable = CategoryCarbSummary(totalCarbsG = 0f, totalWeightG = 0f, carbsPerGram = 0f),
            meat = CategoryCarbSummary(totalCarbsG = 6.0f, totalWeightG = 250f, carbsPerGram = 6f / 250f),
            totalWeightG = 250f,
            totalCarbsG = 6.0f,
        )

        // At default balanced slider (meatRatio = 0.5f), meatFactor = 1.0f
        val intake = calculator.calculateTotalCarbIntake(summary, meatRatio = 0.5f, intakePercent = 1.0f)
        assertEquals(6.0f, intake, 0.0001f)
    }

    @Test
    fun calculateTotalCarbIntake_emptySummary_returnsZero() {
        val summary = MealCarbSummary(
            vegetable = CategoryCarbSummary(totalCarbsG = 0f, totalWeightG = 0f, carbsPerGram = 0f),
            meat = CategoryCarbSummary(totalCarbsG = 0f, totalWeightG = 0f, carbsPerGram = 0f),
            totalWeightG = 0f,
            totalCarbsG = 0f,
        )

        val intake = calculator.calculateTotalCarbIntake(summary, meatRatio = 0.5f, intakePercent = 1.0f)
        assertEquals(0.0f, intake, 0.0001f)
    }

    @Test
    fun referenceMealScenario_seaweedSoupLiangfenGreenPepperPork_fallsWithinRealistic30to35g() {
        // Real-world meal scenario from specification:
        // 1. Seaweed egg drop soup (紫菜蛋花汤):
        //    - 30g cooked/rehydrated seaweed @ 5.3g/100g base carbs
        //    - 60g egg flower @ 1.1g/100g base carbs
        //    - 300g soup broth @ 0g/100g base carbs
        // 2. Liangfen (凉粉):
        //    - 180g high-moisture starch gel @ 9.2g/100g base carbs
        // 3. Green pepper pork (青椒五花肉):
        //    - 160g green pepper @ 5.8g/100g base carbs + 2.0g/100g stir-fry correction
        //    - 120g pork belly @ 0g/100g base carbs
        val ingredients = listOf(
            Ingredient(name = "熟紫菜", category = "素", weightG = 30f, cookingMethod = "煮", giValue = 15f),
            Ingredient(name = "鸡蛋", category = "荤", weightG = 60f, cookingMethod = "煮", giValue = 0f),
            Ingredient(name = "清汤", category = "素", weightG = 300f, cookingMethod = "煮", giValue = 0f),
            Ingredient(name = "凉粉", category = "素", weightG = 180f, cookingMethod = "凉拌", giValue = 40f),
            Ingredient(name = "青椒", category = "素", weightG = 160f, cookingMethod = "炒", giValue = 15f),
            Ingredient(name = "五花肉", category = "荤", weightG = 120f, cookingMethod = "炒", giValue = 0f),
        )

        val baseCarbsMap = mapOf(
            "熟紫菜" to 5.3f,
            "鸡蛋" to 1.1f,
            "清汤" to 0.0f,
            "凉粉" to 9.2f,
            "青椒" to 5.8f,
            "五花肉" to 0.0f,
        )

        val cookingCorrections = mapOf(
            "炒" to 2.0f,
            "煮" to 0.0f,
            "凉拌" to 0.0f,
        )

        val details = calculator.calculateIngredientCarbs(ingredients, baseCarbsMap, cookingCorrections)
        val summary = calculator.summarizeByCategory(details)

        // 100% meal intake with balanced meat/veg proportion (0.5f)
        val totalCarbIntake = calculator.calculateTotalCarbIntake(summary, meatRatio = 0.5f, intakePercent = 1.0f)

        // Mathematical verification:
        // Seaweed: 30 * 5.3 / 100 = 1.59g
        // Egg: 60 * 1.1 / 100 = 0.66g
        // Broth: 300 * 0 / 100 = 0.00g
        // Liangfen: 180 * 9.2 / 100 = 16.56g
        // Green pepper: 160 * (5.8 + 2.0) / 100 = 12.48g
        // Pork belly (stir-fry correction 2.0g/100g): 120 * (0 + 2.0) / 100 = 2.40g
        // Total expected net carbs = 1.59 + 0.66 + 0 + 16.56 + 12.48 + 2.40 = 33.69g
        assertEquals(33.69f, totalCarbIntake, 0.05f)

        // Acceptance Criteria:
        // Total meal carbohydrate strictly falls within the realistic ~30g - 35g range (instead of inflating to 50g+)
        assertTrue(
            "Reference meal carbohydrate intake ($totalCarbIntake g) must fall within 30g - 35g range",
            totalCarbIntake in 30.0f..35.0f
        )
    }

    @Test
    fun calculateWeightedGi_weightedByCarbContribution() {
        val details = listOf(
            IngredientCarbDetail("米饭", "素", 100f, "蒸", 83f, 28f, 0f, 28f, 28.0f), // 28g carb, GI 83
            IngredientCarbDetail("菠菜", "素", 200f, "煮", 15f, 2f, 0f, 2f, 4.0f),   // 4g carb, GI 15
            IngredientCarbDetail("鸡胸肉", "荤", 150f, "水煮", 0f, 0f, 0f, 0f, 0.0f), // 0g carb, GI 0
        )

        val weightedGi = calculator.calculateWeightedGi(details)

        // Total carbs = 32.0g
        // Weighted GI = (28 / 32 * 83) + (4 / 32 * 15) = 72.625 + 1.875 = 74.5f
        assertEquals(74.5f, weightedGi, 0.01f)
    }

    @Test
    fun calculateWeightedGi_zeroCarbDetails_returnsZero() {
        val details = listOf(
            IngredientCarbDetail("纯瘦肉", "荤", 200f, "煮", 0f, 0f, 0f, 0f, 0f),
            IngredientCarbDetail("饮用水", "素", 500f, "煮", 0f, 0f, 0f, 0f, 0f),
        )

        val weightedGi = calculator.calculateWeightedGi(details)
        assertEquals(0.0f, weightedGi, 0.0001f)
    }

    @Test
    fun mapGiToGammaParams_computesAccurateParameters() {
        val gammaLow = calculator.mapGiToGammaParams(0f)
        assertEquals(3.0f, gammaLow.a, 0.0001f)
        assertEquals(25.0f, gammaLow.b, 0.0001f)

        val gammaMid = calculator.mapGiToGammaParams(50f)
        assertEquals(2.25f, gammaMid.a, 0.0001f)
        assertEquals(22.5f, gammaMid.b, 0.0001f)

        val gammaHigh = calculator.mapGiToGammaParams(100f)
        assertEquals(1.5f, gammaHigh.a, 0.0001f)
        assertEquals(20.0f, gammaHigh.b, 0.0001f)
    }
}
