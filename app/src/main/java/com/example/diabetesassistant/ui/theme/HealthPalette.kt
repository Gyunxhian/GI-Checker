package com.example.diabetesassistant.ui.theme

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
}
