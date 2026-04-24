package com.example.diabetesassistant.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.diabetesassistant.data.local.entity.UsdaCache

/**
 * USDA 缓存数据访问接口。
 */
@Dao
interface UsdaCacheDao {
    /**
     * 写入 USDA 缓存。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: UsdaCache): Long

    /**
     * 读取原料缓存。
     */
    @Query("SELECT * FROM usda_cache WHERE ingredient_name = :ingredientName LIMIT 1")
    suspend fun getByIngredientName(ingredientName: String): UsdaCache?
}
