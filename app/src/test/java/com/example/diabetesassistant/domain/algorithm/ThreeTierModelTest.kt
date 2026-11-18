package com.example.diabetesassistant.domain.algorithm

import com.example.diabetesassistant.camera.AnalysisResultJson
import com.example.diabetesassistant.data.model.Dish
import com.example.diabetesassistant.data.model.DishRecognitionResult
import com.example.diabetesassistant.data.model.FoodCategory
import com.example.diabetesassistant.data.model.Ingredient
import com.example.diabetesassistant.ui.screens.extractCarbBreakdown
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 针对“211 控糖三元组分模型”（主食 Staple、蔬菜 Vegetable、荤菜 Meat）的全链路与边界单元测试。
 */
class ThreeTierModelTest {

    private val calculator = CarbAndGiCalculator()

    @Test
    fun analysisResultJson_backwardsCompatibility_defaultsGracefullyTo1() {
        // 场景 1：最原始老记录，没有任何 stapleIntake, vegIntake, meatIntake，默认 meatRatio=0.5, intakePercent=1.0
        val legacyRecord = AnalysisResultJson(
            totalCarbIntake = 45.0f,
            weightedGi = 65.0f,
            meatRatio = 0.5f,
            intakePercent = 1.0f,
            stapleIntake = null,
            vegIntake = null,
            meatIntake = null,
        )

        assertEquals(1.0f, legacyRecord.effectiveStapleIntake, 0.0001f)
        assertEquals(1.0f, legacyRecord.effectiveVegIntake, 0.0001f)
        assertEquals(1.0f, legacyRecord.effectiveMeatIntake, 0.0001f)
    }

    @Test
    fun analysisResultJson_backwardsCompatibility_calculatesEquivalentIntakeFromLegacyRatio() {
        // 场景 2：老记录设置了非对称荤素比：meatRatio=0.25 (偏素), intakePercent=0.8
        val legacyPartial = AnalysisResultJson(
            totalCarbIntake = 30.0f,
            meatRatio = 0.25f,
            intakePercent = 0.8f,
            stapleIntake = null,
            vegIntake = null,
            meatIntake = null,
        )

        // effectiveStaple = 0.8f
        // effectiveVeg = (2 * (1 - 0.25)).coerceIn(0, 1) * 0.8 = 1.0 * 0.8 = 0.8f
        // effectiveMeat = (2 * 0.25).coerceIn(0, 1) * 0.8 = 0.5 * 0.8 = 0.4f
        assertEquals(0.8f, legacyPartial.effectiveStapleIntake, 0.0001f)
        assertEquals(0.8f, legacyPartial.effectiveVegIntake, 0.0001f)
        assertEquals(0.4f, legacyPartial.effectiveMeatIntake, 0.0001f)
    }

    @Test
    fun analysisResultJson_explicitThreeTierFields_takesPrecedence() {
        // 场景 3：新模型下 explicit 显式输入
        val modernRecord = AnalysisResultJson(
            totalCarbIntake = 25.0f,
            meatRatio = 0.5f,
            intakePercent = 1.0f,
            stapleIntake = 0.3f,
            vegIntake = 1.0f,
            meatIntake = 0.6f,
        )

        assertEquals(0.3f, modernRecord.effectiveStapleIntake, 0.0001f)
        assertEquals(1.0f, modernRecord.effectiveVegIntake, 0.0001f)
        assertEquals(0.6f, modernRecord.effectiveMeatIntake, 0.0001f)
    }

    @Test
    fun extractCarbBreakdown_fromActualIngredients_allocatesToThreeTiers() {
        // 构造典型三元餐食：米饭 (主食 28g), 西兰花 (蔬菜 4g), 鸡胸肉 (荤菜 0g)
        val dish = Dish(
            dishName = "健身减脂餐",
            ingredients = listOf(
                Ingredient(name = "米饭", category = "主食", weightG = 100f, cookingMethod = "蒸", giValue = 83f),
                Ingredient(name = "西兰花", category = "素", weightG = 100f, cookingMethod = "水煮", giValue = 15f),
                Ingredient(name = "鸡胸肉", category = "荤", weightG = 100f, cookingMethod = "水煮", giValue = 0f),
            )
        )
        val carbMap = mapOf(
            "米饭" to 28.0f,
            "西兰花" to 4.0f,
            "鸡胸肉" to 0.0f,
        )

        val record = AnalysisResultJson(
            totalCarbIntake = 32.0f,
            stapleIntake = 1.0f,
            vegIntake = 1.0f,
            meatIntake = 1.0f,
            recognitionResult = DishRecognitionResult(dishes = listOf(dish), coinDetected = false),
            ingredientCarbsMap = carbMap,
        )

        val (stapleC, vegC, meatC) = extractCarbBreakdown(record)

        assertEquals(28.0f, stapleC, 0.1f)
        assertEquals(4.0f, vegC, 0.1f)
        assertEquals(0.0f, meatC, 0.1f)
        assertEquals(32.0f, stapleC + vegC + meatC, 0.1f)
    }

