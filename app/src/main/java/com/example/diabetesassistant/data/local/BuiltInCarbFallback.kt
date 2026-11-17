package com.example.diabetesassistant.data.local

/**
 * Common foods built-in carb table (g carbs per 100g ingredient).
 * Fallback when USDA API is unavailable.
 * Includes cooked staples, high-moisture starch gels, soups, and common dishes.
 * Sources: USDA FoodData Central, China Food Composition Table (2022).
 */
object BuiltInCarbFallback {
    private val table: Map<String, Float> = mapOf(
        // === 中文高水分天然与淀粉凝胶、小吃（绝大多数为水） ===
        "冰粉" to 1.5f, "手搓冰粉" to 1.5f, "红糖冰粉" to 7.0f, "白凉粉" to 2.5f,
        "爱玉" to 1.5f, "爱玉冻" to 1.5f, "烧仙草" to 3.0f, "仙草冻" to 3.0f, "黑凉粉" to 3.0f,
        "凉粉" to 9.2f, "川味凉粉" to 9.2f, "红糖凉粉" to 15.0f, "凉皮" to 11.5f, "陕西凉皮" to 11.5f,
        "魔芋" to 2.2f, "魔芋豆腐" to 2.2f, "魔芋丝" to 2.2f, "魔芋结" to 2.2f, "魔芋爽" to 4.5f,
        "米豆腐" to 12.0f, "仙草" to 3.0f, "龟苓膏" to 4.0f, "果冻" to 12.0f,
        "粉皮" to 12.0f, "拉皮" to 11.0f, "东北拉皮" to 11.0f,

        // === 中文汤品与泡发干货 ===
        "紫菜蛋花汤" to 1.5f, "紫菜汤" to 1.5f, "熟紫菜" to 5.3f, "紫菜" to 5.3f,
        "裙带菜" to 3.0f, "熟裙带菜" to 3.0f, "海带" to 9.6f, "熟海带" to 4.5f, "海带汤" to 2.0f,
        "木耳" to 3.5f, "黑木耳" to 3.5f, "熟木耳" to 3.5f, "凉拌木耳" to 4.5f,
        "银耳" to 2.8f, "熟银耳" to 2.8f, "银耳汤" to 3.5f,
        "虾皮" to 0.5f, "熟虾皮" to 0.5f,
        "西红柿蛋花汤" to 2.2f, "番茄蛋汤" to 2.2f, "青菜豆腐汤" to 1.2f,
        "冬瓜汤" to 1.5f, "排骨汤" to 0.5f, "鸡汤" to 0.2f, "清汤" to 0.0f,

        // === 中文熟食主食与面点 ===
        "熟米饭" to 28.6f, "米饭" to 28.6f, "白米饭" to 28.6f, "杂粮饭" to 23.5f, "糙米饭" to 24.0f,
        "熟面条" to 24.7f, "面条" to 24.7f, "炒饭" to 25.0f, "蛋炒饭" to 24.0f, "扬州炒饭" to 24.0f,
        "炒面" to 26.0f, "肉丝炒面" to 25.0f, "拌面" to 26.0f, "热干面" to 32.0f,
        "小笼包" to 22.0f, "馒头" to 47.0f, "包子" to 30.0f, "肉包" to 30.0f, "菜包" to 28.0f, "豆沙包" to 48.0f,
        "饺子" to 20.0f, "水饺" to 20.0f, "蒸饺" to 22.0f, "煎饺" to 24.0f, "饺子皮" to 27.0f,
        "馄饨" to 15.0f, "云吞" to 15.0f, "小馄饨" to 14.0f,
        "烧麦" to 28.0f, "烧卖" to 28.0f, "油条" to 51.0f,
        "煎饼果子" to 23.0f, "煎饼" to 38.0f, "手抓饼" to 38.0f, "葱油饼" to 40.0f,
        "花卷" to 42.0f, "窝窝头" to 38.0f, "生煎包" to 26.0f, "春卷" to 28.0f,
        "面包" to 52.0f, "全麦面包" to 43.0f, "米粉" to 25.0f, "肠粉" to 18.0f,
        "米线" to 23.0f, "熟米线" to 23.0f, "粉丝" to 14.0f, "熟粉丝" to 14.0f,
        "年糕" to 34.0f, "汤圆" to 33.0f, "元宵" to 35.0f,
        "燕麦" to 66.0f, "小米粥" to 8.4f, "白粥" to 9.8f,

        // === 中文熟食菜肴与肉类 ===
        "青椒炒肉" to 4.5f, "五花肉" to 0.0f, "猪肉" to 0f, "里脊肉" to 0f, "瘦肉" to 0f, "排骨" to 0f,
        "牛肉" to 0f, "牛腩" to 0f, "羊肉" to 0f, "鸡肉" to 0f, "鸡胸肉" to 0f, "鸡翅" to 0f, "鸡腿" to 0f, "鸭肉" to 0f,
        "鱼" to 0f, "鲈鱼" to 0f, "草鱼" to 0f, "三文鱼" to 0f, "虾" to 0.2f, "大虾" to 0.2f,
        "蟹" to 0f, "螃蟹" to 0f, "鸡蛋" to 1.1f, "鸭蛋" to 0.8f,

        // === 中文蔬菜与豆制品 ===
        "西红柿" to 3.9f, "番茄" to 3.9f, "土豆" to 17.5f, "胡萝卜" to 10.0f, "西兰花" to 6.6f, "青花菜" to 6.6f,
        "菠菜" to 3.6f, "白菜" to 5.8f, "大白菜" to 5.8f, "生菜" to 2.9f, "芹菜" to 3.3f, "黄瓜" to 2.9f,
        "茄子" to 5.9f, "青椒" to 5.8f, "彩椒" to 6.3f, "洋葱" to 9.3f, "大蒜" to 33.1f, "生姜" to 17.8f,
        "葱" to 7.0f, "蘑菇" to 3.3f, "香菇" to 5.2f, "金针菇" to 6.0f, "玉米" to 22.8f, "南瓜" to 5.3f,
        "苦瓜" to 4.6f, "冬瓜" to 2.6f, "花菜" to 4.5f, "豆角" to 7.1f, "豌豆" to 14.5f, "豆芽" to 5.9f,
        "豆腐" to 1.9f, "北豆腐" to 2.3f, "南豆腐" to 1.1f, "豆干" to 6.8f, "莲藕" to 16.4f, "竹笋" to 3.6f,
        "芋头" to 26.2f, "红薯" to 20.1f, "地瓜" to 20.1f, "山药" to 12.4f,

        // === 中文调料水果奶类 ===
        "白糖" to 100f, "红糖" to 98.0f, "冰糖" to 99.0f, "蜂蜜" to 82.4f,
        "酱油" to 7.3f, "老抽" to 9.0f, "生抽" to 7.3f, "蚝油" to 14.0f, "醋" to 4.9f, "料酒" to 3.0f,
        "淀粉" to 85.0f, "生粉" to 85.0f, "玉米淀粉" to 85.0f, "面粉" to 73.0f,
        "苹果" to 14.0f, "香蕉" to 22.8f, "橘子" to 11.8f, "葡萄" to 18.1f, "西瓜" to 6.2f, "草莓" to 7.7f,
        "牛奶" to 5.0f, "酸奶" to 7.0f, "无糖酸奶" to 3.5f, "芝士" to 1.3f, "黄油" to 0.1f,

        // === High-Moisture Gels (English) ===
        "bingfen" to 1.5f, "ice jelly" to 1.5f, "aiyu" to 1.5f, "aiyu jelly" to 1.5f,
        "starch jelly" to 9.2f, "liangfen" to 9.2f,
        "cold skin noodle" to 11.5f, "cold skin noodles" to 11.5f, "liangpi" to 11.5f,
        "konjac" to 2.2f, "konjac tofu" to 2.2f, "konjac jelly" to 2.2f, "konjac snack" to 4.5f, "shirataki" to 2.2f,
        "rice tofu" to 12.0f, "grass jelly" to 3.0f, "herbal jelly" to 3.0f, "guilinggao" to 4.0f, "jelly" to 12.0f,
        "fenpi" to 12.0f, "lapi" to 11.0f,

        // === Soups & Soaked Dried Goods (English) ===
        "seaweed egg drop soup" to 1.5f, "seaweed egg soup" to 1.5f, "seaweed soup" to 1.5f,
        "cooked seaweed" to 5.3f, "seaweed" to 5.3f, "wakame" to 3.0f, "cooked wakame" to 3.0f, "sea mustard" to 3.0f,
        "wood ear" to 3.5f, "black fungus" to 3.5f, "cooked wood ear" to 3.5f,
        "white fungus" to 2.8f, "snow fungus" to 2.8f, "silver ear fungus" to 2.8f,
        "dried shrimp" to 0.5f, "dried small shrimp" to 0.5f,
        "tomato egg soup" to 2.2f, "clear soup" to 0.0f, "broth" to 0.0f,

        // === Cooked Staples & Pastries (English) ===
        "cooked rice" to 28.6f, "rice" to 28.6f, "white rice" to 28.6f,
        "cooked noodle" to 24.7f, "cooked noodles" to 24.7f, "noodle" to 24.7f, "noodles" to 24.7f,
        "fried rice" to 25.0f, "egg fried rice" to 24.0f,
        "fried noodles" to 26.0f, "fried noodle" to 26.0f, "chow mein" to 26.0f, "mixed noodle" to 26.0f,
        "xiaolongbao" to 22.0f, "steamed bun" to 47.0f, "steamed stuffed bun" to 30.0f, "baozi" to 30.0f,
        "dumplings" to 20.0f, "dumpling" to 20.0f, "jiaozi" to 20.0f, "boiled dumpling" to 20.0f, "steamed dumpling" to 22.0f, "potsticker" to 24.0f,
        "wonton" to 15.0f, "wontons" to 15.0f, "shaomai" to 28.0f, "shumai" to 28.0f,
        "youtiao" to 51.0f, "steamed twist roll" to 42.0f, "huajuan" to 42.0f, "chinese pancake" to 38.0f, "jianbing" to 23.0f,
        "scallion pancake" to 40.0f, "rice noodle roll" to 18.0f, "glass noodle" to 14.0f, "glass noodles" to 14.0f, "vermicelli" to 14.0f,
        "rice cake" to 34.0f, "tangyuan" to 33.0f,
        "bread" to 52.0f, "whole wheat bread" to 43.0f, "dumpling wrapper" to 27.0f, "rice noodle" to 25.0f,
        "oats" to 66.0f, "millet porridge" to 8.4f, "porridge" to 9.8f, "congee" to 9.8f,

        // === Cooked Dishes / Meats (English) ===
        "green pepper pork" to 4.5f, "pork belly" to 0.0f, "pork" to 0f, "lean pork" to 0f, "ribs" to 0f,
        "beef" to 0f, "beef brisket" to 0f, "lamb" to 0f, "chicken" to 0f, "chicken breast" to 0f,
        "chicken wing" to 0f, "chicken leg" to 0f, "duck" to 0f, "fish" to 0f, "salmon" to 0f, "sea bass" to 0f,
        "shrimp" to 0.2f, "prawn" to 0.2f, "crab" to 0f,
        "egg" to 1.1f, "whole egg" to 1.1f, "fresh egg" to 1.1f, "boiled egg" to 1.1f, "fried egg" to 1.5f, "poached egg" to 1.2f, "duck egg" to 0.8f,

        // === Vegetables (English fallback) ===
        "tomato" to 3.9f, "potato" to 17.5f, "carrot" to 10.0f, "broccoli" to 6.6f,
        "spinach" to 3.6f, "cabbage" to 5.8f, "lettuce" to 2.9f, "celery" to 3.3f,
        "cucumber" to 2.9f, "eggplant" to 5.9f, "bell pepper" to 6.3f, "green pepper" to 5.8f,
        "onion" to 9.3f, "garlic" to 33.1f, "ginger" to 17.8f, "green onion" to 7.0f, "scallion" to 7.0f,
        "mushroom" to 3.3f, "shiitake" to 5.2f, "enoki" to 6.0f, "corn" to 22.8f, "pumpkin" to 5.3f,
        "bitter melon" to 4.6f, "winter melon" to 2.6f, "cauliflower" to 4.5f,
        "green beans" to 7.1f, "pea" to 14.5f, "bean sprouts" to 5.9f, "tofu" to 1.9f,
        "firm tofu" to 2.3f, "soft tofu" to 1.1f, "dried tofu" to 6.8f,
        "lotus root" to 16.4f, "bamboo shoot" to 3.6f, "taro" to 26.2f,
        "sweet potato" to 20.1f, "yam" to 12.4f, "kelp" to 9.6f,

        // === Seasonings (English fallback) ===
        "sugar" to 100f, "brown sugar" to 98.0f, "rock sugar" to 99.0f, "honey" to 82.4f,
        "soy sauce" to 7.3f, "dark soy sauce" to 9.0f, "oyster sauce" to 14.0f,
        "vinegar" to 4.9f, "cooking wine" to 3.0f, "ketchup" to 24.0f,
        "bean paste" to 12.0f, "chili sauce" to 10.0f, "sesame oil" to 0f,
        "peanut oil" to 0f, "corn starch" to 85.0f, "starch" to 85.0f, "flour" to 73.0f,

        // === Fruits (English fallback) ===
        "apple" to 14.0f, "banana" to 22.8f, "orange" to 11.8f, "grape" to 18.1f,
        "watermelon" to 6.2f, "strawberry" to 7.7f,

        // === Dairy / Other (English fallback) ===
        "milk" to 5.0f, "cheese" to 1.3f, "butter" to 0.1f, "yogurt" to 7.0f, "sugar free yogurt" to 3.5f,
    )

