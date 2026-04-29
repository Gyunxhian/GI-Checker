package com.example.diabetesassistant.camera

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.diabetesassistant.R
import com.example.diabetesassistant.ui.components.GlassCard
import com.example.diabetesassistant.ui.components.GreenGlassBackground
import java.io.File
import java.util.concurrent.Executors

private val CameraGreen = Color(0xFF17DC66)
private val CameraDarkGreen = Color(0xFF008A3D)
private val CameraInk = Color(0xFF173025)
private val CameraMuted = Color(0xFF64746B)

private fun copyPickedImageToCache(context: android.content.Context, source: Uri): Uri {
    val dir = File(context.cacheDir, "camera_captures").apply { mkdirs() }
    val outFile = File(dir, "picked_${System.currentTimeMillis()}.jpg")
    context.contentResolver.openInputStream(source)?.use { input ->
        outFile.outputStream().use { output -> input.copyTo(output) }
    } ?: error("openInputStream null")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        outFile,
    )
}

@Composable
fun CameraCaptureScreen(
    viewModel: CameraRecognitionViewModel,
    onExitCamera: () -> Unit,
    onNavigateToPreview: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    val previewView = remember(context) {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasPermission = granted },
    )

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val stableUri = copyPickedImageToCache(context, uri)
            viewModel.setPendingCaptureUri(stableUri.toString())
            onNavigateToPreview()
        } catch (_: Exception) {
            Toast.makeText(context, R.string.camera_pick_failed, Toast.LENGTH_SHORT).show()
        }
    }

    fun launchGalleryPicker() {
        pickImageLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
        )
    }

    fun takePhoto() {
        val dir = File(context.cacheDir, "camera_captures").apply { mkdirs() }
        val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
        val opts = ImageCapture.OutputFileOptions.Builder(file).build()
        val mainEx = ContextCompat.getMainExecutor(context)
        imageCapture.takePicture(
            opts,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val uri = output.savedUri ?: FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file,
                    )
                    viewModel.setPendingCaptureUri(uri.toString())
                    mainEx.execute { onNavigateToPreview() }
                }

                override fun onError(exception: ImageCaptureException) {
                    mainEx.execute {
                        Toast.makeText(
                            context,
                            exception.message ?: "",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
            },
        )
    }

    DisposableEffect(lifecycleOwner, previewView, hasPermission) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val mainExecutor = ContextCompat.getMainExecutor(context)
        val listener = Runnable {
            if (!hasPermission) return@Runnable
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture,
                )
            } catch (_: Exception) {
                Toast.makeText(context, R.string.camera_permission_rationale, Toast.LENGTH_SHORT).show()
            }
        }
        cameraProviderFuture.addListener(listener, mainExecutor)
        onDispose {
            if (hasPermission) {
                try {
                    cameraProviderFuture.get().unbindAll()
                } catch (_: Exception) {
                }
            }
        }
    }

    GreenGlassBackground(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (hasPermission) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.16f),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.50f)),
                )
            CameraTopBar(onBackClick = onExitCamera)

                Text(
                    text = "请将硬币放入画面作尺寸参照",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CameraDarkGreen,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 74.dp),
                )

                ScannerFrame(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .height(270.dp)
                        .padding(horizontal = 46.dp),
                )
                CameraControls(
                    onGalleryClick = { launchGalleryPicker() },
                    onShutterClick = { takePhoto() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 36.dp),
                )
            } else {
            CameraTopBar(onBackClick = onExitCamera)
                PermissionGlassPanel(
                    onGrantClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    onGalleryClick = { launchGalleryPicker() },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp),
                )
            }
        }
    }
}

@Composable
private fun CameraTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                onClick = onBackClick,
                shape = CircleShape,
                color = Color.Transparent,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回首页",
                    tint = CameraDarkGreen,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(24.dp),
                )
            }
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = null,
                tint = Color(0xFF2D6B61),
                modifier = Modifier.size(34.dp),
            )
            Text(
                text = "糖控助手",
                style = MaterialTheme.typography.titleMedium,
                color = CameraDarkGreen,
                fontWeight = FontWeight.ExtraBold,
            )
        }
        Icon(
            imageVector = Icons.Filled.Notifications,
            contentDescription = null,
            tint = CameraDarkGreen,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun ScannerFrame(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val corner = 32.dp.toPx()
            val stroke = 2.dp.toPx()
            val lineY = size.height * 0.5f
            val glow = CameraGreen.copy(alpha = 0.14f)
            val guide = CameraGreen.copy(alpha = 0.72f)

            drawLine(glow, Offset(0f, lineY), Offset(size.width, lineY), strokeWidth = 18.dp.toPx(), cap = StrokeCap.Round)
            drawLine(guide, Offset(0f, lineY), Offset(size.width, lineY), strokeWidth = 1.4.dp.toPx(), cap = StrokeCap.Round)

            drawLine(guide, Offset(0f, 0f), Offset(corner, 0f), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(guide, Offset(0f, 0f), Offset(0f, corner), strokeWidth = stroke, cap = StrokeCap.Round)

            drawLine(guide, Offset(size.width, 0f), Offset(size.width - corner, 0f), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(guide, Offset(size.width, 0f), Offset(size.width, corner), strokeWidth = stroke, cap = StrokeCap.Round)

            drawLine(guide, Offset(0f, size.height), Offset(corner, size.height), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(guide, Offset(0f, size.height), Offset(0f, size.height - corner), strokeWidth = stroke, cap = StrokeCap.Round)

            drawLine(guide, Offset(size.width, size.height), Offset(size.width - corner, size.height), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(guide, Offset(size.width, size.height), Offset(size.width, size.height - corner), strokeWidth = stroke, cap = StrokeCap.Round)
        }
    }
}

@Composable
private fun CameraControls(
    onGalleryClick: () -> Unit,
    onShutterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Surface(
            onClick = onGalleryClick,
            modifier = Modifier
                .size(48.dp)
                .border(1.dp, Color.White.copy(alpha = 0.72f), CircleShape),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.58f),
            shadowElevation = 8.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.PhotoLibrary,
                    contentDescription = stringResource(R.string.camera_pick_gallery),
                    tint = CameraDarkGreen,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Spacer(modifier = Modifier.size(20.dp))
        Surface(
            onClick = onShutterClick,
            modifier = Modifier
                .size(82.dp)
                .shadow(
                    elevation = 32.dp,
                    shape = CircleShape,
                    ambientColor = CameraGreen.copy(alpha = 0.70f),
                    spotColor = CameraGreen.copy(alpha = 0.70f),
                )
                .border(1.dp, Color.White.copy(alpha = 0.70f), CircleShape),
            shape = CircleShape,
            color = CameraGreen,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = stringResource(R.string.camera_shutter),
                    tint = Color.White,
                    modifier = Modifier.size(30.dp),
                )
            }
        }
    }
}

@Composable
private fun PermissionGlassPanel(
    onGrantClick: () -> Unit,
    onGalleryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = stringResource(R.string.camera_permission_rationale),
                style = MaterialTheme.typography.titleMedium,
                color = CameraInk,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.camera_pick_hint),
                style = MaterialTheme.typography.bodySmall,
                color = CameraMuted,
                textAlign = TextAlign.Center,
            )
            Button(
                onClick = onGrantClick,
                colors = ButtonDefaults.buttonColors(containerColor = CameraDarkGreen),
            ) {
                Text(stringResource(R.string.camera_grant_permission))
            }
            OutlinedButton(onClick = onGalleryClick) {
                Icon(
                    Icons.Outlined.PhotoLibrary,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text(stringResource(R.string.camera_pick_gallery))
            }
        }
    }
}
