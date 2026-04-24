package com.example.diabetesassistant.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * USDA 查询请求体。
 */
data class UsdaSearchRequest(
    @SerializedName("query")
    val query: String,
    @SerializedName("pageSize")
    val pageSize: Int = 1,
)

/**
 * USDA 查询响应。
 */
data class UsdaSearchResponse(
    @SerializedName("foods")
    val foods: List<UsdaFood> = emptyList(),
)

/**
 * USDA 食物条目。
 */
data class UsdaFood(
    @SerializedName("foodNutrients")
    val foodNutrients: List<UsdaNutrient> = emptyList(),
)

/**
 * USDA 营养素条目。
 */
data class UsdaNutrient(
    @SerializedName("nutrientName")
    val nutrientName: String?,
    @SerializedName("value")
    val value: Float?,
)
