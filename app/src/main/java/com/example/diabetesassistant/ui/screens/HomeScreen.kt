package com.example.diabetesassistant.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.diabetesassistant.ui.theme.LocalIsDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File
import com.example.diabetesassistant.camera.AnalysisResultJson
import com.example.diabetesassistant.data.local.entity.FoodRecord
import com.example.diabetesassistant.data.local.preferences.UserPreferencesDataSource
import com.example.diabetesassistant.data.repository.FoodRecordRepository
import com.example.diabetesassistant.ui.components.AmbientGlowBackground
import com.example.diabetesassistant.ui.components.GlassCard
import com.example.diabetesassistant.ui.theme.HealthPalette
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class MetabolicRiskStatus(val title: String, val badgeColor: Color, val badgeBg: Color) {
    NO_DATA("待记录", Color(0xFF6B7280), Color(0xFFEBEFEB)),
    OPTIMAL("平稳达标", HealthPalette.Dark, HealthPalette.PastelMint),
    ELEVATED("偏高警戒", Color(0xFFE76F51), HealthPalette.PastelButter),
    SPIKE_RISK("高负荷预警", HealthPalette.Danger, HealthPalette.PastelPeach),
}

data class HomeDashboardUiState(
    val todayRecords: List<FoodRecord> = emptyList(),
    val todayCarbIntake: Float = 0f,
    val todayCalories: Float = 0f,
    val todayFiber: Float = 0f,
    val todayMealCount: Int = 0,
    val dailyCarbTarget: Float = 130f,
    val latestRecord: FoodRecord? = null,
    val latestAnalysis: AnalysisResultJson? = null,
    val latestPeakValue: Float = 0f,
    val latestPeakMinute: Int = 0,
    val riskStatus: MetabolicRiskStatus = MetabolicRiskStatus.NO_DATA,
    val aiCoachAdvice: String = "拍摄餐盘，AI 将即刻为您预测餐后血糖波动曲线与重点时段。",
    val recent7DaysTrend: List<Float> = emptyList(),
    val weeklyComplianceRate: Int = 100,
)

@HiltViewModel
class HomeDashboardViewModel @Inject constructor(
    foodRecordRepository: FoodRecordRepository,
    userPreferencesDataSource: UserPreferencesDataSource,
) : ViewModel() {
    private val gson = Gson()

    val uiState: StateFlow<HomeDashboardUiState> = combine(
        foodRecordRepository.observeAll(),
        userPreferencesDataSource.dailyCarbTargetFlow,
    ) { records, target ->
        buildDashboardState(records, target)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeDashboardUiState(),
    )

    private fun buildDashboardState(records: List<FoodRecord>, target: Float): HomeDashboardUiState {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        val todayRecords = records.filter { it.recordDate >= startOfDay }
        val latest = records.firstOrNull()

        var todayCarbs = 0f
        var todayCalories = 0f
        var todayFiber = 0f

        todayRecords.forEach { rec ->
            try {
                val parsed = gson.fromJson(rec.aiJson, AnalysisResultJson::class.java)
                if (parsed != null) {
                    val carbs = parsed.totalCarbIntake
                    todayCarbs += carbs
                    todayCalories += carbs * 4f * 1.8f + 80f
                    todayFiber += carbs * 0.12f
                }
            } catch (_: Exception) {}
        }

        val latestParsed = latest?.let { rec ->
            try {
                gson.fromJson(rec.aiJson, AnalysisResultJson::class.java)
            } catch (_: Exception) {
                null
            }
        }

        val peakVal = latestParsed?.curvePoints?.maxOfOrNull { it.bloodSugar } ?: 0f
        val peakMin = latestParsed?.curvePoints?.maxByOrNull { it.bloodSugar }?.minute ?: 0

        val risk = when {
            records.isEmpty() -> MetabolicRiskStatus.NO_DATA
            peakVal >= 10.0f -> MetabolicRiskStatus.SPIKE_RISK
            peakVal >= 7.8f -> MetabolicRiskStatus.ELEVATED
            else -> MetabolicRiskStatus.OPTIMAL
        }

        val advice = latestParsed?.reminders?.firstOrNull()?.takeIf { it.isNotBlank() }
            ?: when (risk) {
                MetabolicRiskStatus.NO_DATA -> "拍摄餐盘，AI 将即刻为您预测餐后血糖波动曲线与吸收峰值。"
                MetabolicRiskStatus.SPIKE_RISK -> "最近一餐升糖较快，建议餐后 30 分钟慢走 15 分钟促进血糖利用。"
                MetabolicRiskStatus.ELEVATED -> "碳水负荷稍偏高，建议下一餐增加绿叶蔬菜与优质蛋白。"
                MetabolicRiskStatus.OPTIMAL -> "上一餐碳水配比合理，升糖曲线平缓，继续保持健康餐盘搭配！"
            }

        // 计算近 7 天每日真实碳水走势 (无数据填 0f，无虚假 mock)
        val trendPoints = mutableListOf<Float>()
        val oneDayMillis = 86400000L
        for (i in 6 downTo 0) {
            val dayStart = startOfDay - i * oneDayMillis
            val dayEnd = dayStart + oneDayMillis
            val dayRecords = records.filter { it.recordDate in dayStart until dayEnd }
            var dayCarbs = 0f
            dayRecords.forEach { r ->
                try {
                    val p = gson.fromJson(r.aiJson, AnalysisResultJson::class.java)
                    dayCarbs += (p?.totalCarbIntake ?: 0f)
                } catch (_: Exception) {}
            }
            trendPoints.add(dayCarbs)
        }

        val totalRecorded = records.size
        val optimalCount = records.count { r ->
            try {
                val p = gson.fromJson(r.aiJson, AnalysisResultJson::class.java)
                val pk = p?.curvePoints?.maxOfOrNull { it.bloodSugar } ?: 0f
                pk < 8.5f
            } catch (_: Exception) {
                true
            }
        }
        val complianceRate = if (totalRecorded > 0) ((optimalCount.toFloat() / totalRecorded) * 100).toInt() else 100

        return HomeDashboardUiState(
            todayRecords = todayRecords,
            todayCarbIntake = todayCarbs,
            todayCalories = todayCalories,
            todayFiber = todayFiber,
            todayMealCount = todayRecords.size,
            dailyCarbTarget = target,
            latestRecord = latest,
            latestAnalysis = latestParsed,
            latestPeakValue = peakVal,
            latestPeakMinute = peakMin,
            riskStatus = risk,
            aiCoachAdvice = advice,
            recent7DaysTrend = trendPoints,
            weeklyComplianceRate = complianceRate,
        )
    }
}

