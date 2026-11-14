package com.example.diabetesassistant.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.example.diabetesassistant.camera.AnalysisResultJson
import com.example.diabetesassistant.data.local.entity.FoodRecord
import com.example.diabetesassistant.data.local.preferences.UserPreferencesDataSource
import com.example.diabetesassistant.data.repository.FoodRecordRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

// 现代代谢健康科技风色彩（Levels / Lumen 风格）
private val MetabolicBg = Color(0xFFF7FAF8)
private val MetabolicForest = Color(0xFF144D30)
private val MetabolicGreen = Color(0xFF227B4E)
private val MetabolicMint = Color(0xFF52B788)
private val MetabolicLightMint = Color(0xFFE8F5EE)
private val MetabolicBorder = Color(0xFFDCE6DF)
private val MetabolicTextDark = Color(0xFF1A2620)
private val MetabolicTextMuted = Color(0xFF4A5568)
private val StatusWarningAmber = Color(0xFFE76F51)
private val StatusRiskRed = Color(0xFFD62828)

enum class MetabolicRiskStatus(val title: String, val badgeColor: Color, val badgeBg: Color) {
    NO_DATA("待记录", MetabolicTextMuted, Color(0xFFEBEFEB)),
    OPTIMAL("平稳达标", MetabolicGreen, MetabolicLightMint),
    ELEVATED("偏高警戒", StatusWarningAmber, Color(0xFFFDF0ED)),
    SPIKE_RISK("高负荷预警", StatusRiskRed, Color(0xFFFBEAEA)),
}

data class HomeDashboardUiState(
    val todayRecords: List<FoodRecord> = emptyList(),
    val todayCarbIntake: Float = 0f,
    val todayMealCount: Int = 0,
    val dailyCarbTarget: Float = 130f,
    val latestRecord: FoodRecord? = null,
    val latestAnalysis: AnalysisResultJson? = null,
    val latestPeakValue: Float = 0f,
    val latestPeakMinute: Int = 0,
    val riskStatus: MetabolicRiskStatus = MetabolicRiskStatus.NO_DATA,
    val aiCoachAdvice: String = "拍摄餐盘，AI 将即刻为您预测餐后血糖波动曲线与重点时段。",
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
        todayRecords.forEach { rec ->
            try {
                val parsed = gson.fromJson(rec.aiJson, AnalysisResultJson::class.java)
                todayCarbs += (parsed?.totalCarbIntake ?: 0f)
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
                MetabolicRiskStatus.SPIKE_RISK -> "最近一餐预计升糖较快，建议餐后 30 分钟慢走 15 分钟促进血糖利用。"
                MetabolicRiskStatus.ELEVATED -> "碳水负荷稍偏高，建议下一餐增加绿叶蔬菜与蛋白质比例。"
                MetabolicRiskStatus.OPTIMAL -> "上一餐碳水配比合理，升糖曲线平缓，继续保持健康餐盘搭配！"
            }

        return HomeDashboardUiState(
            todayRecords = todayRecords,
            todayCarbIntake = todayCarbs,
            todayMealCount = todayRecords.size,
            dailyCarbTarget = target,
            latestRecord = latest,
            latestAnalysis = latestParsed,
            latestPeakValue = peakVal,
            latestPeakMinute = peakMin,
            riskStatus = risk,
            aiCoachAdvice = advice,
        )
    }
}

