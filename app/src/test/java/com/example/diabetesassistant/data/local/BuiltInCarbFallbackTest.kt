package com.example.diabetesassistant.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [BuiltInCarbFallback].
 * Validates carbohydrate values for high-moisture starch gels, soups & rehydrated goods,
 * cooked staples, aliases, case/whitespace trimming, compound dish substrings,
 * and specificity prioritization.
 */
class BuiltInCarbFallbackTest {

    @Test
    fun lookup_highMoistureGels_returnsExpectedCarbValues() {
        assertEquals(9.2f, BuiltInCarbFallback.lookup("凉粉")!!, 0.001f)
        assertEquals(11.5f, BuiltInCarbFallback.lookup("凉皮")!!, 0.001f)
        assertEquals(2.2f, BuiltInCarbFallback.lookup("魔芋")!!, 0.001f)
        assertEquals(2.2f, BuiltInCarbFallback.lookup("魔芋豆腐")!!, 0.001f)
        assertEquals(2.5f, BuiltInCarbFallback.lookup("冰粉")!!, 0.001f)
        assertEquals(12.0f, BuiltInCarbFallback.lookup("米豆腐")!!, 0.001f)
    }

    @Test
    fun lookup_soupsAndRehydratedIngredients_returnsExpectedCarbValues() {
        assertEquals(1.5f, BuiltInCarbFallback.lookup("紫菜蛋花汤")!!, 0.001f)
        assertEquals(1.5f, BuiltInCarbFallback.lookup("紫菜汤")!!, 0.001f)
        assertEquals(5.3f, BuiltInCarbFallback.lookup("熟紫菜")!!, 0.001f)
        assertEquals(5.3f, BuiltInCarbFallback.lookup("紫菜")!!, 0.001f)
        assertEquals(3.0f, BuiltInCarbFallback.lookup("裙带菜")!!, 0.001f)
        assertEquals(3.5f, BuiltInCarbFallback.lookup("木耳")!!, 0.001f)
        assertEquals(3.5f, BuiltInCarbFallback.lookup("熟木耳")!!, 0.001f)
        assertEquals(3.5f, BuiltInCarbFallback.lookup("黑木耳")!!, 0.001f)
        assertEquals(0.5f, BuiltInCarbFallback.lookup("虾皮")!!, 0.001f)
    }

    @Test
    fun lookup_cookedStaplesAndCommonDishes_returnsExpectedCarbValues() {
        assertEquals(28.6f, BuiltInCarbFallback.lookup("熟米饭")!!, 0.001f)
        assertEquals(28.6f, BuiltInCarbFallback.lookup("米饭")!!, 0.001f)
        assertEquals(24.7f, BuiltInCarbFallback.lookup("熟面条")!!, 0.001f)
        assertEquals(24.7f, BuiltInCarbFallback.lookup("面条")!!, 0.001f)
        assertEquals(25.0f, BuiltInCarbFallback.lookup("炒饭")!!, 0.001f)
        assertEquals(26.0f, BuiltInCarbFallback.lookup("炒面")!!, 0.001f)
        assertEquals(22.0f, BuiltInCarbFallback.lookup("小笼包")!!, 0.001f)
        assertEquals(47.0f, BuiltInCarbFallback.lookup("馒头")!!, 0.001f)
        assertEquals(30.0f, BuiltInCarbFallback.lookup("包子")!!, 0.001f)
        assertEquals(20.0f, BuiltInCarbFallback.lookup("饺子")!!, 0.001f)
        assertEquals(20.0f, BuiltInCarbFallback.lookup("水饺")!!, 0.001f)
    }

    @Test
    fun lookup_englishAliases_resolvesCorrectValues() {
        assertEquals(9.2f, BuiltInCarbFallback.lookup("starch jelly")!!, 0.001f)
        assertEquals(9.2f, BuiltInCarbFallback.lookup("liangfen")!!, 0.001f)
        assertEquals(11.5f, BuiltInCarbFallback.lookup("cold skin noodle")!!, 0.001f)
        assertEquals(11.5f, BuiltInCarbFallback.lookup("liangpi")!!, 0.001f)
        assertEquals(2.2f, BuiltInCarbFallback.lookup("konjac")!!, 0.001f)
        assertEquals(2.5f, BuiltInCarbFallback.lookup("ice jelly")!!, 0.001f)
        assertEquals(2.5f, BuiltInCarbFallback.lookup("bingfen")!!, 0.001f)
        assertEquals(1.5f, BuiltInCarbFallback.lookup("seaweed egg drop soup")!!, 0.001f)
        assertEquals(1.5f, BuiltInCarbFallback.lookup("seaweed soup")!!, 0.001f)
        assertEquals(5.3f, BuiltInCarbFallback.lookup("cooked seaweed")!!, 0.001f)
        assertEquals(3.0f, BuiltInCarbFallback.lookup("wakame")!!, 0.001f)
        assertEquals(3.5f, BuiltInCarbFallback.lookup("wood ear")!!, 0.001f)
        assertEquals(0.5f, BuiltInCarbFallback.lookup("dried shrimp")!!, 0.001f)
        assertEquals(28.6f, BuiltInCarbFallback.lookup("cooked rice")!!, 0.001f)
        assertEquals(24.7f, BuiltInCarbFallback.lookup("cooked noodles")!!, 0.001f)
        assertEquals(25.0f, BuiltInCarbFallback.lookup("fried rice")!!, 0.001f)
        assertEquals(26.0f, BuiltInCarbFallback.lookup("chow mein")!!, 0.001f)
        assertEquals(22.0f, BuiltInCarbFallback.lookup("xiaolongbao")!!, 0.001f)
        assertEquals(47.0f, BuiltInCarbFallback.lookup("steamed bun")!!, 0.001f)
        assertEquals(30.0f, BuiltInCarbFallback.lookup("baozi")!!, 0.001f)
        assertEquals(20.0f, BuiltInCarbFallback.lookup("dumplings")!!, 0.001f)
    }

