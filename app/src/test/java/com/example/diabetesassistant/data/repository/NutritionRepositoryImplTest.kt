package com.example.diabetesassistant.data.repository

import com.example.diabetesassistant.data.local.dao.UsdaCacheDao
import com.example.diabetesassistant.data.local.entity.UsdaCache
import com.example.diabetesassistant.data.remote.ApiKeyProvider
import com.example.diabetesassistant.data.remote.NetworkResult
import com.example.diabetesassistant.data.remote.UsdaApi
import com.example.diabetesassistant.data.remote.dto.UsdaFood
import com.example.diabetesassistant.data.remote.dto.UsdaNutrient
import com.example.diabetesassistant.data.remote.dto.UsdaSearchRequest
import com.example.diabetesassistant.data.remote.dto.UsdaSearchResponse
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NutritionRepositoryImplTest {

    private class FakeUsdaApi : UsdaApi {
        var callCount = 0
        var returnFoods: List<UsdaFood> = emptyList()

        override suspend fun searchFood(apiKey: String, request: UsdaSearchRequest): UsdaSearchResponse {
            callCount++
            return UsdaSearchResponse(foods = returnFoods)
        }
    }

    private class FakeUsdaCacheDao : UsdaCacheDao {
        val cache = mutableMapOf<String, UsdaCache>()

        override suspend fun upsert(item: UsdaCache): Long {
            cache[item.ingredientName] = item
            return 1L
        }

        override suspend fun getByIngredientName(ingredientName: String): UsdaCache? {
            return cache[ingredientName]
        }
    }

    @Test
    fun getCarbsPer100g_bingfen_returns1point5WithoutCallingUsda() = runBlocking {
        val api = FakeUsdaApi()
        val dao = FakeUsdaCacheDao()
        val repo = NutritionRepositoryImpl(api, dao, ApiKeyProvider())

        val result = repo.getCarbsPer100g(name = "冰粉", nameEn = "bingfen")
        assertTrue("Expected Success", result is NetworkResult.Success)
        val carbs = (result as NetworkResult.Success).data
        assertEquals(1.5f, carbs, 0.001f)
        assertEquals("USDA API should not be called for known BuiltIn foods", 0, api.callCount)
    }

    @Test
    fun getCarbsPer100g_egg_returns1point1WithoutCallingUsda() = runBlocking {
        val api = FakeUsdaApi()
        val dao = FakeUsdaCacheDao()
        val repo = NutritionRepositoryImpl(api, dao, ApiKeyProvider())

        val result = repo.getCarbsPer100g(name = "鸡蛋", nameEn = "egg")
        assertTrue("Expected Success", result is NetworkResult.Success)
        val carbs = (result as NetworkResult.Success).data
        assertEquals(1.1f, carbs, 0.001f)
        assertEquals("USDA API should not be called for egg", 0, api.callCount)
    }

    @Test
    fun getCarbsPer100g_liangfen_returns9point2WithoutCallingUsda() = runBlocking {
        val api = FakeUsdaApi()
        val dao = FakeUsdaCacheDao()
        val repo = NutritionRepositoryImpl(api, dao, ApiKeyProvider())

        val result = repo.getCarbsPer100g(name = "凉粉", nameEn = "starch jelly")
        assertTrue("Expected Success", result is NetworkResult.Success)
        val carbs = (result as NetworkResult.Success).data
        assertEquals(9.2f, carbs, 0.001f)
        assertEquals(0, api.callCount)
    }

    @Test
    fun getCarbsPer100g_seaweed_returns5point3WithoutCallingUsda() = runBlocking {
        val api = FakeUsdaApi()
        val dao = FakeUsdaCacheDao()
        val repo = NutritionRepositoryImpl(api, dao, ApiKeyProvider())

        val result = repo.getCarbsPer100g(name = "紫菜", nameEn = "seaweed")
        assertTrue("Expected Success", result is NetworkResult.Success)
        val carbs = (result as NetworkResult.Success).data
        assertEquals(5.3f, carbs, 0.001f)
        assertEquals(0, api.callCount)
    }

    @Test
    fun getCarbsPer100g_sanitizesPoisonedUsdaCache_forEgg() = runBlocking {
        val api = FakeUsdaApi()
        val dao = FakeUsdaCacheDao()
        // Simulate poisoned 57.7g in cache for an egg product
        dao.upsert(UsdaCache(ingredientName = "special egg product", carbsPer100g = 57.7f))
        val repo = NutritionRepositoryImpl(api, dao, ApiKeyProvider())

        val result = repo.getCarbsPer100g(name = "某种纯蛋", nameEn = "special egg product")
        assertTrue("Expected Success", result is NetworkResult.Success)
        val carbs = (result as NetworkResult.Success).data
        // Because it contains '蛋', raw 57.7g must be sanitized to 1.1g
        assertEquals(1.1f, carbs, 0.001f)
    }

    @Test
    fun getCarbsPer100g_sanitizesPoisonedUsdaApiResult_forJellyStarch() = runBlocking {
        val api = FakeUsdaApi().apply {
            returnFoods = listOf(
                UsdaFood(
                    foodNutrients = listOf(
                        UsdaNutrient(nutrientName = "Carbohydrate, by difference", value = 98.9f)
                    )
                )
            )
        }
        val dao = FakeUsdaCacheDao()
        val repo = NutritionRepositoryImpl(api, dao, ApiKeyProvider())

        val result = repo.getCarbsPer100g(name = "自制白冰粉", nameEn = "homemade ice jelly")
        assertTrue("Expected Success", result is NetworkResult.Success)
        val carbs = (result as NetworkResult.Success).data
        // Because it contains '冰粉' / 'ice jelly', raw 98.9f must be sanitized to 1.5f
        assertEquals(1.5f, carbs, 0.001f)
    }
}
