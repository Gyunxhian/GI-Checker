package com.example.diabetesassistant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 外观模式枚举（三选一，存储于 DataStore）
 * implementation_plan.md: "深色模式：三选一：跟随系统/浅色/深色"
 */
enum class AppThemeMode {
    /** 跟随系统 */
    FOLLOW_SYSTEM,

    /** 强制浅色 */
    LIGHT,

    /** 强制深色 */
    DARK,
}

// ─── 浅色方案（基于文档 6 色） ─────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary = HealthPalette.Dark,                   // #2E7E5D 主交互色
    onPrimary = Color.White,
    primaryContainer = HealthPalette.LightBackground, // #A8D4B9
    onPrimaryContainer = HealthPalette.Darkest,       // #1A5F3A
    secondary = HealthPalette.Secondary,              // #4B9B6E
    onSecondary = Color.White,
    secondaryContainer = HealthPalette.Accent.copy(alpha = 0.25f), // #C2E05E 25%
    onSecondaryContainer = HealthPalette.Darkest,
    tertiary = HealthPalette.Accent,                  // #C2E05E 强调
    onTertiary = HealthPalette.Darkest,
    background = Color(0xFFF4FAF6),                   // 极浅绿白底
    onBackground = Color(0xFF15231B),
    surface = Color(0xFFF8FCFA),
    onSurface = Color(0xFF15231B),
    surfaceVariant = HealthPalette.LightBackground.copy(alpha = 0.45f),
    onSurfaceVariant = Color(0xFF3E5448),
    outline = HealthPalette.Primary.copy(alpha = 0.35f),
    error = HealthPalette.Danger,
    onError = Color.White,
)

// ─── 深色方案（保持绿色调，背景深灰绿） ──────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary = HealthPalette.Primary,                  // #6BBF8B
    onPrimary = HealthPalette.Darkest,
    primaryContainer = HealthPalette.Secondary,        // #4B9B6E
    onPrimaryContainer = HealthPalette.Accent,         // #C2E05E
    secondary = HealthPalette.LightBackground,         // #A8D4B9
    onSecondary = HealthPalette.Darkest,
    secondaryContainer = HealthPalette.Dark,            // #2E7E5D
    onSecondaryContainer = HealthPalette.LightBackground,
    tertiary = HealthPalette.Accent,
    onTertiary = HealthPalette.Darkest,
    background = Color(0xFF0B1610),                    // 极深绿黑底
    onBackground = Color(0xFFD8F0E4),
    surface = Color(0xFF111F18),
    onSurface = Color(0xFFD8F0E4),
    surfaceVariant = Color(0xFF1E2F26),
    onSurfaceVariant = Color(0xFFA8CCBA),
    outline = HealthPalette.Dark.copy(alpha = 0.55f),
    error = HealthPalette.Danger,
    onError = Color.White,
)

/**
 * 全局主题入口。
 *
 * @param themeMode 外观模式（三选一，由 DataStore 偏好控制）
 * @param content   子组合树
 */
@Composable
fun DiabetesAssistantTheme(
    themeMode: AppThemeMode = AppThemeMode.FOLLOW_SYSTEM,
    content: @Composable () -> Unit,
) {
    val useDark = when (themeMode) {
        AppThemeMode.FOLLOW_SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val colorScheme = if (useDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
