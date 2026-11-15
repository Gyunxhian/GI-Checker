package com.example.diabetesassistant.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 文档规定的 6 色主题色板（implementation_plan.md V4）
 * 不得在此文件之外硬编码这些颜色值，统一引用此对象。
 */
object HealthPalette {
    /** 强调色 — 黄绿高亮 */
    val Accent = Color(0xFFC2E05E)

    /** 浅背景色 — 薄荷绿 */
    val LightBackground = Color(0xFFA8D4B9)

    /** 主色 — 清新绿 */
    val Primary = Color(0xFF6BBF8B)

    /** 次主色 — 中绿 */
    val Secondary = Color(0xFF4B9B6E)

    /** 深色 — 深绿 */
    val Dark = Color(0xFF2E7E5D)

    /** 最深色 — 最深绿 */
    val Darkest = Color(0xFF1A5F3A)

    // ─── 辅助语义色 ───────────────────────────────────────────────
    /** 危险 / 高血糖提示 */
    val Danger = Color(0xFFFF5353)

    /** 警告 / 偏高血糖提示 */
    val Warning = Color(0xFFF4C84E)

    /** 低血糖提示 */
    val LowGlucose = Color(0xFF3E8BFF)

    // ─── 胰岛素色带三色 ───────────────────────────────────────────
    /** 色带：偏少（蓝） */
    val InsulinLow = Color(0xFF5B9BF0)

    /** 色带：适中（绿） */
    val InsulinMid = Color(0xFF6BBF8B)

    /** 色带：偏多（红） */
    val InsulinHigh = Color(0xFFFF6B6B)

    // ─── 玻璃拟态通用 ─────────────────────────────────────────────
    val GlassWhite = Color(0xB3FFFFFF)       // 70% 白
    val GlassBorder = Color(0xDDFFFFFF)      // 87% 白边框

    // ─── 现代 GI 灵动轻愈色板体系 (基于 color-system) ──────────
    /** 基础画布底色：极其柔和清爽的浅白绿 */
    val SurfaceCanvas = Color(0xFFF7FAF8)

    /** 弥散背景光晕：清新薄荷绿 */
    val AuraMint = Color(0xFFD6F0DF)

    /** 弥散背景光晕：淡薰衣草紫 */
    val AuraLavender = Color(0xFFEADBFA)

    /** 弥散背景光晕：轻柔暖蜜桃杏色 */
    val AuraPeach = Color(0xFFFDE6D8)

    /** 马卡龙粉彩：柔粉（低血糖/重点注意） */
    val PastelPeach = Color(0xFFFBE3D8)

    /** 马卡龙粉彩：淡薰衣草（AI 助手/心理状态） */
    val PastelLavender = Color(0xFFEBE4F8)

    /** 马卡龙粉彩：暖黄杏（碳水/热量指标） */
    val PastelButter = Color(0xFFFCF4D9)

    /** 马卡龙粉彩：薄荷浅绿（平稳达标标签） */
    val PastelMint = Color(0xFFE3F5EB)

    /** 马卡龙粉彩：冰川柔蓝（水分/静息） */
    val PastelBlue = Color(0xFFE2EDFC)

    /** 深海青色：沉稳高反差药丸背景 */
    val DeepPetrol = Color(0xFF0F4C5C)

    /** 柔和卡片边框：微带珠光白 */
    val FrostedBorder = Color(0xEEFFFFFF)

    /** 真实毛玻璃基底浅调 */
    val FrostedTint = Color(0xD8FFFFFF)

    // ─── 动态深浅色响应式语义色彩 (遵循 dark-mode-design 规范) ───────
    @Composable
    fun textPrimary(isDark: Boolean = LocalIsDarkTheme.current): Color =
        if (isDark) Color(0xFFEDF5F0) else Color(0xFF14241B)

    @Composable
    fun textSecondary(isDark: Boolean = LocalIsDarkTheme.current): Color =
        if (isDark) Color(0xFFA0B5A7) else Color(0xFF485D51)

    @Composable
    fun textMuted(isDark: Boolean = LocalIsDarkTheme.current): Color =
        if (isDark) Color(0xFF708678) else Color(0xFF7E9486)

    @Composable
    fun canvasBackground(isDark: Boolean = LocalIsDarkTheme.current): Color =
        if (isDark) Color(0xFF0E1612) else Color(0xFFF7FAF8)

    @Composable
    fun cardBackground(isDark: Boolean = LocalIsDarkTheme.current): Color =
        if (isDark) Color(0xFF16231C).copy(alpha = 0.90f) else Color.White.copy(alpha = 0.94f)

    @Composable
    fun cardBorder(isDark: Boolean = LocalIsDarkTheme.current): Color =
        if (isDark) Color(0xFF2C3E33).copy(alpha = 0.70f) else Color.White.copy(alpha = 0.95f)

    @Composable
    fun chipSurface(isDark: Boolean = LocalIsDarkTheme.current): Color =
        if (isDark) Color(0xFF1E2D24) else Color.White.copy(alpha = 0.92f)

    @Composable
    fun chipBorder(isDark: Boolean = LocalIsDarkTheme.current): Color =
        if (isDark) Color(0xFF2E4236) else Color(0xFFE4EDE7)

    @Composable
    fun pastelMint(isDark: Boolean = LocalIsDarkTheme.current): Color =
        if (isDark) Color(0xFF183324) else Color(0xFFE3F5EB)

    @Composable
    fun pastelLavender(isDark: Boolean = LocalIsDarkTheme.current): Color =
        if (isDark) Color(0xFF2B203C) else Color(0xFFEBE4F8)

    @Composable
    fun pastelButter(isDark: Boolean = LocalIsDarkTheme.current): Color =
        if (isDark) Color(0xFF332A17) else Color(0xFFFCF4D9)

    @Composable
    fun pastelBlue(isDark: Boolean = LocalIsDarkTheme.current): Color =
        if (isDark) Color(0xFF18293B) else Color(0xFFE2EDFC)

    @Composable
    fun pastelPeach(isDark: Boolean = LocalIsDarkTheme.current): Color =
        if (isDark) Color(0xFF38221A) else Color(0xFFFBE3D8)

    @Composable
    fun auraMint(isDark: Boolean = LocalIsDarkTheme.current): Color =
        if (isDark) Color(0xFF153322).copy(alpha = 0.50f) else Color(0xFFD6F0DF).copy(alpha = 0.65f)

    @Composable
    fun auraLavender(isDark: Boolean = LocalIsDarkTheme.current): Color =
        if (isDark) Color(0xFF241635).copy(alpha = 0.40f) else Color(0xFFEADBFA).copy(alpha = 0.60f)

    @Composable
    fun auraPeach(isDark: Boolean = LocalIsDarkTheme.current): Color =
        if (isDark) Color(0xFF2E1A14).copy(alpha = 0.35f) else Color(0xFFFDE6D8).copy(alpha = 0.50f)
}
