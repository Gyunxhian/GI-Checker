package com.example.diabetesassistant.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.example.diabetesassistant.camera.CameraFlowNavHost
import com.example.diabetesassistant.camera.CameraRecognitionViewModel

@Composable
fun CameraScreen(
    parentBackStackEntry: NavBackStackEntry,
    onExitToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: CameraRecognitionViewModel = viewModel(parentBackStackEntry)
    CameraFlowNavHost(
        viewModel = viewModel,
        onExitCamera = onExitToHome,
        modifier = modifier,
    )
}