    fun lookup(ingredientName: String): Float? {
        val raw = ingredientName.trim().lowercase()
        if (raw.isEmpty()) return null

        // 1. Direct match with raw input
        table[raw]?.let { return it }

        // 2. Normalize punctuation / delimiters
        val normalized = raw
            .replace(Regex("[\\-_/(),.，。（）\\[\\]]"), " ")
            .trim()
        table[normalized]?.let { return it }

        val clean = normalized.replace("\\s+".toRegex(), "")
        if (clean.isEmpty()) return null
        table[clean]?.let { return it }

        // 3. Exact match ignoring spaces across dictionary keys
        for ((key, value) in table) {
            val keyClean = key.lowercase().replace("\\s+".toRegex(), "")
            if (keyClean == clean) {
                return value
            }
        }

        // 4. Substring match: dictionary key is contained within user input
        // Prefer the longest matching key to ensure maximum specificity (e.g. "紫菜蛋花汤" over "紫菜")
        val containedMatches = table.filter { (key, _) ->
            val keyClean = key.lowercase().replace("\\s+".toRegex(), "")
            keyClean.isNotEmpty() && (clean.contains(keyClean) || normalized.contains(key.lowercase()))
        }
        if (containedMatches.isNotEmpty()) {
            return containedMatches.maxByOrNull { it.key.length }?.value
        }

        // 5. Substring match: user input is contained within dictionary key
        // Only consider if user input has meaningful length (>= 2 chars) to avoid single-char ambiguity
        val containerMatches = table.filter { (key, _) ->
            val keyClean = key.lowercase().replace("\\s+".toRegex(), "")
            clean.length >= 2 && (keyClean.contains(clean) || key.lowercase().contains(normalized))
        }
        if (containerMatches.isNotEmpty()) {
            return containerMatches.minByOrNull { it.key.length }?.value
        }

        return null
    }

    fun contains(ingredientName: String): Boolean = lookup(ingredientName) != null

    val size: Int get() = table.size
}
