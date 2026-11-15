package com.example.diabetesassistant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 全局深色模式状态 CompositionLocal，供全量组件及色板透明访问
 */
val LocalIsDarkTheme = staticCompositionLocalOf { false }

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

// ─── 浅色方案（高对比医疗生命绿） ─────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF227B4E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8F3ED),
    onPrimaryContainer = Color(0xFF14241B),
    secondary = Color(0xFF2E86C1),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE1F0FA),
    onSecondaryContainer = Color(0xFF1B4965),
    tertiary = Color(0xFF82B53F),
    onTertiary = Color.White,
    background = Color(0xFFF6F9F7),
    onBackground = Color(0xFF14241B),
    surface = Color.White,
    onSurface = Color(0xFF14241B),
    surfaceVariant = Color(0xFFE8F3ED),
    onSurfaceVariant = Color(0xFF485D51),
    outline = Color(0xFF227B4E).copy(alpha = 0.22f),
    error = HealthPalette.Danger,
    onError = Color.White,
)

// ─── 深色方案（高对比低眩光暗墨绿） ──────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4CAF7A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF203127),
    onPrimaryContainer = Color(0xFFEBF7F0),
    secondary = Color(0xFF64B5F6),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1A3344),
    onSecondaryContainer = Color(0xFFBCE0FD),
    tertiary = Color(0xFFA8D468),
    onTertiary = Color(0xFF16231C),
    background = Color(0xFF0E1612),
    onBackground = Color(0xFFEBF7F0),
    surface = Color(0xFF16231C),
    onSurface = Color(0xFFEBF7F0),
    surfaceVariant = Color(0xFF203127),
    onSurfaceVariant = Color(0xFFA3BEAF),
    outline = Color(0xFFA3BEAF).copy(alpha = 0.25f),
    error = Color(0xFFE57373),
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

    CompositionLocalProvider(LocalIsDarkTheme provides useDark) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}
