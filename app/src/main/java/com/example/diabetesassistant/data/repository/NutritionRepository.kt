package com.example.diabetesassistant.data.repository

import com.example.diabetesassistant.data.local.BuiltInCarbFallback
import com.example.diabetesassistant.data.local.dao.UsdaCacheDao
import com.example.diabetesassistant.data.local.entity.UsdaCache
import com.example.diabetesassistant.data.remote.ApiKeyProvider
import com.example.diabetesassistant.data.remote.NetworkResult
import com.example.diabetesassistant.data.remote.UsdaApi
import com.example.diabetesassistant.data.remote.dto.UsdaSearchRequest
import android.util.Log
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.HttpException

private const val TAG = "Diabetes|NutritionRepo"

interface NutritionRepository {
    suspend fun getCarbsPer100g(ingredientName: String): NetworkResult<Float>
}

@Singleton
class NutritionRepositoryImpl @Inject constructor(
    private val usdaApi: UsdaApi,
    private val usdaCacheDao: UsdaCacheDao,
    private val apiKeyProvider: ApiKeyProvider,
) : NutritionRepository {

    override suspend fun getCarbsPer100g(ingredientName: String): NetworkResult<Float> {
        val normalizedName = ingredientName.trim().lowercase()
        Log.d(TAG, "查询碳水: '$ingredientName' → 标准化: '$normalizedName'")

        usdaCacheDao.getByIngredientName(normalizedName)?.let { cached ->
            Log.d(TAG, "缓存命中 '$normalizedName': ${cached.carbsPer100g}g/100g")
            return NetworkResult.Success(cached.carbsPer100g)
        }

        Log.d(TAG, "缓存未命中, 请求USDA API: '$normalizedName'")
        val apiResult = try {
            val response = usdaApi.searchFood(
                apiKey = apiKeyProvider.usdaApiKey(),
                request = UsdaSearchRequest(query = normalizedName),
            )
            val food = response.foods.firstOrNull()
            Log.d(TAG, "USDA返回: ${response.foods.size} 个匹配食物")

            val carbValue = food
                ?.foodNutrients
                ?.firstOrNull { nutrient ->
                    val name = nutrient.nutrientName ?: return@firstOrNull false
                    name.contains("Carbohydrate", ignoreCase = true)
                }
                ?.value
            if (carbValue != null) {
                Log.d(TAG, "USDA碳水数据 '$normalizedName': ${carbValue}g/100g")
            }
            carbValue
        } catch (e: HttpException) {
            Log.e(TAG, "USDA HTTP错误: ${e.code()} ${e.message()}", e)
            null
        } catch (e: IOException) {
            Log.e(TAG, "USDA 网络错误: ${e.message}", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "USDA 未知错误: ${e.message}", e)
            null
        }

        if (apiResult != null) {
            usdaCacheDao.upsert(
                UsdaCache(ingredientName = normalizedName, carbsPer100g = apiResult),
            )
            Log.d(TAG, "USDA结果已缓存: '$normalizedName' = ${apiResult}g/100g")
            return NetworkResult.Success(apiResult)
        }

        val fallback = BuiltInCarbFallback.lookup(normalizedName)
        if (fallback != null) {
            usdaCacheDao.upsert(
                UsdaCache(ingredientName = normalizedName, carbsPer100g = fallback),
            )
            Log.d(TAG, "USDA失败, 使用内置碳水表: '$normalizedName' = ${fallback}g/100g")
            return NetworkResult.Success(fallback)
        }

        Log.e(TAG, "USDA查询失败且无内置数据: '$ingredientName'")
        return NetworkResult.Error(message = "Carbohydrate data not found for: $ingredientName")
    }

}