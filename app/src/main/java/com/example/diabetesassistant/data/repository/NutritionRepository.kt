package com.example.diabetesassistant.data.repository

import com.example.diabetesassistant.data.local.dao.UsdaCacheDao
import com.example.diabetesassistant.data.local.entity.UsdaCache
import com.example.diabetesassistant.data.remote.ApiKeyProvider
import com.example.diabetesassistant.data.remote.NetworkResult
import com.example.diabetesassistant.data.remote.UsdaApi
import com.example.diabetesassistant.data.remote.dto.UsdaSearchRequest
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.HttpException

/**
 * 营养查询仓库接口。
 */
interface NutritionRepository {
    /**
     * 查询基础碳水（g/100g），优先本地缓存。
     */
    suspend fun getCarbsPer100g(ingredientName: String): NetworkResult<Float>
}

/**
 * 营养查询仓库实现（USDA + Room 缓存）。
 */
@Singleton
class NutritionRepositoryImpl @Inject constructor(
    private val usdaApi: UsdaApi,
    private val usdaCacheDao: UsdaCacheDao,
    private val apiKeyProvider: ApiKeyProvider,
) : NutritionRepository {
    /**
     * @see NutritionRepository.getCarbsPer100g
     */
    override suspend fun getCarbsPer100g(ingredientName: String): NetworkResult<Float> {
        val normalizedName = ingredientName.trim().lowercase()
        usdaCacheDao.getByIngredientName(normalizedName)?.let { cached ->
            return NetworkResult.Success(cached.carbsPer100g)
        }

        return try {
            val response = usdaApi.searchFood(
                apiKey = apiKeyProvider.usdaApiKey(),
                request = UsdaSearchRequest(query = translateIngredientName(normalizedName)),
            )
            val carbs = response.foods
                .firstOrNull()
                ?.foodNutrients
                ?.firstOrNull { nutrient ->
                    val name = nutrient.nutrientName ?: return@firstOrNull false
                    name.contains("Carbohydrate", ignoreCase = true)
                }
                ?.value

            if (carbs == null) {
                NetworkResult.Error(message = "USDA carbohydrate nutrient not found.")
            } else {
                usdaCacheDao.upsert(
                    UsdaCache(
                        ingredientName = normalizedName,
                        carbsPer100g = carbs,
                    ),
                )
                NetworkResult.Success(carbs)
            }
        } catch (exception: HttpException) {
            NetworkResult.Error(
                code = exception.code(),
                message = "USDA request failed: HTTP ${exception.code()}",
                throwable = exception,
            )
        } catch (exception: IOException) {
            NetworkResult.Error(message = "USDA network error.", throwable = exception)
        } catch (exception: Exception) {
            NetworkResult.Error(message = "USDA unknown error.", throwable = exception)
        }
    }

    /**
     * 中文原料名映射英文。
     */
    fun translateIngredientName(chineseName: String): String {
        val map = mapOf(
            "番茄" to "tomato",
            "西红柿" to "tomato",
            "鸡蛋" to "egg",
            "五花肉" to "pork belly",
            "米饭" to "rice",
            "土豆" to "potato",
            "胡萝卜" to "carrot",
            "洋葱" to "onion",
            "冰糖" to "rock sugar",
        )
        return map[chineseName] ?: chineseName
    }
}
