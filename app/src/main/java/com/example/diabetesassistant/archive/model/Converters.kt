package com.example.diabetesassistant.archive.model

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter
    fun fromTimestamp(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, dateTimeFormatter) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): String? {
        return date?.format(dateTimeFormatter)
    }

    @TypeConverter
    fun fromLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it, dateFormatter) }
    }

    @TypeConverter
    fun localDateToString(date: LocalDate?): String? {
        return date?.format(dateFormatter)
    }

    // 伪代码：实际使用时推荐使用 kotlinx.serialization 或 Gson
    @TypeConverter
    fun fromDishList(value: String): List<DishAnalysis> {
        // 实现将 JSON 字符串转回 List 的逻辑
        return emptyList()
    }

    @TypeConverter
    fun dishListToString(list: List<DishAnalysis>): String {
        // 实现将 List 转为 JSON 字符串的逻辑
        return ""
    }
}
