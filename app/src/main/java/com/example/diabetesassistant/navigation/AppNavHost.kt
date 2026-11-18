package com.example.diabetesassistant.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.diabetesassistant.camera.ArchiveDetailScreen
import com.example.diabetesassistant.camera.CameraRecognitionViewModel
import com.example.diabetesassistant.ui.screens.AiAssistantScreen
import com.example.diabetesassistant.ui.screens.ArchiveScreen
import com.example.diabetesassistant.ui.screens.CameraScreen
import com.example.diabetesassistant.ui.screens.HomeScreen
import com.example.diabetesassistant.ui.screens.LifeScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    guardianDays: Int = 1,
) {
    NavHost(
        navController = navController,
        startDestination = MainDestination.Home.route,
        modifier = modifier,
    ) {
        // 1. 首页看板
        composable(MainDestination.Home.route) {
            HomeScreen(
                guardianDays = guardianDays,
                onScanClick = {
                    navController.navigate(MainDestination.Camera.route) {
                        launchSingleTop = true
                    }
                },
                onMealClick = { recordId ->
                    navController.navigate(ArchiveSecondaryRoute.mealDetail(recordId))
                },
                onViewArchiveClick = {
                    navController.navigate(MainDestination.Archive.route) {
                        launchSingleTop = true
                    }
                },
                onAiAssistantClick = {
                    navController.navigate(ArchiveSecondaryRoute.AiAssistant)
                },
            )
        }

        // 2. 拍照分析全流程
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

        // 3. 档案夹日历
        composable(MainDestination.Archive.route) {
            ArchiveScreen(
                onRecordClick = { recordId ->
                    navController.navigate(ArchiveSecondaryRoute.mealDetail(recordId))
                },
                onScanClick = {
                    navController.navigate(MainDestination.Camera.route) {
                        launchSingleTop = true
                    }
                },
            )
        }

        // 4. 餐食档案详情复盘与微调（真实数据联动）
        composable(
            route = ArchiveSecondaryRoute.MealDetail,
            arguments = listOf(
                navArgument(ArchiveSecondaryRoute.MealIdArg) {
                    type = NavType.LongType
                }
            ),
        ) { entry ->
            val recordId = entry.arguments?.getLong(ArchiveSecondaryRoute.MealIdArg) ?: 0L
            val viewModel: CameraRecognitionViewModel = hiltViewModel()

            LaunchedEffect(recordId) {
                if (recordId > 0L) {
                    viewModel.loadRecord(recordId)
                }
            }

            val state by viewModel.uiState.collectAsStateWithLifecycle()

            ArchiveDetailScreen(
                uiState = state,
                onStapleIntakeChange = { viewModel.updateStapleIntake(it) },
                onVegIntakeChange = { viewModel.updateVegIntake(it) },
                onMeatIntakeChange = { viewModel.updateMeatIntake(it) },
                onBloodSugarChange = { viewModel.updateBloodSugarStart(it) },
                onInsulinTypeChange = { viewModel.updateInsulinType(it) },
                onSave = {
                    viewModel.saveOrUpdateRecord {
                        navController.popBackStack()
                    }
                },
                onBack = {
                    navController.popBackStack()
                },
            )
        }

        // 5. 个人代谢中心
        composable(MainDestination.Life.route) {
            LifeScreen(
                onResetComplete = {
                    navController.navigate(MainDestination.Home.route) {
                        popUpTo(MainDestination.Home.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        // 6. GI AI 专属健康助手 (沉浸式问答会话)
        composable(ArchiveSecondaryRoute.AiAssistant) {
            AiAssistantScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
