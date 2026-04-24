package com.example.diabetesassistant.archive.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "meal_records")
@TypeConverters(Converters::class)
data class MealRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: LocalDateTime,
    val date: LocalDate, // 用于按天查询
    val imageUrl: String,
    val mealType: String, // 早餐, 午餐, 晚餐, 加餐
    val totalCalories: Double,
    val totalCarbs: Double,
    val totalProtein: Double,
    val totalFat: Double,
    val dishes: List<DishAnalysis> // 存储在该餐中的菜品分析结果
)

data class DishAnalysis(
    val name: String,
    val weight: Double, // 克
    val calories: Double,
    val carbs: Double,
    val protein: Double,
    val fat: Double
)
