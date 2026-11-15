package com.example.diabetesassistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.diabetesassistant.ui.theme.HealthPalette
import com.example.diabetesassistant.ui.theme.LocalIsDarkTheme

/**
 * 现代明亮通透的物理毛玻璃容器卡片 (Luminous Frosted Glass Card)。
 * 支持深浅色模式自动自适应（符合 dark-mode-design 规范）：
 * 1. 浅色模式：高亮透光多阶微渐变白底与菲涅尔珠光边框；
 * 2. 深色模式：高级深邃暗绿黑曜石微透光背景与金属哑光微反光边框；
 * 3. 前景内容绝对锐利清晰，零遮挡、高对比度。
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(26.dp),
    elevation: Dp = 3.dp,
    containerColor: Color = HealthPalette.cardBackground(),
    borderColor: Color = HealthPalette.cardBorder(),
    content: @Composable () -> Unit,
) {
    val isDark = LocalIsDarkTheme.current
    val ambientColor = if (isDark) Color.Black.copy(alpha = 0.25f) else HealthPalette.Darkest.copy(alpha = 0.03f)
    val spotColor = if (isDark) Color.Black.copy(alpha = 0.40f) else HealthPalette.Darkest.copy(alpha = 0.06f)

    val bgGradients = if (isDark) {
        listOf(
            containerColor,
            containerColor.copy(alpha = 0.92f),
            Color(0xFF1E2E25).copy(alpha = 0.85f),
        )
    } else {
        listOf(
            containerColor,
            containerColor.copy(alpha = 0.85f),
            Color.White.copy(alpha = 0.95f),
        )
    }

    val borderGradients = if (isDark) {
        listOf(
            borderColor,
            borderColor.copy(alpha = 0.45f),
            Color(0xFF385042).copy(alpha = 0.60f),
        )
    } else {
        listOf(
            borderColor,
            borderColor.copy(alpha = 0.60f),
            Color.White.copy(alpha = 0.90f),
        )
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = ambientColor,
                spotColor = spotColor,
                clip = false,
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = bgGradients,
                    start = Offset(0f, 0f),
                    end = Offset(800f, 1000f),
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = borderGradients,
                    start = Offset(0f, 0f),
                    end = Offset(600f, 800f),
                ),
                shape = shape,
            )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GlassCardPreview() {
    AmbientGlowBackground {
        Box(modifier = Modifier.padding(24.dp)) {
            GlassCard(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "纯净明亮毛玻璃测试",
                    color = HealthPalette.Darkest,
                    modifier = Modifier.padding(24.dp),
                )
            }
        }
    }
}
