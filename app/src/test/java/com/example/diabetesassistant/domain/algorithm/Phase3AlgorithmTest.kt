package com.example.diabetesassistant.domain.algorithm

import com.example.diabetesassistant.data.model.Ingredient
import com.example.diabetesassistant.data.model.InsulinType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Phase3AlgorithmTest {
    private val calculator = CarbAndGiCalculator()

    @Test
    fun step1_calculatesCombinedAndTotalCarbs_forTomatoAndEgg() {
        val ingredients = listOf(
            Ingredient("番茄", "素", 150f, "炒", 15f),
            Ingredient("鸡蛋", "荤", 100f, "炒", 0f),
        )
        val details = calculator.calculateIngredientCarbs(
            ingredients = ingredients,
            baseCarbsPer100g = mapOf("番茄" to 3.9f, "鸡蛋" to 1.1f),
            cookingCorrectionsPer100g = mapOf("炒" to 2f),
        )

        assertEquals(2, details.size)
        assertEquals(5.9f, details[0].combinedCarbsPer100g, 0.0001f)
        assertEquals(8.85f, details[0].totalCarbsG, 0.0001f)
        assertEquals(3.1f, details[1].combinedCarbsPer100g, 0.0001f)
        assertEquals(3.1f, details[1].totalCarbsG, 0.0001f)
    }

    @Test
    fun step2_summarizesVegetableAndMeatCarbs() {
        val details = listOf(
            IngredientCarbDetail("番茄", "素", 150f, "炒", 15f, 3.9f, 2f, 5.9f, 8.85f),
            IngredientCarbDetail("鸡蛋", "荤", 100f, "炒", 0f, 1.1f, 2f, 3.1f, 3.1f),
            IngredientCarbDetail("冰糖", "素", 15f, "红烧", 65f, 65f, 5f, 70f, 9.75f),
        )

        val summary = calculator.summarizeByCategory(details)

        assertEquals(18.6f, summary.vegetable.totalCarbsG, 0.0001f)
        assertEquals(165f, summary.vegetable.totalWeightG, 0.0001f)
        assertEquals(3.1f, summary.meat.totalCarbsG, 0.0001f)
        assertEquals(100f, summary.meat.totalWeightG, 0.0001f)
        assertEquals(265f, summary.totalWeightG, 0.0001f)
        assertEquals(21.7f, summary.totalCarbsG, 0.0001f)
    }

    @Test
    fun step3_calculatesTotalCarbIntake_fromRatios() {
        val summary = MealCarbSummary(
            vegetable = CategoryCarbSummary(totalCarbsG = 18.6f, totalWeightG = 165f, carbsPerGram = 18.6f / 165f),
            meat = CategoryCarbSummary(totalCarbsG = 3.1f, totalWeightG = 100f, carbsPerGram = 3.1f / 100f),
            totalWeightG = 265f,
            totalCarbsG = 21.7f,
        )

        val intake = calculator.calculateTotalCarbIntake(summary, meatRatio = 0.5f, intakePercent = 0.6f)

        // meatRatio = 0.5f -> vegFactor = 1.0f, meatFactor = 1.0f -> netCarbs = 18.6f + 3.1f = 21.7f
        // intakePercent = 0.6f -> expected = 21.7f * 0.6f = 13.02f
        val expected = (18.6f + 3.1f) * 0.6f
        assertEquals(expected, intake, 0.0001f)
    }

    @Test
    fun step4_calculatesWeightedGi_equals35Point3() {
        val details = listOf(
            IngredientCarbDetail("番茄", "素", 150f, "炒", 15f, 3.9f, 2f, 5.9f, 8.85f),
            IngredientCarbDetail("鸡蛋", "荤", 100f, "炒", 0f, 1.1f, 2f, 3.1f, 3.1f),
            IngredientCarbDetail("五花肉", "荤", 200f, "红烧", 0f, 0f, 5f, 5f, 0f),
            IngredientCarbDetail("冰糖", "素", 15f, "红烧", 65f, 60f, 5f, 65f, 9.75f),
        )

        val gi = calculator.calculateWeightedGi(details)
        val gamma = calculator.mapGiToGammaParams(gi)

        assertEquals(35.3f, gi, 0.1f)
        assertEquals(2.47f, gamma.a, 0.01f)
        assertEquals(23.24f, gamma.b, 0.01f)
    }

    @Test
    fun step5_predicts240Points_peakAround5Point23() {
        val predictor = BloodSugarPredictor(calculator)

        val result = predictor.predict(
            bloodSugarStart = 5.0f,
            totalCarbIntake = 13.02f,
            giAvg = 35.3f,
            pointCount = 240,
        )

        assertEquals(240, result.points.size)
        assertTrue(result.peakMinute in 40..90)
        assertEquals(5.23f, result.peakBloodSugar, 0.03f)
    }

    @Test
    fun step6_calculatesInsulinBand_forRapidType_examples() {
        val bandCalculator = InsulinBandCalculator()

        val c50 = bandCalculator.calculate(InsulinType.RAPID, 50f)
        assertEquals(4.1667f, c50.baseIU, 0.001f)
        assertEquals(1f, c50.blue.minIU, 0.0001f)
        assertEquals(2.9167f, c50.blue.maxIU, 0.001f)
        assertEquals(5.4167f, c50.green.maxIU, 0.001f)
        assertEquals(20f, c50.red.maxIU, 0.0001f)

        val c150 = bandCalculator.calculate(InsulinType.RAPID, 150f)
        assertEquals(12.5f, c150.baseIU, 0.0001f)
        assertEquals(8.75f, c150.blue.maxIU, 0.0001f)
        assertEquals(16.25f, c150.green.maxIU, 0.0001f)
        assertEquals(20f, c150.red.maxIU, 0.0001f)
    }

    @Test
    fun step7_extractsFocusPeriods_andBuildsPrompt() {
        val analyzer = FocusPeriodAnalyzer()
        val points = listOf(
            BloodSugarPoint(0, 5.0f),
            BloodSugarPoint(60, 8.0f),
            BloodSugarPoint(90, 11.2f),
            BloodSugarPoint(120, 7.0f),
            BloodSugarPoint(180, 3.5f),
            BloodSugarPoint(210, 4.2f),
        )

        val periods = analyzer.analyze(points)

        assertEquals(3, periods.size)
        assertEquals(FocusLevel.HIGH, periods[0].level)
        assertEquals(11.2f, periods[0].bloodSugar, 0.0001f)
        assertEquals(FocusLevel.LOW, periods[1].level)
        assertEquals(3.5f, periods[1].bloodSugar, 0.0001f)
        assertEquals(FocusLevel.ELEVATED, periods[2].level)

        val prompt = analyzer.buildReminderPrompt(periods)
        assertTrue(prompt.contains("不是医疗建议"))
        assertTrue(prompt.contains("11.2mmol/L"))
        assertTrue(prompt.contains("3.5mmol/L"))
    }
}