/**
 * 现代 Bento Box（快餐盒 / 便当盒式分区容器）灵动健康看板
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    guardianDays: Int = 1,
    welcomeTrigger: Int = 0,
    onScanClick: () -> Unit = {},
    onMealClick: (Long) -> Unit = {},
    onViewArchiveClick: () -> Unit = {},
    onAiAssistantClick: () -> Unit = {},
    viewModel: HomeDashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AmbientGlowBackground(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 95.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // 1. 顶部便当盒状态区 (Header & AI Quick Access)
            item {
                BentoHeaderSection(
                    guardianDays = guardianDays,
                    status = state.riskStatus,
                    onAiAssistantClick = onAiAssistantClick,
                    onScanClick = onScanClick,
                )
            }

            // 2. Bento 主格 (Hero Container): 今日碳水达标与配额大圆环
            item {
                BentoHeroRingContainer(
                    todayCarbs = state.todayCarbIntake,
                    targetCarbs = state.dailyCarbTarget,
                    mealCount = state.todayMealCount,
                )
            }

            // 3. Bento 并排双副格 (Side-by-Side Dual Bento Boxes): 热量宏量格 + 7日平稳走势格
            item {
                BentoDualSideBySideRow(
                    calories = state.todayCalories,
                    fiber = state.todayFiber,
                    trendData = state.recent7DaysTrend,
                    complianceRate = state.weeklyComplianceRate,
                    targetCarbs = state.dailyCarbTarget,
                )
            }

            // 4. Bento 快餐盒餐具工具区 (Horizontal Quick Action Pills)
            item {
                BentoActionPillsRow(
                    onScanClick = onScanClick,
                    onAiAssistantClick = onAiAssistantClick,
                    onViewArchiveClick = onViewArchiveClick,
                )
            }

            // 5. Bento 餐盘小吃格 (Recent Meal & Today Tray)
            item {
                BentoMealsTrayContainer(
                    todayRecords = state.todayRecords,
                    latestRecord = state.latestRecord,
                    latestAnalysis = state.latestAnalysis,
                    onMealClick = onMealClick,
                    onScanClick = onScanClick,
                    onViewArchiveClick = onViewArchiveClick,
                )
            }

            // 6. Bento 甜品格 (GI 贴士与智能建议交互卡)
            item {
                BentoAdviceContainer(
                    advice = state.aiCoachAdvice,
                    onAiAssistantClick = onAiAssistantClick,
                )
            }
        }
    }
}

/**
 * 顶部导航与问候区
 */
