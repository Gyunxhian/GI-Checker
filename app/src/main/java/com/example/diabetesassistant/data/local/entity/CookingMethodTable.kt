package com.example.diabetesassistant.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 烹饪方式修正表实体。
 */
@Entity(tableName = "cooking_method_table")
data class CookingMethodTable(
    @PrimaryKey
    @ColumnInfo(name = "cooking_method")
    val cookingMethod: String,
    @ColumnInfo(name = "correction_value")
    val correctionValue: Float,
)
