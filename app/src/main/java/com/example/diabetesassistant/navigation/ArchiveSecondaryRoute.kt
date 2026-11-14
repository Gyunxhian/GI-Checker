package com.example.diabetesassistant.navigation

object ArchiveSecondaryRoute {
    const val MealIdArg = "recordId"
    const val MealDetailBase = "archive/meal-detail"
    const val MealDetail = "$MealDetailBase/{$MealIdArg}"

    fun mealDetail(recordId: Long): String = "$MealDetailBase/$recordId"
}
