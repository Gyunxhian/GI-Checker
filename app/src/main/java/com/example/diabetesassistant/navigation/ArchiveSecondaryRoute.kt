package com.example.diabetesassistant.navigation

object ArchiveSecondaryRoute {
    const val AllAnalyses = "archive/all-analyses"
    const val DayDetail = "archive/day-detail"
    const val MealIdArg = "mealId"
    const val MealDetailBase = "archive/meal-detail"
    const val MealDetail = "$MealDetailBase/{$MealIdArg}"

    fun mealDetail(mealId: String): String = "$MealDetailBase/$mealId"
}