@Composable
private fun BentoHeaderSection(
    guardianDays: Int,
    status: MetabolicRiskStatus,
    onAiAssistantClick: () -> Unit,
    onScanClick: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseGlow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseAlpha",
    )

    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = when (hour) {
        in 5..11 -> "早安，生活家 ☀️"
        in 12..17 -> "午后好，保持平稳 🌿"
        else -> "夜晚安宁，代谢恢复中 🌙"
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // 第一行：左侧代谢达标状态微标 + 右侧 GI 助手发光快捷胶囊
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = status.badgeBg,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(status.badgeColor)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = status.title,
                        fontSize = 11.sp,
                        color = status.badgeColor,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = HealthPalette.chipSurface(),
                shadowElevation = 2.dp,
                modifier = Modifier.clickable { onAiAssistantClick() },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(HealthPalette.pastelLavender()),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "GI 助手",
                            tint = HealthPalette.Secondary,
                            modifier = Modifier.size(13.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GI 助手",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = HealthPalette.textPrimary(),
                        maxLines = 1,
                        softWrap = false,
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(HealthPalette.Secondary.copy(alpha = pulseAlpha))
                    )
                }
            }
        }

        // 第二行：全宽问候语与守护天数，零挤压、不换行
        Column {
            Text(
                text = greeting,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = HealthPalette.textPrimary(),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "已私有守护控糖 $guardianDays 天 · 指标平稳运行",
                fontSize = 12.sp,
                color = HealthPalette.textSecondary(),
            )
        }
    }
}

/**
 * Bento 主食格 (Hero Ring Container): 今日碳水达标环形进度
 */
