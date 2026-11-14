package com.example.diabetesassistant.camera

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun CameraFlowNavHost(
    viewModel: CameraRecognitionViewModel,
    onExitCamera: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val nestedNavController = rememberNavController()
    val nestedBackStackEntry by nestedNavController.currentBackStackEntryAsState()
    val currentRoute = nestedBackStackEntry?.destination?.route

    BackHandler {
        if (currentRoute == CameraRoute.Capture) {
            onExitCamera()
        } else {
            nestedNavController.popBackStack()
        }
    }

    NavHost(
        navController = nestedNavController,
        startDestination = CameraRoute.Capture,
        modifier = modifier.fillMaxSize(),
        enterTransition = {
            fadeIn(animationSpec = tween(320, easing = LinearOutSlowInEasing)) +
                slideInHorizontally(animationSpec = tween(320, easing = LinearOutSlowInEasing)) { fullWidth -> fullWidth / 18 }
        },
        exitTransition = {
            fadeOut(animationSpec = tween(240)) +
                slideOutHorizontally(animationSpec = tween(240)) { -(it / 22) }
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(240)) +
                slideInHorizontally(animationSpec = tween(240)) { -(it / 22) }
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(320)) +
                slideOutHorizontally(animationSpec = tween(320)) { fullWidth -> fullWidth / 18 }
        },
    ) {
        composable(CameraRoute.Capture) {
            CameraCaptureScreen(
                viewModel = viewModel,
                onExitCamera = onExitCamera,
                onNavigateToLoading = {
                    nestedNavController.navigate(CameraRoute.Loading)
                },
            )
        }
        composable(CameraRoute.Loading) {
            RecognitionLoadingScreen(
                viewModel = viewModel,
                navController = nestedNavController,
            )
        }
        composable(CameraRoute.Detail) {
            val state by viewModel.uiState.collectAsState()
            ArchiveDetailScreen(
                uiState = state,
                onMeatRatioChange = { viewModel.updateMeatRatio(it) },
                onIntakePercentChange = { viewModel.updateIntakePercent(it) },
                onBloodSugarChange = { viewModel.updateBloodSugarStart(it) },
                onInsulinTypeChange = { viewModel.updateInsulinType(it) },
                onSave = {
                    viewModel.saveOrUpdateRecord {
                        viewModel.resetFlow()
                        onExitCamera()
                    }
                },
                onBack = {
                    viewModel.resetFlow()
                    onExitCamera()
                },
            )
        }
    }
}
