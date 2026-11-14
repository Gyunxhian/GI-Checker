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
)