    @Test
    fun extractCarbBreakdown_fallbackWhenIngredientsMissing() {
        val noIngredientsRecord = AnalysisResultJson(
            totalCarbIntake = 100.0f,
            recognitionResult = null,
        )

        val (stapleC, vegC, meatC) = extractCarbBreakdown(noIngredientsRecord)

        assertEquals(85.0f, stapleC, 0.0001f)
        assertEquals(13.0f, vegC, 0.0001f)
        assertEquals(2.0f, meatC, 0.0001f)
        assertEquals(100.0f, stapleC + vegC + meatC, 0.0001f)
    }

    @Test
    fun strictMassConservation_linearAdditivityHoldsUnderArbitraryIntake() {
        // 数学公理验证：总摄入碳水在任意比例组合下严格等于主食碳水 + 蔬菜碳水 + 荤菜碳水
        val summary = MealCarbSummary(
            staple = CategoryCarbSummary(totalCarbsG = 65.5f, totalWeightG = 200f, carbsPerGram = 65.5f / 200f),
            vegetable = CategoryCarbSummary(totalCarbsG = 12.3f, totalWeightG = 300f, carbsPerGram = 12.3f / 300f),
            meat = CategoryCarbSummary(totalCarbsG = 3.2f, totalWeightG = 250f, carbsPerGram = 3.2f / 250f),
            totalWeightG = 750f,
            totalCarbsG = 81.0f,
        )

        val testCases = listOf(
            Triple(1.0f, 1.0f, 1.0f),
            Triple(0.0f, 1.0f, 1.0f),
            Triple(0.5f, 0.5f, 0.5f),
            Triple(0.2f, 0.8f, 0.0f),
            Triple(0.75f, 0.25f, 1.0f),
        )

        for ((s, v, m) in testCases) {
            val calculated = calculator.calculateTotalCarbIntake(summary, s, v, m)
            val expected = 65.5f * s + 12.3f * v + 3.2f * m
            assertEquals("Mass conservation failed for ($s, $v, $m)", expected, calculated, 0.0001f)
        }
    }

    @Test
    fun foodCategory_vegetableExclusions_doesNotClassifyAsMeat() {
        // 关键防御：植物/菌菇名中含有“鸡/牛/蟹/羊/鱼”等字的食材，绝不能误判为荤菜
        val vegFoods = listOf(
            "鸡毛菜", "鸡腿菇", "蟹味菇", "羊肚菌", "牛油果", "牛蒡", "鱼腥草", "鸡蛋果"
        )
        for (food in vegFoods) {
            val catFromExplicitVeg = FoodCategory.from("素", food)
            assertEquals("Explicit veg category for '$food' must be VEGETABLE", FoodCategory.VEGETABLE, catFromExplicitVeg)

            val catFromNullCategory = FoodCategory.from(null, food)
            assertEquals("Null category for '$food' with veg exclusion must be VEGETABLE", FoodCategory.VEGETABLE, catFromNullCategory)
        }
    }

    @Test
    fun foodCategory_expandedStaples_classifiedAsStaple() {
        // 关键扩展：粥、油条、粽子、酸辣粉、抓饭等中餐核心主食必须准确识别为 STAPLE
        val staples = listOf(
            "皮蛋瘦肉粥" to "荤",
            "小米粥" to "素",
            "油条" to "素",
            "粽子" to "素",
            "酸辣粉" to "素",
            "羊肉抓饭" to "荤",
            "鲜虾水饺" to "荤",
            "牛肉馅饼" to "荤",
        )
        for ((name, cat) in staples) {
            val result = FoodCategory.from(cat, name)
            assertEquals("Food '$name' (cat=$cat) must be classified as STAPLE", FoodCategory.STAPLE, result)
        }
    }