@Composable
private fun BentoHeroRingContainer(
    todayCarbs: Float,
    targetCarbs: Float,
    mealCount: Int,
) {
    val progress = if (targetCarbs > 0f) (todayCarbs / targetCarbs).coerceIn(0f, 1f) else 0f
    val remaining = (targetCarbs - todayCarbs).coerceAtLeast(0f)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        elevation = 5.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 左侧环形进度表 (清晰锐利)
            Box(
                modifier = Modifier.size(105.dp),
                contentAlignment = Alignment.Center,
            ) {
                val trackColor = HealthPalette.chipBorder()
                val isDark = LocalIsDarkTheme.current
                Canvas(modifier = Modifier.size(92.dp)) {
                    val strokeW = 10.dp.toPx()
                    val diameter = size.minDimension - strokeW
                    val topLeft = Offset(strokeW / 2f, strokeW / 2f)
                    val arcSize = Size(diameter, diameter)

                    // 背景导轨环
                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round),
                    )

                    // 动态渐变进度环
                    val sweep = progress * 360f
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = if (isDark) {
                                listOf(
                                    HealthPalette.Primary,
                                    HealthPalette.Accent,
                                    HealthPalette.Secondary,
                                    HealthPalette.Primary,
                                )
                            } else {
                                listOf(
                                    HealthPalette.Secondary,
                                    HealthPalette.Accent,
                                    HealthPalette.Primary,
                                    HealthPalette.Secondary,
                                )
                            }
                        ),
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round),
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = HealthPalette.textPrimary(),
                    )
                    Text(
                        text = "已消耗",
                        fontSize = 10.sp,
                        color = HealthPalette.textMuted(),
                    )
                }
            }

            Spacer(modifier = Modifier.width(18.dp))

            // 右侧文字指标格
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "今日吸收碳水",
                        fontSize = 13.sp,
                        color = HealthPalette.textSecondary(),
                        fontWeight = FontWeight.Medium,
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = HealthPalette.chipSurface(),
                    ) {
                        Text(
                            text = "已记 $mealCount 餐",
                            fontSize = 10.sp,
                            color = HealthPalette.textPrimary(),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f", todayCarbs),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = HealthPalette.textPrimary(),
                    )
                    Text(
                        text = " / %.0fg".format(targetCarbs),
                        fontSize = 14.sp,
                        color = HealthPalette.textMuted(),
                        modifier = Modifier.padding(bottom = 4.dp, start = 2.dp),
                    )
                }

                // 剩余预算胶囊
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = HealthPalette.chipSurface(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "剩余配额预算",
                            fontSize = 11.sp,
                            color = HealthPalette.textSecondary(),
                        )
                        Text(
                            text = "%.1fg".format(remaining),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (remaining > 0f) HealthPalette.textPrimary() else HealthPalette.Danger,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Bento 并排双副格 (Side-by-Side Dual Bento Boxes)
 * 参考 Mental Health 与 GI 灵动风格的双列容器
 */
@Composable
private fun BentoDualSideBySideRow(
    calories: Float,
    fiber: Float,
    trendData: List<Float>,
    complianceRate: Int,
    targetCarbs: Float,
) {
    val isDark = LocalIsDarkTheme.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 左便当格：预估热量与宏量营养素 (淡暖杏色背景)
        GlassCard(
            modifier = Modifier
                .weight(1f)
                .height(138.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = 4.dp,
            containerColor = HealthPalette.pastelButter(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(HealthPalette.chipSurface()),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalFireDepartment,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFFFFB74D) else Color(0xFF996515),
                                modifier = Modifier.size(14.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "预估热量",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthPalette.textPrimary(),
                        )
                    }
                }

                Column {
                    Text(
                        text = "%.0f".format(calories),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = HealthPalette.textPrimary(),
                    )
                    Text(
                        text = "kcal 能量摄入",
                        fontSize = 10.sp,
                        color = HealthPalette.textMuted(),
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HealthPalette.chipSurface(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Spa,
                            contentDescription = null,
                            tint = if (isDark) HealthPalette.Primary else HealthPalette.Dark,
                            modifier = Modifier.size(10.dp),
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "纤维 %.1fg".format(fiber),
                            fontSize = 10.sp,
                            color = HealthPalette.textPrimary(),
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }

        // 右便当格：7日平稳微走势 (淡薄荷色背景)
        GlassCard(
            modifier = Modifier
                .weight(1f)
                .height(138.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = 4.dp,
            containerColor = HealthPalette.pastelMint(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(HealthPalette.chipSurface()),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = if (isDark) HealthPalette.Primary else HealthPalette.Dark,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "7日平稳度",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthPalette.textPrimary(),
                        )
                    }
                }

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$complianceRate%",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = HealthPalette.textPrimary(),
                    )
                    Text(
                        text = " 达标率",
                        fontSize = 10.sp,
                        color = HealthPalette.textMuted(),
                        modifier = Modifier.padding(bottom = 2.dp),
                    )
                }

                // 嵌入微波浪 Sparkline
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        if (trendData.isEmpty()) return@Canvas

                        val maxVal = (trendData.maxOrNull() ?: targetCarbs).coerceAtLeast(targetCarbs) * 1.15f
                        val minVal = 0f
                        val w = size.width
                        val h = size.height
                        val stepX = w / (trendData.size - 1).coerceAtLeast(1)

                        val points = trendData.mapIndexed { index, value ->
                            val ratio = ((value - minVal) / (maxVal - minVal)).coerceIn(0f, 1f)
                            Offset(x = index * stepX, y = h - ratio * h * 0.85f - 2f)
                        }

                        val strokePath = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                val prev = points[i - 1]
                                val curr = points[i]
                                val cx = (prev.x + curr.x) / 2f
                                cubicTo(cx, prev.y, cx, curr.y, curr.x, curr.y)
                            }
                        }

                        drawPath(
                            path = strokePath,
                            color = if (isDark) HealthPalette.Primary else HealthPalette.Secondary,
                            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Bento 快餐盒操作胶囊区 (Quick Action Pills Row)
 */
@Composable
private fun BentoActionPillsRow(
    onScanClick: () -> Unit,
    onAiAssistantClick: () -> Unit,
    onViewArchiveClick: () -> Unit,
) {
    val isDark = LocalIsDarkTheme.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 主操作：拍照测算 (深青色高反差胶囊)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isDark) HealthPalette.Primary else HealthPalette.Secondary,
            shadowElevation = 3.dp,
            modifier = Modifier
                .weight(1.2f)
                .clickable { onScanClick() },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFF0E1612) else Color.White,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "拍餐盘测糖",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFF0E1612) else Color.White,
                )
            }
        }

        // 次操作：问 GI 助手 (淡薰衣草紫胶囊)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = HealthPalette.pastelLavender(),
            border = androidx.compose.foundation.BorderStroke(1.dp, HealthPalette.chipBorder()),
            shadowElevation = 2.dp,
            modifier = Modifier
                .weight(1.1f)
                .clickable { onAiAssistantClick() },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Psychology,
                    contentDescription = null,
                    tint = if (isDark) HealthPalette.Primary else HealthPalette.Dark,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "问 GI 助手",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = HealthPalette.textPrimary(),
                )
            }
        }

        // 次操作：看统计看板 (柔白毛玻璃胶囊)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = HealthPalette.cardBackground(),
            border = androidx.compose.foundation.BorderStroke(1.dp, HealthPalette.cardBorder()),
            shadowElevation = 2.dp,
            modifier = Modifier
                .weight(1f)
                .clickable { onViewArchiveClick() },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Insights,
                    contentDescription = null,
                    tint = if (isDark) HealthPalette.Primary else HealthPalette.Dark,
                    modifier = Modifier.size(15.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "统计看板",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = HealthPalette.textPrimary(),
                )
            }
        }
    }
}

