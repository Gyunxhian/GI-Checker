package com.example.diabetesassistant.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.example.diabetesassistant.camera.CameraFlowNavHost
import com.example.diabetesassistant.camera.CameraRecognitionViewModel

/** Camera entry screen. */
@Composable
fun CameraScreen(
    parentBackStackEntry: NavBackStackEntry,
    onExitToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cameraViewModel: CameraRecognitionViewModel = viewModel(parentBackStackEntry)
    CameraFlowNavHost(
        viewModel = cameraViewModel,
        onExitCamera = onExitToHome,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun CameraScreenPreview() {
    CameraFlowNavHost(
        viewModel = CameraRecognitionViewModel(),
        onExitCamera = {},
    )
}
