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
    suspend fun getCarbsPer100g(name: String, nameEn: String?): NetworkResult<Float> = getCarbsPer100g(name)
}

@Singleton
class NutritionRepositoryImpl @Inject constructor(
    private val usdaApi: UsdaApi,
    private val usdaCacheDao: UsdaCacheDao,
    private val apiKeyProvider: ApiKeyProvider,
) : NutritionRepository {

    override suspend fun getCarbsPer100g(ingredientName: String): NetworkResult<Float> {
        return getCarbsPer100g(name = ingredientName, nameEn = null)
    }

    override suspend fun getCarbsPer100g(name: String, nameEn: String?): NetworkResult<Float> {
        val cleanChinese = name.trim().lowercase()
        val cleanEn = nameEn?.trim()?.lowercase()
        Log.d(TAG, "查询碳水: name='$cleanChinese', nameEn='$cleanEn'")

        // 1. 优先使用本地《中国食物成分表》权威库（BuiltInCarbFallback），杜绝被海外包装食品错误数据污染
        val localChinese = BuiltInCarbFallback.lookup(cleanChinese)
        if (localChinese != null) {
            Log.d(TAG, "权威中餐成分库命中(中文): '$cleanChinese' -> ${localChinese}g/100g")
            return NetworkResult.Success(localChinese)
        }
        if (!cleanEn.isNullOrBlank()) {
            val localEn = BuiltInCarbFallback.lookup(cleanEn)
            if (localEn != null) {
                Log.d(TAG, "权威中餐成分库命中(英文): '$cleanEn' -> ${localEn}g/100g")
                return NetworkResult.Success(localEn)
            }
        }

        // 2. 检查本地数据库缓存（带生理学生物学极限过滤，清洗脏缓存）
        val queryKey = if (!cleanEn.isNullOrBlank()) cleanEn else cleanChinese
        usdaCacheDao.getByIngredientName(queryKey)?.let { cached ->
            val sanitized = sanitizeCarbs(cleanChinese, cleanEn, cached.carbsPer100g)
            if (sanitized != cached.carbsPer100g) {
                usdaCacheDao.upsert(UsdaCache(ingredientName = queryKey, carbsPer100g = sanitized))
            }
            Log.d(TAG, "缓存命中 '$queryKey': ${sanitized}g/100g (原值=${cached.carbsPer100g})")
            return NetworkResult.Success(sanitized)
        }

        // 3. 请求 USDA API (若网络或API不可用则降级)
        Log.d(TAG, "缓存与内置均未直接命中, 请求USDA API: '$queryKey'")
        val apiResult = try {
            val response = usdaApi.searchFood(
                apiKey = apiKeyProvider.usdaApiKey(),
                request = UsdaSearchRequest(query = queryKey),
            )
            val food = response.foods.firstOrNull()
            Log.d(TAG, "USDA返回: ${response.foods.size} 个匹配食物")

            food?.foodNutrients
                ?.firstOrNull { nutrient ->
                    val nutrientName = nutrient.nutrientName ?: return@firstOrNull false
                    nutrientName.contains("Carbohydrate", ignoreCase = true)
                }
                ?.value
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
            val sanitized = sanitizeCarbs(cleanChinese, cleanEn, apiResult)
            usdaCacheDao.upsert(
                UsdaCache(ingredientName = queryKey, carbsPer100g = sanitized),
            )
            Log.d(TAG, "USDA结果已校准并缓存: '$queryKey' = ${sanitized}g/100g (原始=${apiResult})")
            return NetworkResult.Success(sanitized)
        }

        val fallback = BuiltInCarbFallback.lookup(cleanChinese)
            ?: cleanEn?.let { BuiltInCarbFallback.lookup(it) }
        if (fallback != null) {
            usdaCacheDao.upsert(
                UsdaCache(ingredientName = queryKey, carbsPer100g = fallback),
            )
            return NetworkResult.Success(fallback)
        }

        Log.e(TAG, "查询失败且无可用数据: '$name' / '$nameEn'")
        return NetworkResult.Error(message = "Carbohydrate data not found for: $name")
    }

    private fun sanitizeCarbs(chinese: String, english: String?, raw: Float): Float {
        val text = "$chinese ${english.orEmpty()}".lowercase()
        // 蛋类（鸡蛋、鸭蛋、鹌鹑蛋）：绝不可能超过 5g/100g (防止 USDA 匹配到鸡蛋挂面 57.7g)
        if (text.contains("蛋") || text.contains("egg")) {
            if (!text.contains("面") && !text.contains("noodle") && !text.contains("cake") && !text.contains("roll") && !text.contains("饼")) {
                if (raw > 5f) return 1.1f
            }
        }
        // 肉类（五花肉、猪肉、牛肉、羊肉、鸡肉、鱼、虾）：纯肉碳水绝不可能 > 5g/100g
        if (text.contains("肉") || text.contains("猪") || text.contains("牛") || text.contains("羊") ||
            text.contains("鸡") || text.contains("鸭") || text.contains("鱼") || text.contains("虾") ||
            text.contains("pork") || text.contains("beef") || text.contains("chicken") || text.contains("fish") || text.contains("shrimp")) {
            if (!text.contains("面") && !text.contains("饭") && !text.contains("粉") && !text.contains("包") && !text.contains("饼")) {
                if (raw > 5f) return 0.2f
            }
        }
        // 冰粉类：纯水凝胶，水分 > 98%，纯冰粉碳水仅 1.5g/100g (防止 USDA 匹配到纯淀粉 98.9g)
        if (text.contains("冰粉") || text.contains("bingfen") || text.contains("ice jelly") || text.contains("aiyu")) {
            if (raw > 5f) return 1.5f
        }
        // 凉粉/凉皮/魔芋类：水含量 > 90%，绝不能作为生干淀粉(85g+)计算
        if (text.contains("凉粉") || text.contains("凉皮") || text.contains("魔芋") || text.contains("仙草") || text.contains("starch jelly") || text.contains("liangfen")) {
            if (raw > 18f) return 9.2f
        }
        // 汤品海藻类（紫菜、海带）：熟食复水态，绝不能按干燥品(50g+)计算
        if (text.contains("紫菜") || text.contains("海带") || text.contains("seaweed") || text.contains("kelp")) {
            if (raw > 15f) return 5.3f
        }
        return raw
    }
}
