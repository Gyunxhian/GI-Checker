package com.example.diabetesassistant.archive.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val gson = Gson()

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

    @TypeConverter
    fun fromDishList(value: String?): List<DishAnalysis> {
        if (value == null) return emptyList()
        val listType = object : TypeToken<List<DishAnalysis>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun dishListToString(list: List<DishAnalysis>?): String {
        return gson.toJson(list ?: emptyList<DishAnalysis>())
    }
}
