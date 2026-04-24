package com.example.diabetesassistant.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6BFFA0),
    onPrimary = Color(0xFF00391A),
    primaryContainer = Color(0xFF0B5F31),
    onPrimaryContainer = Color(0xFFD9FFE5),
    secondary = Color(0xFF83E8A5),
    onSecondary = Color(0xFF06351A),
    secondaryContainer = Color(0xFF124B2A),
    onSecondaryContainer = Color(0xFFE7FFEF),
    tertiary = Color(0xFF9EF5C1),
    onTertiary = Color(0xFF06351A),
    background = Color(0xFF07140D),
    surface = Color(0xFF10221A),
    surfaceVariant = Color(0xFF21372B),
    onSurface = Color(0xFFEAF7EF),
    onSurfaceVariant = Color(0xFFC4D6CB),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF008A3D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDFBE7),
    onPrimaryContainer = Color(0xFF063B1B),
    secondary = Color(0xFF18A558),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8FFF0),
    onSecondaryContainer = Color(0xFF063B1B),
    tertiary = Color(0xFF69D98A),
    onTertiary = Color.White,
    background = Color(0xFFF1FBF3),
    surface = Color(0xF5FFFFFF),
    surfaceVariant = Color(0xFFE5F4EA),
    onSurface = Color(0xFF16251D),
    onSurfaceVariant = Color(0xFF63746A),
)

@Composable
fun DiabetesAssistantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // 默认关闭动态取色，使用医疗风浅青绿主题；需要系统壁纸取色时可改为 true
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
