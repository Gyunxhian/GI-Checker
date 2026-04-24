package com.example.diabetesassistant.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * USDA 营养查询缓存实体。
 */
@Entity(tableName = "usda_cache")
data class UsdaCache(
    @PrimaryKey
    @ColumnInfo(name = "ingredient_name")
    val ingredientName: String,
    @ColumnInfo(name = "carbs_per_100g")
    val carbsPer100g: Float,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
)