    @Test
    fun analysisResultJson_gsonDeserialization_legacyJsonWithoutIntakePercent() {
        val gson = Gson()

        // 场景 A：老数据库记录完全没有 intakePercent 字段
        val legacyJsonA = """{"totalCarbIntake":45.0,"weightedGi":50.0,"meatRatio":0.5}"""
        val parsedA = gson.fromJson(legacyJsonA, AnalysisResultJson::class.java)
        assertEquals(1.0f, parsedA.effectiveStapleIntake, 0.0001f)
        assertEquals(1.0f, parsedA.effectiveVegIntake, 0.0001f)
        assertEquals(1.0f, parsedA.effectiveMeatIntake, 0.0001f)

        // 场景 B：老数据库记录有 meatRatio=0.25 和 intakePercent=0.8
        val legacyJsonB = """{"totalCarbIntake":30.0,"meatRatio":0.25,"intakePercent":0.8}"""
        val parsedB = gson.fromJson(legacyJsonB, AnalysisResultJson::class.java)
        assertEquals(0.8f, parsedB.effectiveStapleIntake, 0.0001f)
        assertEquals(0.8f, parsedB.effectiveVegIntake, 0.0001f)
        assertEquals(0.4f, parsedB.effectiveMeatIntake, 0.0001f)

        // 场景 C：显式 0% 摄入量（用户主动选择不吃）
        val zeroJson = """{"totalCarbIntake":0.0,"meatRatio":0.5,"intakePercent":0.0}"""
        val parsedZero = gson.fromJson(zeroJson, AnalysisResultJson::class.java)
        assertEquals(0.0f, parsedZero.effectiveStapleIntake, 0.0001f)
        assertEquals(0.0f, parsedZero.effectiveVegIntake, 0.0001f)
        assertEquals(0.0f, parsedZero.effectiveMeatIntake, 0.0001f)
    }

    @Test
    fun extractCarbBreakdown_zeroTotalCarbs_returnsAllZeros() {
        val dish = Dish(
            dishName = "无碳水餐",
            ingredients = listOf(
                Ingredient(name = "米饭", category = "主食", weightG = 100f, cookingMethod = "蒸", giValue = 83f),
            )
        )
        val zeroRecord = AnalysisResultJson(
            totalCarbIntake = 0.0f,
            stapleIntake = 0.0f,
            vegIntake = 0.0f,
            meatIntake = 0.0f,
            recognitionResult = DishRecognitionResult(dishes = listOf(dish), coinDetected = false),
        )
        val (s, v, m) = extractCarbBreakdown(zeroRecord)
        assertEquals(0.0f, s, 0.0001f)
        assertEquals(0.0f, v, 0.0001f)
        assertEquals(0.0f, m, 0.0001f)
    }

    @Test
    fun calculateTotalCarbIntake_nanAndNegativeProtection() {
        val summary = MealCarbSummary(
            staple = CategoryCarbSummary(totalCarbsG = -10f, totalWeightG = 100f, carbsPerGram = 0f),
            vegetable = CategoryCarbSummary(totalCarbsG = 20f, totalWeightG = 100f, carbsPerGram = 0.2f),
            meat = CategoryCarbSummary(totalCarbsG = 0f, totalWeightG = 100f, carbsPerGram = 0f),
        )
        val intake = calculator.calculateTotalCarbIntake(summary, stapleIntake = -0.5f, vegIntake = 1.0f, meatIntake = 1.0f)
        assertEquals(20.0f, intake, 0.0001f)
    }

    @Test
    fun calculateWeightedGi_boundsAndNaNProtection() {
        // 当加权计算出现边界情况时，GI 严格限制在 [0, 100]
        val details = listOf(
            IngredientCarbDetail("特殊食材", "主食", 100f, "生食", 150f, 50f, 0f, 50f, 50f),
        )
        val gi = calculator.calculateWeightedGi(details, stapleIntake = 1.0f, vegIntake = 1.0f, meatIntake = 1.0f)
        assertEquals(100f, gi, 0.0001f)
    }
}
