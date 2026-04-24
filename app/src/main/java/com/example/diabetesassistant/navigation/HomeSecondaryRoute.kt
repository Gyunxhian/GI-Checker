package com.example.diabetesassistant.navigation

object HomeSecondaryRoute {
    const val AiAdvice = "home/ai-advice"
    const val FoodDiary = "home/food-diary"
    const val PostIdArg = "postId"
    const val UserIdArg = "userId"
    const val PostDetailBase = "home/post-detail"
    const val PostDetail = "$PostDetailBase/{$PostIdArg}"
    const val UserProfileBase = "home/user"
    const val UserProfile = "$UserProfileBase/{$UserIdArg}"
    const val RecipeSuggestion = "home/recipe-suggestion"
    const val ExercisePlan = "home/exercise-plan"
    const val MedicationReview = "home/medication-review"
    const val MealEdit = "home/meal-edit"

    fun postDetail(postId: String): String = "$PostDetailBase/$postId"

    fun userProfile(userId: String): String = "$UserProfileBase/$userId"
}
