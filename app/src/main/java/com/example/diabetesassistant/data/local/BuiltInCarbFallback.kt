package com.example.diabetesassistant.data.local

/**
 * 78 common foods built-in carb table (g carbs per 100g raw ingredient).
 * Fallback when USDA API is unavailable.
 * Sources: USDA FoodData Central, China Food Composition Table.
 */
object BuiltInCarbFallback {
    private val table: Map<String, Float> = mapOf(
        // === 中文蔬菜类 ===
        "西红柿" to 3.9f, "番茄" to 3.9f, "土豆" to 17.5f, "胡萝卜" to 10.0f, "西兰花" to 6.6f, "青花菜" to 6.6f,
        "菠菜" to 3.6f, "白菜" to 5.8f, "大白菜" to 5.8f, "生菜" to 2.9f, "芹菜" to 3.3f, "黄瓜" to 2.9f,
        "茄子" to 5.9f, "青椒" to 5.8f, "彩椒" to 6.3f, "洋葱" to 9.3f, "大蒜" to 33.1f, "生姜" to 17.8f,
        "葱" to 7.0f, "蘑菇" to 3.3f, "香菇" to 5.2f, "金针菇" to 6.0f, "玉米" to 22.8f, "南瓜" to 5.3f,
        "苦瓜" to 4.6f, "冬瓜" to 2.6f, "花菜" to 4.5f, "豆角" to 7.1f, "豌豆" to 14.5f, "豆芽" to 5.9f,
        "豆腐" to 1.9f, "北豆腐" to 2.3f, "南豆腐" to 1.1f, "豆干" to 6.8f, "莲藕" to 16.4f, "竹笋" to 3.6f,
        "芋头" to 26.2f, "红薯" to 20.1f, "地瓜" to 20.1f, "山药" to 12.4f, "海带" to 9.6f, "紫菜" to 5.3f,

        // === 中文肉禽蛋类 ===
        "猪肉" to 0f, "五花肉" to 0f, "里脊肉" to 0f, "瘦肉" to 0f, "排骨" to 0f, "牛肉" to 0f, "牛腩" to 0f,
        "羊肉" to 0f, "鸡肉" to 0f, "鸡胸肉" to 0f, "鸡翅" to 0f, "鸡腿" to 0f, "鸭肉" to 0f,
        "鱼" to 0f, "鲈鱼" to 0f, "草鱼" to 0f, "三文鱼" to 0f, "虾" to 0.2f, "大虾" to 0.2f,
        "蟹" to 0f, "螃蟹" to 0f, "鸡蛋" to 1.1f, "鸭蛋" to 0.8f,

        // === 中文主食谷物 ===
        "米饭" to 28.6f, "白米饭" to 28.6f, "杂粮饭" to 23.5f, "糙米饭" to 24.0f, "面条" to 24.7f,
        "面包" to 52.0f, "全麦面包" to 43.0f, "馒头" to 44.0f, "饺子皮" to 27.0f, "米粉" to 25.0f,
        "燕麦" to 66.0f, "小米粥" to 8.4f, "白粥" to 9.8f,

        // === 中文调料水果奶类 ===
        "白糖" to 100f, "红糖" to 98.0f, "蜂蜜" to 82.4f, "酱油" to 7.3f, "蚝油" to 14.0f, "淀粉" to 85.0f,
        "苹果" to 14.0f, "香蕉" to 22.8f, "橘子" to 11.8f, "葡萄" to 18.1f, "西瓜" to 6.2f,
        "牛奶" to 5.0f, "酸奶" to 7.0f, "无糖酸奶" to 3.5f,

        // === Vegetables (English fallback) ===
        "tomato" to 3.9f, "potato" to 17.5f, "carrot" to 10.0f, "broccoli" to 6.6f,
        "spinach" to 3.6f, "cabbage" to 5.8f, "lettuce" to 2.9f, "celery" to 3.3f,
        "cucumber" to 2.9f, "eggplant" to 5.9f, "bell pepper" to 6.3f, "green pepper" to 5.8f,
        "onion" to 9.3f, "garlic" to 33.1f, "ginger" to 17.8f, "green onion" to 7.0f,
        "mushroom" to 3.3f, "shiitake" to 5.2f, "corn" to 22.8f, "pumpkin" to 5.3f,
        "bitter melon" to 4.6f, "winter melon" to 2.6f, "cauliflower" to 4.5f,
        "green beans" to 7.1f, "pea" to 14.5f, "bean sprouts" to 5.9f, "tofu" to 1.9f,
        "firm tofu" to 2.3f, "soft tofu" to 1.1f, "dried tofu" to 6.8f,
        "lotus root" to 16.4f, "bamboo shoot" to 3.6f, "taro" to 26.2f,
        "sweet potato" to 20.1f, "yam" to 12.4f, "kelp" to 9.6f, "seaweed" to 5.3f,

        // === Meat / Protein ===
        "pork" to 0f, "pork belly" to 0f, "lean pork" to 0f, "ribs" to 0f,
        "beef" to 0f, "lamb" to 0f, "chicken" to 0f, "chicken breast" to 0f,
        "chicken wing" to 0f, "duck" to 0f, "fish" to 0f, "salmon" to 0f,
        "shrimp" to 0.2f, "crab" to 0f, "egg" to 1.1f, "duck egg" to 0.8f,

        // === Staples / Grains ===
        "rice" to 28.6f, "noodle" to 24.7f, "bread" to 52.0f, "steamed bun" to 44.0f,
        "dumpling wrapper" to 27.0f, "rice noodle" to 25.0f,

        // === Seasonings ===
        "sugar" to 100f, "brown sugar" to 98.0f, "rock sugar" to 99.0f, "honey" to 82.4f,
        "soy sauce" to 7.3f, "dark soy sauce" to 9.0f, "oyster sauce" to 14.0f,
        "vinegar" to 4.9f, "cooking wine" to 3.0f, "ketchup" to 24.0f,
        "bean paste" to 12.0f, "chili sauce" to 10.0f, "sesame oil" to 0f,
        "peanut oil" to 0f, "corn starch" to 85.0f, "flour" to 73.0f,

        // === Fruits ===
        "apple" to 14.0f, "banana" to 22.8f, "orange" to 11.8f, "grape" to 18.1f,
        "watermelon" to 6.2f, "strawberry" to 7.7f,

        // === Dairy / Other ===
        "milk" to 5.0f, "cheese" to 1.3f, "butter" to 0.1f, "yogurt" to 7.0f,
    )

    fun lookup(ingredientName: String): Float? {
        val normalized = ingredientName.trim().lowercase()
        return table[normalized]
            ?: table.entries.firstOrNull { (key, _) ->
                normalized.contains(key.replace(" ", ""), ignoreCase = true) ||
                    key.replace(" ", "").contains(normalized, ignoreCase = true)
            }?.value
    }

    fun contains(ingredientName: String): Boolean = lookup(ingredientName) != null

    val size: Int get() = table.size
}
