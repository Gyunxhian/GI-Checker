package com.example.diabetesassistant.data.model

/**
 * AI 菜品识别返回模型。
 */
data class DishRecognitionResult(
    val dishes: List<Dish>,
    val coinDetected: Boolean,
)

/**
 * 菜品模型。
 */
data class Dish(
    val dishName: String,
    val ingredients: List<Ingredient>,
)

/**
 * 原料模型。
 */
data class Ingredient(
    val name: String,
    val category: String,
    val weightG: Float,
    val cookingMethod: String,
    val giValue: Float,
)
