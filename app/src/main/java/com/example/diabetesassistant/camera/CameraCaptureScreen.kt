package com.example.diabetesassistant.camera

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File

private val StagingBg = Color(0xFFF7FAF8)
private val BrandForest = Color(0xFF144D30)
private val BrandGreen = Color(0xFF227B4E)
private val BrandMint = Color(0xFF52B788)
private val BrandLightMint = Color(0xFFE8F5EE)
private val StagingBorder = Color(0xFFDCE6DF)
private val TextDark = Color(0xFF1A2620)
private val TextMuted = Color(0xFF4A5568)

private fun copyPickedImageToCache(context: android.content.Context, source: Uri): Uri {
    val dir = File(context.filesDir, "meal_images").apply { mkdirs() }
    val outFile = File(dir, "picked_${System.currentTimeMillis()}_${(100..999).random()}.jpg")
    context.contentResolver.openInputStream(source)?.use { input ->
        outFile.outputStream().use { output -> input.copyTo(output) }
    } ?: error("无法打开相册图片")
    return Uri.fromFile(outFile)
}

private fun createCameraTempUri(context: android.content.Context): Uri {
    val dir = File(context.filesDir, "meal_images").apply { mkdirs() }
    val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
    if (!file.exists()) {
        file.createNewFile()
    }
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )
}

/**
 * 现代极简餐盘多图暂存台
 * 直接调用手机自带系统相机与相册多选，无多余冗杂扫描框。
 */
@Composable
fun CameraCaptureScreen(
    viewModel: CameraRecognitionViewModel,
    onExitCamera: () -> Unit,
    onNavigateToLoading: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val stagedImages = uiState.stagedImages

    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    // 1. 系统自带原生相机调用
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            pendingCameraUri?.let { uri ->
                viewModel.addStagedImage(uri.toString())
            }
        }
    }

    // 2. 系统相册多图选取
    val pickMultipleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 6),
    ) { uris ->
        if (uris.isNotEmpty()) {
            val copiedUris = uris.mapNotNull { uri ->
                try {
                    copyPickedImageToCache(context, uri).toString()
                } catch (e: Exception) {
                    null
                }
            }
            viewModel.addStagedImages(copiedUris)
        }
    }

    fun launchNativeCamera() {
        if (stagedImages.size >= 6) {
            Toast.makeText(context, "一次最多添加 6 张餐盘照片", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = createCameraTempUri(context)
        pendingCameraUri = uri
        takePictureLauncher.launch(uri)
    }

    fun launchGallery() {
        if (stagedImages.size >= 6) {
            Toast.makeText(context, "一次最多添加 6 张餐盘照片", Toast.LENGTH_SHORT).show()
            return
        }
        pickMultipleLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
        )
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = StagingBg,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 顶部导航栏，适配状态栏安全区域
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 1.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onExitCamera) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = BrandForest,
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "记录本次餐盘",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                        )
                        Text(
                            text = "支持拍摄或上传多道菜品，一次性合并测算",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                        )
                    }
                    if (stagedImages.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearStagedImages() }) {
                            Icon(
                                imageVector = Icons.Filled.DeleteSweep,
                                contentDescription = "清空全部",
                                tint = Color(0xFFD62828),
                            )
                        }
                    }
                }
            }

            // 主展示区
            if (stagedImages.isEmpty()) {
                // 空态：提供大卡片引导拍照与选图
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(BrandLightMint),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Restaurant,
                                contentDescription = null,
                                tint = BrandGreen,
                                modifier = Modifier.size(46.dp),
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = "拍摄这一餐的全部菜品与主食",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                            )
                            Text(
                                text = "支持一次拍摄多张照片（例如主菜、米饭、汤碗）。\nAI 将自动合并所有菜品清单，精准测算整餐升糖负荷。",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted,
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp,
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = { launchNativeCamera() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandGreen,
                                contentColor = Color.White,
                            ),
                        ) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("调用系统相机拍照", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = { launchGallery() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, BrandGreen),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandGreen),
                        ) {
                            Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("从手机相册多选", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // 已有照片暂存网格
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "已添加照片 (${stagedImages.size}/6)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                        )
                        Text(
                            text = "可点击右上方 × 移除单张",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                        )
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 16.dp),
                    ) {
                        itemsIndexed(stagedImages) { index, uriString ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(170.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, StagingBorder),
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = uriString,
                                        contentDescription = "已选菜品 $index",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                    )

                                    // 菜品序号胶囊
                                    Surface(
                                        shape = RoundedCornerShape(bottomEnd = 10.dp),
                                        color = BrandForest.copy(alpha = 0.85f),
                                        modifier = Modifier.align(Alignment.TopStart),
                                    ) {
                                        Text(
                                            text = "菜品 ${index + 1}",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        )
                                    }

                                    // 单张删除按钮
                                    Surface(
                                        onClick = { viewModel.removeStagedImage(uriString) },
                                        shape = CircleShape,
                                        color = Color.Black.copy(alpha = 0.65f),
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .size(28.dp),
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Filled.Close,
                                                contentDescription = "移除此图",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 如果未满 6 张，显示添加更多按钮卡片
                        if (stagedImages.size < 6) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.5.dp,
                                        BrandMint.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(170.dp),
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            IconButton(
                                                onClick = { launchNativeCamera() },
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .background(BrandLightMint, CircleShape),
                                            ) {
                                                Icon(
                                                    Icons.Filled.AddAPhoto,
                                                    contentDescription = "继续拍照",
                                                    tint = BrandGreen,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = { launchGallery() },
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .background(BrandLightMint, CircleShape),
                                            ) {
                                                Icon(
                                                    Icons.Filled.AddPhotoAlternate,
                                                    contentDescription = "相册加图",
                                                    tint = BrandGreen,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            text = "追加菜品图片",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = BrandForest,
                                        )
                                        Text(
                                            text = "拍照或相册选图",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 底部启动分析栏，适配导航栏安全区域
            if (stagedImages.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 10.dp,
                    shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = onNavigateToLoading,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandGreen,
                                contentColor = Color.White,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                        ) {
                            Icon(Icons.Filled.Restaurant, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "开始智能分析餐盘 (${stagedImages.size} 张)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }
                    }
                }
            }
        }
    }
}
