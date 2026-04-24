package com.example.diabetesassistant.data.remote

import com.example.diabetesassistant.data.remote.dto.UsdaSearchRequest
import com.example.diabetesassistant.data.remote.dto.UsdaSearchResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * USDA FoodData Central 接口。
 */
interface UsdaApi {
    /**
     * 查询原料基础碳水。
     */
    @POST("fdc/v1/foods/search")
    suspend fun searchFood(
        @Query("api_key") apiKey: String,
        @Body request: UsdaSearchRequest,
    ): UsdaSearchResponse
}
