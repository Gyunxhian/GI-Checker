package com.example.diabetesassistant.data.local

import androidx.room.TypeConverter
import com.example.diabetesassistant.data.local.entity.FoodRecordStatus

/**
 * Room 类型转换器。
 */
class Converters {
    /**
     * 状态枚举转字符串。
     */
    @TypeConverter
    fun fromFoodRecordStatus(status: FoodRecordStatus): String = status.name

    /**
     * 字符串转状态枚举。
     */
    @TypeConverter
    fun toFoodRecordStatus(value: String): FoodRecordStatus = FoodRecordStatus.valueOf(value)
}
