package com.example.diabetesassistant.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    elevation: Dp = 12.dp,
    containerColor: Color = Color.White.copy(alpha = 0.46f),
    borderColor: Color = Color.White.copy(alpha = 0.76f),
    content: @Composable () -> Unit,
) {
    val glassTint = containerColor.copy(alpha = containerColor.alpha.coerceIn(0.24f, 0.58f))

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Color(0xFF1A5F3A).copy(alpha = 0.12f),
                spotColor = Color(0xFF0A2C1A).copy(alpha = 0.18f),
                clip = false,
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.70f),
                        glassTint,
                        Color(0xFFA8D4B9).copy(alpha = 0.18f),
                        Color.White.copy(alpha = 0.34f),
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(850f, 1050f),
                )
            )
            .border(1.dp, borderColor, shape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.34f),
                            Color.White.copy(alpha = 0.08f),
                            Color(0xFFC2E05E).copy(alpha = 0.08f),
                            Color(0xFF2E7E5D).copy(alpha = 0.07f),
                        )
                    )
                )
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.42f),
                            Color.Transparent,
                            Color(0xFFA8D4B9).copy(alpha = 0.20f),
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height * 0.85f),
                    ),
                )
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.18f),
                            Color.Transparent,
                            Color(0xFF6BBF8B).copy(alpha = 0.12f),
                        )
                    ),
                    topLeft = Offset.Zero,
                    size = Size(size.width, size.height),
                )
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.32f),
                            Color.Transparent,
                            Color.Transparent,
                        ),
                        start = Offset(size.width * 0.08f, 0f),
                        end = Offset(size.width * 0.52f, size.height * 0.56f),
                    ),
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.30f),
                    start = Offset(size.width * 0.12f, size.height * 0.08f),
                    end = Offset(size.width * 0.86f, size.height * 0.84f),
                    strokeWidth = 0.8.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.82f),
                    start = Offset(size.width * 0.08f, 1.2f),
                    end = Offset(size.width * 0.92f, 1.2f),
                    strokeWidth = 1.4.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.42f),
                    start = Offset(1.2f, size.height * 0.12f),
                    end = Offset(1.2f, size.height * 0.86f),
                    strokeWidth = 1.2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = Color(0xFF0E5E37).copy(alpha = 0.10f),
                    start = Offset(size.width * 0.12f, size.height - 1.2f),
                    end = Offset(size.width * 0.88f, size.height - 1.2f),
                    strokeWidth = 1.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
            content()
        }
    }
}

@Composable
fun GreenGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) = FlowingGlassBackground(
    modifier = modifier,
    content = content,
)

@Composable
fun FlowingGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "flowing_background")
    val flowA by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "flow_a",
    )
    val flowB by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "flow_b",
    )
    val flowC by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "flow_c",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFFF8FBF4),
                        Color(0xFFEAF5EF),
                        Color(0xFFA8D4B9),
                        Color(0xFFEFF7F2),
                    ),
                    start = Offset.Zero,
                    end = Offset(900f, 1400f),
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val topWave = h * (0.02f + flowA * 0.16f)
            val middleWave = h * (0.28f + flowB * 0.22f)
            val lowerWave = h * (0.58f + flowC * 0.18f)

            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.58f + flowA * 0.08f),
                        Color.Transparent,
                        Color(0xFFC2E05E).copy(alpha = 0.16f + flowB * 0.05f),
                    ),
                    start = Offset(w * (flowB - 0.35f), 0f),
                    end = Offset(w * (0.82f + flowA * 0.45f), h),
                ),
            )

            val upperPath = Path().apply {
                moveTo(-w * 0.24f, topWave)
                cubicTo(w * 0.18f, topWave - h * 0.18f, w * 0.52f, topWave + h * 0.13f, w * 1.24f, topWave - h * 0.04f)
                lineTo(w * 1.24f, topWave + h * 0.30f)
                cubicTo(w * 0.72f, topWave + h * 0.44f, w * 0.34f, topWave + h * 0.10f, -w * 0.24f, topWave + h * 0.34f)
                close()
            }
            drawPath(
                path = upperPath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.62f),
                        Color(0xFFC2E05E).copy(alpha = 0.24f),
                        Color.Transparent,
                    ),
                    start = Offset(w * (flowB - 0.20f), 0f),
                    end = Offset(w * (1.08f - flowA * 0.45f), h * 0.48f),
                ),
            )

            val middlePath = Path().apply {
                moveTo(-w * 0.28f, middleWave)
                cubicTo(w * 0.22f, middleWave + h * 0.22f, w * 0.48f, middleWave - h * 0.20f, w * 1.28f, middleWave + h * 0.03f)
                lineTo(w * 1.28f, middleWave + h * 0.35f)
                cubicTo(w * 0.78f, middleWave + h * 0.16f, w * 0.28f, middleWave + h * 0.44f, -w * 0.28f, middleWave + h * 0.27f)
                close()
            }
            drawPath(
                path = middlePath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.40f),
                        Color(0xFFA8D4B9).copy(alpha = 0.34f),
                    ),
                    start = Offset(w * (-0.20f + flowA * 0.58f), h * 0.18f),
                    end = Offset(w * (1.12f - flowB * 0.20f), h * (0.72f + flowB * 0.18f)),
                ),
            )

            val lowerPath = Path().apply {
                moveTo(-w * 0.22f, lowerWave)
                cubicTo(w * 0.22f, lowerWave - h * 0.14f, w * 0.58f, lowerWave + h * 0.20f, w * 1.22f, lowerWave)
                lineTo(w * 1.22f, h * 1.18f)
                lineTo(-w * 0.22f, h * 1.18f)
                close()
            }
            drawPath(
                path = lowerPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.34f),
                        Color(0xFFF8FBF4).copy(alpha = 0.92f),
                    ),
                    startY = h * 0.58f,
                    endY = h,
                ),
            )

            val brightRibbon = Path().apply {
                moveTo(w * (0.12f + flowA * 0.16f), -h * 0.08f)
                cubicTo(w * (0.04f + flowB * 0.12f), h * 0.22f, w * (0.22f + flowC * 0.18f), h * 0.42f, w * (0.15f + flowA * 0.24f), h * 0.76f)
                lineTo(w * (0.28f + flowA * 0.18f), h * 0.84f)
                cubicTo(w * (0.34f + flowB * 0.10f), h * 0.48f, w * (0.18f + flowC * 0.14f), h * 0.26f, w * (0.30f + flowA * 0.14f), -h * 0.08f)
                close()
            }
            drawPath(
                path = brightRibbon,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.46f),
                        Color(0xFFA8D4B9).copy(alpha = 0.14f),
                        Color.Transparent,
                    ),
                    startY = 0f,
                    endY = h,
                ),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent,
                            Color.White.copy(alpha = 0.26f),
                        )
                    )
                )
        )
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun GlassCardPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(0.5f),
            elevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("Glass Card Content", color = Color.White)
            }
        }
    }
}
