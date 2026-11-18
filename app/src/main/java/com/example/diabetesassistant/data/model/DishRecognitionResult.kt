package com.example.diabetesassistant.data.model

import com.google.gson.annotations.SerializedName

/**
 * AI 菜品识别返回模型。
 */
data class DishRecognitionResult(
    @SerializedName("dishes")
    val dishes: List<Dish>,
    @SerializedName("coin_detected")
    val coinDetected: Boolean,
)

/**
 * 菜品模型。
 */
data class Dish(
    @SerializedName("dish_name")
    val dishName: String,
    @SerializedName("ingredients")
    val ingredients: List<Ingredient>,
)

/**
 * 食物三元组分分类（211 控糖模型）。
 * STAPLE: 主食 (高碳水/高GI)
 * VEGETABLE: 素/蔬菜 (高纤维/低碳水/低GI)
 * MEAT: 荤/蛋白质 (零/极低碳水)
 */
enum class FoodCategory(val label: String) {
    STAPLE("主食"),
    VEGETABLE("素"),
    MEAT("荤");

    companion object {
        private val STAPLE_KEYWORDS = listOf(
            "米饭", "大米", "糙米", "糯米", "杂粮饭", "炒饭", "盖浇饭", "拌饭", "煲仔饭", "稀饭", "粥",
            "面条", "拉面", "意面", "炒面", "拌面", "冷面", "汤面", "热干面", "泡面", "刀削面", "挂面", "乌冬面", "方便面",
            "水饺", "饺子", "煎饺", "蒸饺", "锅贴", "馄饨", "云吞", "抄手",
            "包子", "生煎包", "小笼包", "灌汤包", "馒头", "花卷", "烧麦", "烧卖", "窝头", "窝窝头",
            "饼", "披萨", "面包", "吐司", "凉皮", "凉粉",
            "土豆", "马铃薯", "红薯", "地瓜", "紫薯", "芋头", "山药", "玉米",
            "年糕", "麦片", "燕麦", "米粉", "米线", "河粉", "粉丝", "粉条", "通心粉",
            "油条", "麻花", "油饼", "粽子", "粽", "汤圆", "元宵", "宽粉", "酸辣粉", "螺蛳粉", "抓饭", "炒米", "莜麦", "荞麦", "青稞"
        )

        private val MEAT_KEYWORDS = listOf(
            "肉", "鸡", "鸭", "鹅", "猪", "牛", "羊", "鱼", "虾", "蟹", "海鲜",
            "蛋", "培根", "香肠", "火腿", "排骨", "翅", "爪", "蹄", "肚", "肠", "肝", "贝", "蛤", "蚝"
        )

        private val VEGETABLE_EXCLUSIONS = listOf(
            "鸡毛菜", "鸡腿菇", "蟹味菇", "羊肚菌", "牛油果", "牛蒡", "鱼腥草", "鸡蛋果", "鸡冠花", "鸭掌木", "猪笼草"
        )

        fun from(category: String?, name: String? = null): FoodCategory {
            val cat = category?.trim()?.lowercase() ?: ""
            val n = name?.trim()?.lowercase() ?: ""

            // 1. Explicit category or keyword match for staple
            if (cat == "主食" || cat == "staple" || cat.contains("主食")) {
                return STAPLE
            }
            if (n.isNotEmpty() && STAPLE_KEYWORDS.any { n.contains(it) }) {
                return STAPLE
            }

            // 2. Explicit vegetable category (素) must not be misclassified as meat
            val isExplicitVeg = cat == "素" || cat == "veg" || cat == "vegetable" || cat.contains("素")
            if (isExplicitVeg) {
                return VEGETABLE
            }

            // 3. Explicit category or keyword match for meat/protein (excluding false-positive vegetable names)
            if (cat == "荤" || cat == "meat" || cat == "protein" || cat.contains("荤")) {
                return MEAT
            }
            val isVegetarianName = VEGETABLE_EXCLUSIONS.any { n.contains(it) }
            if (!isVegetarianName && n.isNotEmpty() && MEAT_KEYWORDS.any { n.contains(it) }) {
                return MEAT
            }

            // 4. Vegetable fallback
            return VEGETABLE
        }
    }
}

/**
 * 原料模型。
 */
data class Ingredient(
    @SerializedName("name")
    val name: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("weight_g")
    val weightG: Float,
    @SerializedName("cooking_method")
    val cookingMethod: String,
    @SerializedName("gi_value")
    val giValue: Float,
    @SerializedName("name_en")
    val nameEn: String? = null,
) {
    val foodCategory: FoodCategory
        get() = FoodCategory.from(category, name)
}
