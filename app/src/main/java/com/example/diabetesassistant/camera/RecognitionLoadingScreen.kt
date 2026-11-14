package com.example.diabetesassistant.camera

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RecognitionLoadingScreen(
    viewModel: CameraRecognitionViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    var stepIndex by remember { mutableIntStateOf(0) }
    val steps = remember {
        listOf(
            "正在预处理与压缩多张餐盘图片...",
            "DeepSeek 视觉模型正在深度识别菜品与食材...",
            "正在对接 USDA 数据库核验精准碳水化合物与 GI...",
            "正在通过吸收动力学模拟 Gamma 代谢曲线...",
            "正在计算胰岛素参考带与重点关注时段...",
            "正在生成智能个性化餐后关怀提醒...",
        )
    }

    BackHandler {
        navController.popBackStack()
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1600L)
            stepIndex = (stepIndex + 1) % steps.size
        }
    }

    LaunchedEffect(Unit) {
        viewModel.startAnalysis()
    }

    // 只有在分析完成且存在结果、无致命阻断错误时，才平滑进入详情
    LaunchedEffect(state.isLoading, state.error, state.recognitionResult) {
        if (!state.isLoading && state.error == null && state.recognitionResult != null) {
            navController.popBackStack()
            navController.navigate(CameraRoute.Detail)
        }
    }

    val transition = rememberInfiniteTransition(label = "loadingPulse")
    val pulse by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(horizontal = 16.dp, vertical = 24.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (state.error != null) {
                    // 异常提示视图
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                        modifier = Modifier.size(76.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "错误",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(38.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "餐盘分析遇到问题",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = state.error ?: "网络连接或识别异常，请检查后重试",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedButton(
                            onClick = { navController.popBackStack() },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("返回修改", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    viewModel.startAnalysis()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White,
                            ),
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("重新分析", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    // 正在分析加载动效视图
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(120.dp),
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .size(112.dp)
                                .scale(pulse),
                        ) {}

                        CircularProgressIndicator(
                            modifier = Modifier.size(88.dp),
                            strokeWidth = 5.dp,
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        )

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(56.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI分析",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "正在智能分析餐盘",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 19.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = steps[stepIndex],
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        minLines = 2,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth(0.72f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "基于国家营养库与动力学算法实时测算\n全程本地私密计算，保护医疗隐私",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp,
                    )
                }
            }
        }
    }
}
