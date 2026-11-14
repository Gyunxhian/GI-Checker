package com.example.diabetesassistant.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import com.example.diabetesassistant.camera.CameraFlowNavHost
import com.example.diabetesassistant.camera.CameraRecognitionViewModel

@Composable
fun CameraScreen(
    parentBackStackEntry: NavBackStackEntry,
    onExitToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cameraViewModel: CameraRecognitionViewModel = hiltViewModel(parentBackStackEntry)
    CameraFlowNavHost(
        viewModel = cameraViewModel,
        onExitCamera = onExitToHome,
        modifier = modifier,
    )
}