    @Test
    fun lookup_trimsWhitespaceAndIgnoresCase() {
        assertEquals(9.2f, BuiltInCarbFallback.lookup("   凉粉   ")!!, 0.001f)
        assertEquals(28.6f, BuiltInCarbFallback.lookup("  COOKED RICE  ")!!, 0.001f)
        assertEquals(2.2f, BuiltInCarbFallback.lookup("  KonJAc  ")!!, 0.001f)
    }

    @Test
    fun lookup_handlesPunctuationAndHyphensGracefully() {
        assertEquals(9.2f, BuiltInCarbFallback.lookup("川味凉粉(微辣)")!!, 0.001f)
        assertEquals(2.2f, BuiltInCarbFallback.lookup("魔芋（魔芋豆腐）")!!, 0.001f)
        assertEquals(9.2f, BuiltInCarbFallback.lookup("starch-jelly")!!, 0.001f)
        assertEquals(28.6f, BuiltInCarbFallback.lookup("cooked_rice")!!, 0.001f)
        assertEquals(1.5f, BuiltInCarbFallback.lookup("seaweed/egg/soup")!!, 0.001f)
    }

    @Test
    fun lookup_prioritizesMoreSpecificLongestKey() {
        // "紫菜蛋花汤" (length 5, 1.5f) should take precedence over "紫菜" (length 2, 5.3f)
        assertEquals(1.5f, BuiltInCarbFallback.lookup("紫菜蛋花汤")!!, 0.001f)
        assertEquals(1.5f, BuiltInCarbFallback.lookup("美味紫菜蛋花汤")!!, 0.001f)

        // "熟紫菜" (length 3, 5.3f) vs "紫菜" (length 2, 5.3f)
        assertEquals(5.3f, BuiltInCarbFallback.lookup("熟紫菜")!!, 0.001f)

        // "紫菜汤" (length 3, 1.5f) vs "紫菜" (length 2, 5.3f)
        assertEquals(1.5f, BuiltInCarbFallback.lookup("一碗紫菜汤")!!, 0.001f)
    }

    @Test
    fun lookup_compoundDishNames_matchesSubstrings() {
        assertEquals(9.2f, BuiltInCarbFallback.lookup("川味凉粉")!!, 0.001f)
        assertEquals(11.5f, BuiltInCarbFallback.lookup("陕西麻酱凉皮")!!, 0.001f)
        assertEquals(3.5f, BuiltInCarbFallback.lookup("香菇炒黑木耳")!!, 0.001f)
        assertEquals(28.6f, BuiltInCarbFallback.lookup("一碗熟米饭")!!, 0.001f)
    }

    @Test
    fun lookup_blankAndUnknown_returnsNull() {
        assertNull(BuiltInCarbFallback.lookup(""))
        assertNull(BuiltInCarbFallback.lookup("   "))
        assertNull(BuiltInCarbFallback.lookup("completely_unknown_ingredient_xyz_12345"))
    }

    @Test
    fun contains_returnsTrueForKnownAndFalseForUnknown() {
        assertTrue(BuiltInCarbFallback.contains("凉粉"))
        assertTrue(BuiltInCarbFallback.contains("紫菜蛋花汤"))
        assertTrue(BuiltInCarbFallback.contains("cooked rice"))
        assertFalse(BuiltInCarbFallback.contains("non_existent_dish_xyz"))
    }

    @Test
    fun tableSize_hasSubstantiallyExpanded() {
        // Expanded table should have well over 100 entries (originally 78)
        assertTrue(
            "BuiltInCarbFallback table size (${BuiltInCarbFallback.size}) must be >= 120",
            BuiltInCarbFallback.size >= 120
        )
    }
}
