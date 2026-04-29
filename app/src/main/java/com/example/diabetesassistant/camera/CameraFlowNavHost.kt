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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
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
            fadeIn(
                animationSpec = tween(
                    durationMillis = 320,
                    easing = LinearOutSlowInEasing,
                ),
            ) + slideInHorizontally(
                animationSpec = tween(320, easing = LinearOutSlowInEasing),
            ) { fullWidth -> fullWidth / 18 }
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
                onNavigateToPreview = {
                    nestedNavController.navigate(CameraRoute.Preview)
                },
            )
        }
        composable(CameraRoute.Preview) {
            val pendingUriString by viewModel.pendingImageUri.collectAsState()
            CapturePreviewScreen(
                imageUriString = pendingUriString.orEmpty(),
                onRetake = {
                    viewModel.setPendingCaptureUri("")
                    nestedNavController.popBackStack()
                },
                onConfirm = {
                    nestedNavController.navigate(CameraRoute.Loading)
                },
                onBack = { nestedNavController.popBackStack() },
            )
        }
        composable(CameraRoute.Loading) {
            RecognitionLoadingScreen(
                viewModel = viewModel,
                navController = nestedNavController,
            )
        }
        composable(CameraRoute.Confirm) {
            RecognitionConfirmScreen(
                viewModel = viewModel,
                navController = nestedNavController,
            )
        }
        composable(CameraRoute.Insights) {
            AiInsightsScreen(
                viewModel = viewModel,
                navController = nestedNavController,
                onFinish = {
                    viewModel.resetFlow()
                    onExitCamera()
                },
            )
        }
    }
}