/**
 * 现代极简代谢健康看板首页（纯私有·真实数据驱动）
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    guardianDays: Int = 1,
    welcomeTrigger: Int = 0,
    onScanClick: () -> Unit = {},
    onMealClick: (Long) -> Unit = {},
    onViewArchiveClick: () -> Unit = {},
    viewModel: HomeDashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                // 1. 顶部 Header 卡片（已守护 XX 天 + 代谢状态胶囊）
                item {
                    HeaderGuardianCard(
                        guardianDays = guardianDays,
                        status = state.riskStatus,
                    )
                }

                // 2. 今日摄入概览卡片（真实碳水累计与进度）
                item {
                    TodayIntakeMetricCard(
                        totalCarbs = state.todayCarbIntake,
                        mealCount = state.todayMealCount,
                        targetCarbs = state.dailyCarbTarget,
                    )
                }

                // 3. 今日餐盘记录动态流（支持一日多餐清晰回溯与快捷追加）
                item {
                    TodayMealsSection(
                        todayRecords = state.todayRecords,
                        onMealClick = onMealClick,
                        onScanClick = onScanClick,
                        onViewArchiveClick = onViewArchiveClick,
                    )
                }

                // 4. AI 代谢微建议卡片（基于真实时段分析生成的温馨提示）
                item {
                    AiMetabolicAdviceCard(
                        advice = state.aiCoachAdvice,
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderGuardianCard(
    guardianDays: Int,
    status: MetabolicRiskStatus,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Text(
                        text = "私有控糖守护",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                    )
                }

                // 状态药丸胶囊
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, status.badgeColor.copy(alpha = 0.4f)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(status.badgeColor)
                        )
                        Text(
                            text = status.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = status.badgeColor,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Text(
                text = "已为您平稳守护 $guardianDays 天",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
            )
        }
    }
}

@Composable
private fun TodayIntakeMetricCard(
    totalCarbs: Float,
    mealCount: Int,
    targetCarbs: Float = 130f,
) {
    val progress = if (targetCarbs > 0f) (totalCarbs / targetCarbs).coerceIn(0f, 1f) else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "今日吸收碳水总量",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "已记录 $mealCount 餐",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "%.1f".format(totalCarbs),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                )
                Text(
                    text = "g",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "参考配额 ${targetCarbs.toInt()}g",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (progress > 0.85f) StatusWarningAmber else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun TodayMealsSection(
    todayRecords: List<FoodRecord>,
    onMealClick: (Long) -> Unit,
    onScanClick: () -> Unit,
    onViewArchiveClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Restaurant,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = "今日餐食记录",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Text(
                            text = "${todayRecords.size} 餐",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }

                if (todayRecords.isNotEmpty()) {
                    TextButton(
                        onClick = onViewArchiveClick,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "全部档案",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }

            if (todayRecords.isEmpty()) {
                // 空状态
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Restaurant,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "今日尚未记录餐食",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "点击拍下餐盘，AI 将为您智能识别并绘制 240 分钟升糖曲线。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                // 列表展示今日各餐
                val gson = remember { Gson() }
                val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    todayRecords.forEach { record ->
                        val parsed = remember(record.aiJson) {
                            try {
                                gson.fromJson(record.aiJson, AnalysisResultJson::class.java)
                            } catch (_: Exception) {
                                null
                            }
                        }
                        val peakVal = parsed?.curvePoints?.maxOfOrNull { it.bloodSugar } ?: 0f
                        val firstImage = record.imagePath.split("|").firstOrNull().orEmpty()

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onMealClick(record.id) },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                AsyncImage(
                                    model = firstImage,
                                    contentDescription = record.dishName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surface),
                                )

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Text(
                                            text = record.dishName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false),
                                        )
                                        Text(
                                            text = timeFormat.format(Date(record.recordDate)),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        Text(
                                            text = "碳水 ${parsed?.totalCarbIntake ?: 0f}g",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        if (peakVal > 0f) {
                                            Text(
                                                text = "峰值 ${"%.1f".format(peakVal)} mmol/L",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (peakVal >= 7.8f) StatusWarningAmber else MaterialTheme.colorScheme.primary,
                                            )
                                        }
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Filled.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            }

            // 拍照测算餐盘按钮
            Button(
                onClick = onScanClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                ),
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (todayRecords.isEmpty()) "拍照测算今日第一餐" else "+ 测算新的一餐",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun AiMetabolicAdviceCard(advice: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "AI 代谢微建议",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }

            Text(
                text = advice,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    HomeScreen(guardianDays = 12)
}
