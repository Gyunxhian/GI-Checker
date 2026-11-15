package com.example.diabetesassistant.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.diabetesassistant.ui.theme.HealthPalette
import com.example.diabetesassistant.ui.theme.LocalIsDarkTheme

/**
 * 现代 Vita 灵动轻愈风格的动态弥散光晕背景 (Ambient Mesh Aurora Background)。
 * 在底色画板上渲染缓慢流动的薄荷绿、淡薰衣草紫与温暖蜜桃色呼吸光球，
 * 为表层的 AGSL 真实毛玻璃卡片提供丰富的背光与折射色彩。
 */
@Composable
fun AmbientGlowBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambientGlow")

    // 缓慢流动的相位偏移 (14秒柔和周期)
    val phaseA by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "phaseA",
    )

    val phaseB by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "phaseB",
    )

    val isDark = LocalIsDarkTheme.current
    val canvasBg = HealthPalette.canvasBackground(isDark)
    val auraMint = HealthPalette.auraMint(isDark)
    val auraLavender = HealthPalette.auraLavender(isDark)
    val auraPeach = HealthPalette.auraPeach(isDark)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(canvasBg)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // 1. 顶部流动光团：柔和薄荷青光晕
            val mintCenter = Offset(
                x = canvasW * (0.25f + phaseA * 0.25f),
                y = canvasH * (0.12f + phaseB * 0.10f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        auraMint,
                        auraMint.copy(alpha = auraMint.alpha * 0.40f),
                        Color.Transparent,
                    ),
                    center = mintCenter,
                    radius = canvasW * 0.65f,
                ),
                center = mintCenter,
                radius = canvasW * 0.65f,
            )

            // 2. 右侧居中流动光团：淡薰衣草紫光晕
            val lavenderCenter = Offset(
                x = canvasW * (0.85f - phaseB * 0.20f),
                y = canvasH * (0.38f + phaseA * 0.15f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        auraLavender,
                        auraLavender.copy(alpha = auraLavender.alpha * 0.35f),
                        Color.Transparent,
                    ),
                    center = lavenderCenter,
                    radius = canvasW * 0.70f,
                ),
                center = lavenderCenter,
                radius = canvasW * 0.70f,
            )

            // 3. 底部温暖光团：柔蜜桃/暖杏光晕
            val peachCenter = Offset(
                x = canvasW * (0.20f + phaseB * 0.35f),
                y = canvasH * (0.75f - phaseA * 0.12f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        auraPeach,
                        auraPeach.copy(alpha = auraPeach.alpha * 0.30f),
                        Color.Transparent,
                    ),
                    center = peachCenter,
                    radius = canvasW * 0.75f,
                ),
                center = peachCenter,
                radius = canvasW * 0.75f,
            )
        }

        content()
    }
}
