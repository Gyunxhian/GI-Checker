package com.example.diabetesassistant.domain.usecase

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [AnalyzeFoodUseCase.FOOD_ANALYSIS_PROMPT].
 * Verifies that the AI system prompt contains mandatory instructions for
 * rehydrated dried goods in soups (pre-soak dry weight equivalent) and
 * high-moisture starch gels (8-10% carbs, disallowing dry raw starch).
 */
class AnalyzeFoodPromptTest {

    @Test
    fun foodAnalysisPrompt_containsSoakedDriedGoodsInSoupGuidance() {
        val prompt = AnalyzeFoodUseCase.FOOD_ANALYSIS_PROMPT

        // Must mention soaked dried goods in soups
        assertTrue("Prompt must mention 紫菜", prompt.contains("紫菜"))
        assertTrue("Prompt must mention 裙带菜", prompt.contains("裙带菜"))
        assertTrue("Prompt must mention 木耳", prompt.contains("木耳"))
        assertTrue("Prompt must mention 虾皮", prompt.contains("虾皮"))

        // Must specify dry weight equivalent estimation
        assertTrue("Prompt must instruct pre-soak dry weight or rehydrated state",
            prompt.contains("干重") || prompt.contains("泡发前"))

        // Must specify realistic 2-5g dry weight for soup seaweed
        assertTrue("Prompt must mention 2-5g or 2~5g for seaweed in soup",
            prompt.contains("2-5g") || prompt.contains("2~5g"))

        // Must suggest cooked seaweed or seaweed name_en
        assertTrue("Prompt must recommend seaweed / cooked seaweed name_en",
            prompt.contains("seaweed"))
    }

    @Test
    fun foodAnalysisPrompt_containsHighMoistureStarchGelGuidance() {
        val prompt = AnalyzeFoodUseCase.FOOD_ANALYSIS_PROMPT

        // Must mention starch gel items
        assertTrue("Prompt must mention 凉粉", prompt.contains("凉粉"))
        assertTrue("Prompt must mention 凉皮", prompt.contains("凉皮"))
        assertTrue("Prompt must mention 魔芋", prompt.contains("魔芋"))
        assertTrue("Prompt must mention 冰粉", prompt.contains("冰粉"))

        // Must specify low density / ~8-10% carbs
        assertTrue("Prompt must mention 8-10% or 8%~10% carbs",
            prompt.contains("8-10%") || prompt.contains("8%~10%"))

        // Must explicitly warn against matching raw dry starch
        assertTrue("Prompt must warn against raw dry starch (corn starch / 生淀粉)",
            prompt.contains("生淀粉") || prompt.contains("干燥生淀粉") || prompt.contains("corn starch"))

        // Must recommend starch jelly / cold skin noodle / konjac name_en
        assertTrue("Prompt must recommend starch jelly / konjac name_en",
            prompt.contains("starch jelly") || prompt.contains("konjac"))
    }

    @Test
    fun foodAnalysisPrompt_specifiesJsonContractAndFields() {
        val prompt = AnalyzeFoodUseCase.FOOD_ANALYSIS_PROMPT

        assertTrue("Prompt must require dish_name", prompt.contains("dish_name"))
        assertTrue("Prompt must require ingredients", prompt.contains("ingredients"))
        assertTrue("Prompt must require name", prompt.contains("name"))
        assertTrue("Prompt must require name_en", prompt.contains("name_en"))
        assertTrue("Prompt must require category", prompt.contains("category"))
        assertTrue("Prompt must require weight_g", prompt.contains("weight_g"))
        assertTrue("Prompt must require cooking_method", prompt.contains("cooking_method"))
        assertTrue("Prompt must require gi_value", prompt.contains("gi_value"))
    }
}