/**
 * Bento 餐盘格子托盘 (Meals Tray Container)
 */
@Composable
private fun BentoMealsTrayContainer(
    todayRecords: List<FoodRecord>,
    latestRecord: FoodRecord?,
    latestAnalysis: AnalysisResultJson?,
    onMealClick: (Long) -> Unit,
    onScanClick: () -> Unit,
    onViewArchiveClick: () -> Unit,
) {
    val isDark = LocalIsDarkTheme.current
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        elevation = 5.dp,
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(HealthPalette.chipSurface()),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Restaurant,
                            contentDescription = null,
                            tint = if (isDark) HealthPalette.Primary else HealthPalette.Dark,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "今日餐盘便当格子",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = HealthPalette.textPrimary(),
                    )
                }
                Text(
                    text = "全部记录 ->",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) HealthPalette.Primary else HealthPalette.Secondary,
                    modifier = Modifier.clickable { onViewArchiveClick() },
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (todayRecords.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = HealthPalette.chipSurface(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HealthPalette.chipBorder()),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onScanClick() },
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(HealthPalette.pastelMint()),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = null,
                                tint = if (isDark) HealthPalette.Primary else HealthPalette.Dark,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "今日暂无用餐记录",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = HealthPalette.textPrimary(),
                            )
                            Text(
                                text = "拍下餐盘，即刻为您测算餐后血糖波动",
                                fontSize = 11.sp,
                                color = HealthPalette.textMuted(),
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = if (isDark) HealthPalette.Primary else HealthPalette.Secondary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(todayRecords) { record ->
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = HealthPalette.chipSurface(),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HealthPalette.chipBorder()),
                            modifier = Modifier
                                .width(148.dp)
                                .clickable { onMealClick(record.id) },
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                val imageList = remember(record.imagePath) {
                                    record.imagePath.split("|").map { it.trim() }.filter { it.isNotBlank() }
                                }
                                val firstImage = imageList.firstOrNull().orEmpty()

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(84.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(HealthPalette.cardBorder()),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (firstImage.isNotBlank()) {
                                        var isImageError by remember(firstImage) { mutableStateOf(false) }

                                        if (!isImageError) {
                                            val imageModel = remember(firstImage) {
                                                if (firstImage.startsWith("/")) File(firstImage) else firstImage
                                            }
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(imageModel)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = record.dishName,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize(),
                                                onError = { isImageError = true },
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Filled.Restaurant,
                                                contentDescription = null,
                                                tint = HealthPalette.textMuted(),
                                                modifier = Modifier.size(26.dp),
                                            )
                                        }

                                        if (imageList.size > 1) {
                                            Surface(
                                                shape = RoundedCornerShape(bottomStart = 6.dp, topEnd = 6.dp),
                                                color = Color.Black.copy(alpha = 0.65f),
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .padding(3.dp),
                                            ) {
                                                Text(
                                                    text = "${imageList.size}图",
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                )
                                            }
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.Restaurant,
                                            contentDescription = null,
                                            tint = HealthPalette.textMuted(),
                                            modifier = Modifier.size(26.dp),
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = record.dishName.ifBlank { "餐盘记录" },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = HealthPalette.textPrimary(),
                                )

                                val timeStr = remember(record.recordDate) {
                                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(record.recordDate))
                                }
                                Text(
                                    text = timeStr,
                                    fontSize = 11.sp,
                                    color = HealthPalette.textMuted(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Bento 甜品格：GI 贴士与智能建议交互卡
 */
@Composable
private fun BentoAdviceContainer(
    advice: String,
    onAiAssistantClick: () -> Unit,
) {
    val isDark = LocalIsDarkTheme.current
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAiAssistantClick() },
        shape = RoundedCornerShape(26.dp),
        elevation = 4.dp,
        containerColor = HealthPalette.pastelLavender(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(HealthPalette.chipSurface()),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = if (isDark) HealthPalette.Primary else HealthPalette.Dark,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "GI 代谢贴士",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = HealthPalette.textPrimary(),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = HealthPalette.chipSurface(),
                    ) {
                        Text(
                            text = "智能解惑",
                            fontSize = 9.sp,
                            color = HealthPalette.textPrimary(),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = advice,
                    fontSize = 12.sp,
                    color = HealthPalette.textSecondary(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp,
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "向顾问提问",
                tint = if (isDark) HealthPalette.Primary else HealthPalette.Secondary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
