package com.example.diabetesassistant.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 档案状态枚举。
 */
enum class FoodRecordStatus {
    PENDING,
    COMPLETED,
}

/**
 * 菜品识别档案实体。
 */
@Entity(tableName = "food_records")
data class FoodRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "image_path")
    val imagePath: String,
    @ColumnInfo(name = "record_date")
    val recordDate: Long,
    @ColumnInfo(name = "dish_name")
    val dishName: String,
    @ColumnInfo(name = "ai_json")
    val aiJson: String,
    @ColumnInfo(name = "status")
    val status: FoodRecordStatus = FoodRecordStatus.PENDING,
)
