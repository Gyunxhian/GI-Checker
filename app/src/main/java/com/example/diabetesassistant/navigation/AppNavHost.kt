package com.example.diabetesassistant.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.diabetesassistant.ui.screens.ArchiveAllAnalysesScreen
import com.example.diabetesassistant.ui.screens.ArchiveDayDetailScreen
import com.example.diabetesassistant.ui.screens.ArchiveMealDetailScreen
import com.example.diabetesassistant.ui.screens.ArchiveScreen
import com.example.diabetesassistant.ui.screens.AiAdviceScreen
import com.example.diabetesassistant.ui.screens.CameraScreen
import com.example.diabetesassistant.ui.screens.AnalysisPlanScreen
import com.example.diabetesassistant.ui.screens.DarkModeSettingsScreen
import com.example.diabetesassistant.ui.screens.ExercisePlanScreen
import com.example.diabetesassistant.ui.screens.FoodDiaryDetailScreen
import com.example.diabetesassistant.ui.screens.HomeScreen
import com.example.diabetesassistant.ui.screens.CommunityPostDetailScreen
import com.example.diabetesassistant.ui.screens.CommunityUserProfileScreen
import com.example.diabetesassistant.ui.screens.LifeScreen
import com.example.diabetesassistant.ui.screens.MealEditScreen
import com.example.diabetesassistant.ui.screens.MedicationPlanScreen
import com.example.diabetesassistant.ui.screens.MedicationReviewScreen
import com.example.diabetesassistant.ui.screens.NotificationSettingsScreen
import com.example.diabetesassistant.ui.screens.PrivacyDataScreen
import com.example.diabetesassistant.ui.screens.ProfileDetailScreen
import com.example.diabetesassistant.ui.screens.RecipeSuggestionScreen
import com.example.diabetesassistant.ui.screens.SyncArchiveScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onSignOut: () -> Unit = {},
) {
    val followedUserIds = remember { mutableStateListOf("user-lin") }

    NavHost(
        navController = navController,
        startDestination = MainDestination.Home.route,
        modifier = modifier,
    ) {
        composable(MainDestination.Home.route) {
            HomeScreen(
                onScanClick = {
                    navController.navigate(MainDestination.Camera.route) {
                        launchSingleTop = true
                    }
                },
                onAiAdviceClick = {
                    navController.navigate(HomeSecondaryRoute.AiAdvice)
                },
                onTodayRecordsClick = {
                    navController.navigate(HomeSecondaryRoute.FoodDiary)
                },
                onPostClick = { postId ->
                    navController.navigate(HomeSecondaryRoute.postDetail(postId))
                },
                followedUserIds = followedUserIds,
                onAuthorClick = { userId ->
                    navController.navigate(HomeSecondaryRoute.userProfile(userId))
                },
                onFollowClick = { userId ->
                    if (followedUserIds.contains(userId)) {
                        followedUserIds.remove(userId)
                    } else {
                        followedUserIds.add(userId)
                    }
                },
            )
        }
        composable(
            route = HomeSecondaryRoute.PostDetail,
            arguments = listOf(navArgument(HomeSecondaryRoute.PostIdArg) { type = NavType.StringType }),
        ) { entry ->
            CommunityPostDetailScreen(
                onBackClick = { navController.popBackStack() },
                postId = entry.arguments?.getString(HomeSecondaryRoute.PostIdArg) ?: "post-1",
                followedUserIds = followedUserIds,
                onAuthorClick = { userId ->
                    navController.navigate(HomeSecondaryRoute.userProfile(userId))
                },
                onFollowClick = { userId ->
                    if (followedUserIds.contains(userId)) {
                        followedUserIds.remove(userId)
                    } else {
                        followedUserIds.add(userId)
                    }
                },
            )
        }
        composable(
            route = HomeSecondaryRoute.UserProfile,
            arguments = listOf(navArgument(HomeSecondaryRoute.UserIdArg) { type = NavType.StringType }),
        ) { entry ->
            CommunityUserProfileScreen(
                onBackClick = { navController.popBackStack() },
                userId = entry.arguments?.getString(HomeSecondaryRoute.UserIdArg) ?: "user-zhang",
                followedUserIds = followedUserIds,
                onFollowClick = { userId ->
                    if (followedUserIds.contains(userId)) {
                        followedUserIds.remove(userId)
                    } else {
                        followedUserIds.add(userId)
                    }
                },
                onPostClick = { postId ->
                    navController.navigate(HomeSecondaryRoute.postDetail(postId))
                },
            )
        }
        composable(HomeSecondaryRoute.AiAdvice) {
            AiAdviceScreen(
                onBackClick = { navController.popBackStack() },
                onRecipeClick = {
                    navController.navigate(HomeSecondaryRoute.RecipeSuggestion)
                },
                onExerciseClick = {
                    navController.navigate(HomeSecondaryRoute.ExercisePlan)
                },
                onMedicationClick = {
                    navController.navigate(HomeSecondaryRoute.MedicationReview)
                },
            )
        }
        composable(HomeSecondaryRoute.FoodDiary) {
            FoodDiaryDetailScreen(
                onBackClick = { navController.popBackStack() },
                onAddMealClick = {
                    navController.navigate(MainDestination.Camera.route) {
                        launchSingleTop = true
                    }
                },
                onMealClick = { mealId ->
                    navController.navigate(ArchiveSecondaryRoute.mealDetail(mealId))
                },
                onEditMealClick = {
                    navController.navigate(HomeSecondaryRoute.MealEdit)
                },
            )
        }
        composable(HomeSecondaryRoute.RecipeSuggestion) {
            RecipeSuggestionScreen(
                onBackClick = { navController.popBackStack() },
            )
        }
        composable(HomeSecondaryRoute.ExercisePlan) {
            ExercisePlanScreen(
                onBackClick = { navController.popBackStack() },
            )
        }
        composable(HomeSecondaryRoute.MedicationReview) {
            MedicationReviewScreen(
                onBackClick = { navController.popBackStack() },
            )
        }
        composable(HomeSecondaryRoute.MealEdit) {
            MealEditScreen(
                onBackClick = { navController.popBackStack() },
                onRescanClick = {
                    navController.navigate(MainDestination.Camera.route) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(MainDestination.Camera.route) { entry ->
            CameraScreen(
                parentBackStackEntry = entry,
                onExitToHome = {
                    navController.navigate(MainDestination.Home.route) {
                        popUpTo(MainDestination.Home.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(MainDestination.Archive.route) {
            ArchiveScreen(
                onViewAllClick = {
                    navController.navigate(ArchiveSecondaryRoute.AllAnalyses)
                },
                onDayClick = {
                    navController.navigate(ArchiveSecondaryRoute.DayDetail)
                },
                onRecordClick = { mealId ->
                    navController.navigate(ArchiveSecondaryRoute.mealDetail(mealId))
                },
            )
        }
        composable(ArchiveSecondaryRoute.AllAnalyses) {
            ArchiveAllAnalysesScreen(
                onBackClick = { navController.popBackStack() },
                onRecordClick = { mealId ->
                    navController.navigate(ArchiveSecondaryRoute.mealDetail(mealId))
                },
            )
        }
        composable(ArchiveSecondaryRoute.DayDetail) {
            ArchiveDayDetailScreen(
                onBackClick = { navController.popBackStack() },
                onRecordClick = { mealId ->
                    navController.navigate(ArchiveSecondaryRoute.mealDetail(mealId))
                },
            )
        }
        composable(
            route = ArchiveSecondaryRoute.MealDetail,
            arguments = listOf(navArgument(ArchiveSecondaryRoute.MealIdArg) { type = NavType.StringType }),
        ) { entry ->
            ArchiveMealDetailScreen(
                onBackClick = { navController.popBackStack() },
                mealId = entry.arguments?.getString(ArchiveSecondaryRoute.MealIdArg) ?: "lunch_salad",
            )
        }
        composable(MainDestination.Life.route) {
            LifeScreen(
                onProfileClick = {
                    navController.navigate(LifeSecondaryRoute.Profile)
                },
                onAnalysisPlanClick = {
                    navController.navigate(LifeSecondaryRoute.AnalysisPlan)
                },
                onMedicationClick = {
                    navController.navigate(LifeSecondaryRoute.Medication)
                },
                onNotificationClick = {
                    navController.navigate(LifeSecondaryRoute.Notification)
                },
                onDarkModeClick = {
                    navController.navigate(LifeSecondaryRoute.DarkMode)
                },
                onSyncClick = {
                    navController.navigate(LifeSecondaryRoute.Sync)
                },
                onPrivacyClick = {
                    navController.navigate(LifeSecondaryRoute.Privacy)
                },
                onSignOutClick = onSignOut,
            )
        }
        composable(LifeSecondaryRoute.Profile) {
            ProfileDetailScreen(onBackClick = { navController.popBackStack() })
        }
        composable(LifeSecondaryRoute.AnalysisPlan) {
            AnalysisPlanScreen(onBackClick = { navController.popBackStack() })
        }
        composable(LifeSecondaryRoute.Medication) {
            MedicationPlanScreen(onBackClick = { navController.popBackStack() })
        }
        composable(LifeSecondaryRoute.Notification) {
            NotificationSettingsScreen(onBackClick = { navController.popBackStack() })
        }
        composable(LifeSecondaryRoute.DarkMode) {
            DarkModeSettingsScreen(onBackClick = { navController.popBackStack() })
        }
        composable(LifeSecondaryRoute.Sync) {
            SyncArchiveScreen(onBackClick = { navController.popBackStack() })
        }
        composable(LifeSecondaryRoute.Privacy) {
            PrivacyDataScreen(onBackClick = { navController.popBackStack() })
        }
    }
}
