package com.example.diabetesassistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.diabetesassistant.ui.components.GlassCard
import com.example.diabetesassistant.ui.components.GreenGlassBackground

private val ArchiveBackground = Color(0xFFF3F8F4)
private val ArchiveGreen = Color(0xFF007A35)
private val ArchiveBrightGreen = Color(0xFF43FF82)
private val ArchiveInk = Color(0xFF17251E)
private val ArchiveMuted = Color(0xFF6B7770)
private val ArchiveYellow = Color(0xFFF4C84E)
private val ArchiveRed = Color(0xFFFF5353)
private val ArchiveSoftGreen = Color(0xFFE9F9EE)

@Composable
fun ArchiveScreen(
    modifier: Modifier = Modifier,
    onViewAllClick: () -> Unit = {},
    onDayClick: () -> Unit = {},
    onRecordClick: (String) -> Unit = {},
) {
    GreenGlassBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            ArchiveTopBar()
            ArchiveSearchBar()
            MonthlyCalendarCard(onDayClick = onDayClick)
            RecentRecordsSection(
                onViewAllClick = onViewAllClick,
                onRecordClick = onRecordClick,
            )
        }
    }
}

@Composable
fun ArchiveAllAnalysesScreen(
    onBackClick: () -> Unit,
    onRecordClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedFilter by remember { mutableStateOf("全部") }
    val filters = listOf("全部", "高糖负荷", "需核对")
    ArchiveSecondaryScaffold(
        modifier = modifier,
        title = "全部分析",
        onBackClick = onBackClick,
    ) {
        ArchiveSearchBar()
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            filters.forEach { filter ->
                ArchiveFilterChip(
                    text = filter,
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                )
            }
        }
        ArchiveSummaryCard(
            title = "本周识别概览",
            subtitle = "共分析 12 餐，3 餐建议减少主食，2 餐提示药量需复核。",
            metrics = listOf("平均糖摄入" to "39g", "建议步行" to "260分"),
        )
        archiveRecords.forEach { record ->
            RecentRecordItem(
                record = record,
                onClick = { onRecordClick(record.id) },
                showAdvice = true,
            )
        }
    }
}

@Composable
fun ArchiveDayDetailScreen(
    onBackClick: () -> Unit,
    onRecordClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    ArchiveSecondaryScaffold(
        modifier = modifier,
        title = "10月11日分析",
        onBackClick = onBackClick,
    ) {
        ArchiveSummaryCard(
            title = "今日餐食识别汇总",
            subtitle = "已完成早餐、午餐和加餐分析。午餐糖摄入较高，晚餐建议减少主食并优先补充蔬菜。",
            metrics = listOf("可吸收糖" to "125g", "总热量" to "1780千卡"),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DayActionCard(
                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                title = "运动建议",
                value = "餐后步行 30 分钟",
                modifier = Modifier.weight(1f),
            )
            DayActionCard(
                icon = Icons.Filled.Medication,
                title = "药量提醒",
                value = "晚餐前复核剂量",
                modifier = Modifier.weight(1f),
            )
        }
        Text(
            text = "当日记录",
            style = MaterialTheme.typography.titleLarge,
            color = ArchiveInk,
            fontWeight = FontWeight.ExtraBold,
        )
        archiveRecords.forEach { record ->
            DayTimelineItem(record = record, onClick = { onRecordClick(record.id) })
        }
    }
}

@Composable
fun ArchiveMealDetailScreen(
    onBackClick: () -> Unit,
    mealId: String = archiveRecords.first().id,
    modifier: Modifier = Modifier,
) {
    val record = archiveRecords.find { it.id == mealId } ?: archiveRecords.first()
    ArchiveSecondaryScaffold(
        modifier = modifier,
        title = "餐食分析详情",
        onBackClick = onBackClick,
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = record.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = ArchiveInk,
                            fontWeight = FontWeight.ExtraBold,
                        )
                        Text(
                            text = "${record.meta} · 识别可信度 ${record.confidence}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ArchiveMuted,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    SugarBadge(text = record.carbs, color = record.badgeColor)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailMetric(label = "估算热量", value = record.calories, modifier = Modifier.weight(1f))
                    DetailMetric(label = "糖负荷", value = record.sugarLevel, modifier = Modifier.weight(1f))
                }
            }
        }

        Text(
            text = "食材与烹饪方式",
            style = MaterialTheme.typography.titleLarge,
            color = ArchiveInk,
            fontWeight = FontWeight.ExtraBold,
        )
        record.ingredients.forEach { ingredient ->
            IngredientDetailCard(ingredient)
        }

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White.copy(alpha = 0.64f),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "糖摄入估算逻辑",
                    style = MaterialTheme.typography.titleMedium,
                    color = ArchiveInk,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = record.estimateNote,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ArchiveMuted,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                )
            }
        }

        Text(
            text = "行动建议",
            style = MaterialTheme.typography.titleLarge,
            color = ArchiveInk,
            fontWeight = FontWeight.ExtraBold,
        )
        record.actionAdvice.forEachIndexed { index, advice ->
            AdviceRow(
                icon = when (index) {
                    0 -> Icons.AutoMirrored.Filled.DirectionsWalk
                    1 -> Icons.Filled.LocalDining
                    else -> Icons.Filled.Medication
                },
                title = advice.title,
                subtitle = advice.subtitle,
            )
        }
    }
}

@Composable
private fun ArchiveSecondaryScaffold(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    GreenGlassBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(top = 12.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            ArchiveSecondaryTopBar(title = title, onBackClick = onBackClick)
            content()
        }
    }
}

@Composable
private fun ArchiveSecondaryTopBar(
    title: String,
    onBackClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Surface(
            modifier = Modifier
                .size(36.dp)
                .clickable(onClick = onBackClick),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.36f),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = ArchiveGreen,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = ArchiveInk,
            fontWeight = FontWeight.ExtraBold,
        )
        Box(modifier = Modifier.size(36.dp))
    }
}

@Composable
private fun ArchiveTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Icon(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = null,
            tint = Color(0xFF2D6B61),
            modifier = Modifier.size(32.dp),
        )
        Text(
            text = "分析档案",
            style = MaterialTheme.typography.headlineSmall,
            color = ArchiveInk,
            fontWeight = FontWeight.ExtraBold,
        )
        Icon(
            imageVector = Icons.Filled.Notifications,
            contentDescription = null,
            tint = ArchiveGreen,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun ArchiveSearchBar() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color.White.copy(alpha = 0.50f),
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = ArchiveMuted,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = "搜索餐食、建议或日期...",
                style = MaterialTheme.typography.bodyMedium,
                color = ArchiveMuted,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun MonthlyCalendarCard(onDayClick: () -> Unit) {
    var monthIndex by remember { mutableStateOf(1) }
    val monthTitles = listOf("2023年9月", "2023年10月", "2023年11月")
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = monthTitles[monthIndex],
                    style = MaterialTheme.typography.titleLarge,
                    color = ArchiveInk,
                    fontWeight = FontWeight.ExtraBold,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RoundIconButton(
                        icon = Icons.Filled.ChevronLeft,
                        onClick = { monthIndex = (monthIndex - 1).coerceAtLeast(0) },
                    )
                    RoundIconButton(
                        icon = Icons.Filled.ChevronRight,
                        onClick = { monthIndex = (monthIndex + 1).coerceAtMost(monthTitles.lastIndex) },
                    )
                }
            }

            CalendarGrid(
                monthOffset = monthIndex - 1,
                onDayClick = onDayClick,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CalendarLegend(color = ArchiveBrightGreen, label = "已建议")
                Spacer(modifier = Modifier.width(22.dp))
                CalendarLegend(color = ArchiveYellow, label = "需核对")
                Spacer(modifier = Modifier.width(22.dp))
                CalendarLegend(color = ArchiveRed, label = "高糖负荷")
            }
        }
    }
}

@Composable
private fun RoundIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .size(36.dp),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.58f),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ArchiveMuted,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    monthOffset: Int,
    onDayClick: () -> Unit,
) {
    val weekdays = listOf("日", "一", "二", "三", "四", "五", "六")
    val weeks = listOf(
        listOf(CalendarDay(1), CalendarDay(2), CalendarDay(3), CalendarDay(4, DayStatus.Normal), CalendarDay(5, DayStatus.Normal), CalendarDay(6, if (monthOffset == 1) DayStatus.Danger else DayStatus.Warning), CalendarDay(7, DayStatus.Normal)),
        listOf(CalendarDay(8, DayStatus.Normal), CalendarDay(9, DayStatus.Danger), CalendarDay(10, DayStatus.Normal), CalendarDay(11, DayStatus.Normal, selected = monthOffset == 0), CalendarDay(12), CalendarDay(13), CalendarDay(14)),
        listOf(CalendarDay(15), CalendarDay(16), CalendarDay(17), CalendarDay(18), CalendarDay(19), CalendarDay(20), CalendarDay(21)),
    )

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            weekdays.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = ArchiveMuted.copy(alpha = 0.72f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        weeks.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    CalendarDayCell(
                        day = day,
                        modifier = Modifier.weight(1f),
                        onClick = onDayClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDay,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.clickable(enabled = day.status != DayStatus.None, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(
                    color = if (day.selected) ArchiveBrightGreen.copy(alpha = 0.20f) else Color.Transparent,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = day.number.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (day.selected) ArchiveGreen else ArchiveInk.copy(alpha = 0.84f),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(day.status.colorOrTransparent(), CircleShape),
        )
    }
}

@Composable
private fun CalendarLegend(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = ArchiveMuted,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun RecentRecordsSection(
    onViewAllClick: () -> Unit,
    onRecordClick: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "最近分析",
                style = MaterialTheme.typography.titleLarge,
                color = ArchiveInk,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = "查看全部",
                modifier = Modifier.clickable(onClick = onViewAllClick),
                style = MaterialTheme.typography.labelLarge,
                color = ArchiveGreen,
                fontWeight = FontWeight.Bold,
            )
        }

        archiveRecords.take(3).forEach { record ->
            RecentRecordItem(record = record, onClick = { onRecordClick(record.id) })
        }
    }
}

@Composable
private fun RecentRecordItem(
    record: ArchiveMealRecord,
    onClick: () -> Unit,
    showAdvice: Boolean = false,
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        elevation = 8.dp,
        containerColor = Color.White.copy(alpha = 0.66f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(record.iconBackground, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = record.icon,
                        contentDescription = null,
                        tint = record.iconColor,
                        modifier = Modifier.size(25.dp),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = record.meta,
                        style = MaterialTheme.typography.labelMedium,
                        color = ArchiveMuted,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = record.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = ArchiveInk,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = record.methods,
                        style = MaterialTheme.typography.labelSmall,
                        color = ArchiveMuted,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        text = "可吸收糖",
                        style = MaterialTheme.typography.labelSmall,
                        color = ArchiveMuted,
                        fontWeight = FontWeight.Bold,
                    )
                    SugarBadge(text = record.carbs, color = record.badgeColor)
                }
            }
            if (showAdvice) {
                Text(
                    text = record.advice,
                    style = MaterialTheme.typography.bodySmall,
                    color = ArchiveGreen,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun SugarBadge(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(50),
        color = color,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = ArchiveGreen,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Composable
private fun ArchiveFilterChip(
    text: String,
    selected: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) ArchiveBrightGreen.copy(alpha = 0.72f) else Color.White.copy(alpha = 0.55f),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) ArchiveGreen else ArchiveMuted,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Composable
private fun ArchiveSummaryCard(
    title: String,
    subtitle: String,
    metrics: List<Pair<String, String>>,
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(ArchiveBrightGreen.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = ArchiveGreen,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = ArchiveInk,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = ArchiveMuted,
                fontWeight = FontWeight.SemiBold,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                metrics.forEach { (label, value) ->
                    DetailMetric(label = label, value = value, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DetailMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.50f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = ArchiveMuted,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = ArchiveInk,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}

@Composable
private fun DayActionCard(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        containerColor = Color.White.copy(alpha = 0.62f),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ArchiveGreen,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = ArchiveMuted,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = ArchiveInk,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}

@Composable
private fun DayTimelineItem(
    record: ArchiveMealRecord,
    onClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(record.iconColor, CircleShape),
            )
            Spacer(
                modifier = Modifier
                    .width(2.dp)
                    .height(74.dp)
                    .background(record.iconColor.copy(alpha = 0.22f)),
            )
        }
        GlassCard(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(22.dp),
            elevation = 8.dp,
            containerColor = Color.White.copy(alpha = 0.66f),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        text = record.meta,
                        style = MaterialTheme.typography.labelMedium,
                        color = ArchiveMuted,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = record.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = ArchiveInk,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = record.advice,
                        style = MaterialTheme.typography.bodySmall,
                        color = ArchiveGreen,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = ArchiveMuted,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

@Composable
private fun IngredientDetailCard(ingredient: IngredientArchiveDetail) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = 8.dp,
        containerColor = Color.White.copy(alpha = 0.65f),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = ingredient.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = ArchiveInk,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = "${ingredient.weight} · ${ingredient.sugar}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ArchiveMuted,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                SugarBadge(text = ingredient.contribution, color = ArchiveSoftGreen)
            }
            ingredient.methods.forEach { method ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.40f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = method.first,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ArchiveInk,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = method.second,
                        style = MaterialTheme.typography.labelMedium,
                        color = ArchiveGreen,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun AdviceRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = 8.dp,
        containerColor = Color.White.copy(alpha = 0.62f),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(ArchiveBrightGreen.copy(alpha = 0.20f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ArchiveGreen,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = ArchiveInk,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = ArchiveMuted,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Immutable
private data class CalendarDay(
    val number: Int,
    val status: DayStatus = DayStatus.None,
    val selected: Boolean = false,
)

private enum class DayStatus {
    None,
    Normal,
    Warning,
    Danger,
}

private fun DayStatus.colorOrTransparent(): Color = when (this) {
    DayStatus.None -> Color.Transparent
    DayStatus.Normal -> ArchiveBrightGreen
    DayStatus.Warning -> ArchiveYellow
    DayStatus.Danger -> ArchiveRed
}

@Immutable
private data class ArchiveMealRecord(
    val id: String,
    val icon: ImageVector,
    val iconColor: Color,
    val iconBackground: Color,
    val meta: String,
    val title: String,
    val carbs: String,
    val calories: String,
    val sugarLevel: String,
    val confidence: String,
    val badgeColor: Color,
    val methods: String,
    val advice: String,
    val ingredients: List<IngredientArchiveDetail>,
    val estimateNote: String,
    val actionAdvice: List<ArchiveAdvice>,
)

@Immutable
private data class IngredientArchiveDetail(
    val name: String,
    val weight: String,
    val sugar: String,
    val contribution: String,
    val methods: List<Pair<String, String>>,
)

@Immutable
private data class ArchiveAdvice(
    val title: String,
    val subtitle: String,
)

private val archiveRecords = listOf(
    ArchiveMealRecord(
        id = "lunch_salad",
        icon = Icons.Filled.Restaurant,
        iconColor = ArchiveGreen,
        iconBackground = Color(0xFFE8F7EE),
        meta = "今日 12:30 · 午餐",
        title = "糙米鸡胸肉沙拉",
        carbs = "35g",
        calories = "520千卡",
        sugarLevel = "中等",
        confidence = "92%",
        badgeColor = Color(0xFFBDFED1),
        methods = "鸡胸肉：煎制 120g；糙米：蒸煮 90g；蔬菜：生食 160g",
        advice = "餐后步行 25 分钟，晚餐主食减半。",
        ingredients = listOf(
            IngredientArchiveDetail(
                name = "鸡胸肉",
                weight = "120g",
                sugar = "约 0g 糖",
                contribution = "0g",
                methods = listOf("煎制" to "90g", "水煮" to "30g"),
            ),
            IngredientArchiveDetail(
                name = "糙米",
                weight = "90g",
                sugar = "约 28g 糖",
                contribution = "28g",
                methods = listOf("蒸煮" to "90g"),
            ),
            IngredientArchiveDetail(
                name = "蔬菜组合",
                weight = "160g",
                sugar = "约 7g 糖",
                contribution = "7g",
                methods = listOf("生食" to "100g", "焯水" to "60g"),
            ),
        ),
        estimateNote = "糙米按蒸煮重量计算，鸡胸肉分煎制和水煮两段估算，蔬菜按生食与焯水分别计算后汇总。",
        actionAdvice = listOf(
            ArchiveAdvice("餐后步行 25 分钟", "帮助平稳餐后糖吸收曲线。"),
            ArchiveAdvice("晚餐减少主食", "建议主食减半，并增加绿叶菜。"),
            ArchiveAdvice("用药前复核", "如需调整药量，请按医生方案或设备记录复核。"),
        ),
    ),
    ArchiveMealRecord(
        id = "breakfast_latte",
        icon = Icons.Filled.Coffee,
        iconColor = ArchiveYellow,
        iconBackground = Color(0xFFFFF8DC),
        meta = "今日 08:15 · 早餐",
        title = "全麦面包配燕麦拿铁",
        carbs = "38g",
        calories = "460千卡",
        sugarLevel = "中等",
        confidence = "89%",
        badgeColor = Color(0xFFFFEAA8),
        methods = "面包：烘烤 80g；燕麦：冲泡 35g；牛奶：加热 200ml",
        advice = "下一餐减少精制碳水，优先补充蛋白质。",
        ingredients = listOf(
            IngredientArchiveDetail("全麦面包", "80g", "约 24g 糖", "24g", listOf("烘烤" to "80g")),
            IngredientArchiveDetail("燕麦", "35g", "约 9g 糖", "9g", listOf("冲泡" to "35g")),
            IngredientArchiveDetail("牛奶", "200ml", "约 5g 糖", "5g", listOf("加热" to "200ml")),
        ),
        estimateNote = "面包按烘烤成品重量计算，燕麦和牛奶按饮品配比折算；咖啡本身不额外增加可吸收糖。",
        actionAdvice = listOf(
            ArchiveAdvice("午餐控制主食", "建议米饭不超过半碗。"),
            ArchiveAdvice("增加蛋白质", "午餐优先选择鸡蛋、鱼肉或豆制品。"),
            ArchiveAdvice("无需额外药量调整", "本餐为中等糖负荷，按日常方案复核即可。"),
        ),
    ),
    ArchiveMealRecord(
        id = "snack_cake",
        icon = Icons.Filled.Cake,
        iconColor = ArchiveRed,
        iconBackground = Color(0xFFFFECEA),
        meta = "昨日 15:45 · 加餐",
        title = "芝士蛋糕薄片",
        carbs = "52g",
        calories = "390千卡",
        sugarLevel = "偏高",
        confidence = "91%",
        badgeColor = Color(0xFFFFC7C7),
        methods = "蛋糕：烘焙 95g；奶油：冷加工 25g",
        advice = "糖负荷偏高，建议复核用药并增加轻运动。",
        ingredients = listOf(
            IngredientArchiveDetail("芝士蛋糕", "95g", "约 46g 糖", "46g", listOf("烘焙" to "95g")),
            IngredientArchiveDetail("奶油层", "25g", "约 6g 糖", "6g", listOf("冷加工" to "25g")),
        ),
        estimateNote = "甜品类按成品重量估算，其中蛋糕体和奶油层分开计算；含糖量偏高，因此提高提醒等级。",
        actionAdvice = listOf(
            ArchiveAdvice("餐后轻步行 30 分钟", "避免久坐，帮助消耗部分糖负荷。"),
            ArchiveAdvice("晚餐避免主食叠加", "晚餐以蔬菜和蛋白质为主。"),
            ArchiveAdvice("复核药量方案", "若有医生设置的高糖餐处理方案，请优先参考。"),
        ),
    ),
    ArchiveMealRecord(
        id = "home_breakfast",
        icon = Icons.Filled.Coffee,
        iconColor = ArchiveYellow,
        iconBackground = Color(0xFFFFF8DC),
        meta = "今日 07:30 · 早餐",
        title = "燕麦粥和牛奶",
        carbs = "12g",
        calories = "320千卡",
        sugarLevel = "较低",
        confidence = "88%",
        badgeColor = Color(0xFFBDFED1),
        methods = "燕麦：熬煮 50g；牛奶：加热 200ml",
        advice = "餐后步行 15 分钟即可，无需额外减少午餐蛋白质。",
        ingredients = listOf(
            IngredientArchiveDetail("燕麦", "50g", "约 8g 糖", "8g", listOf("熬煮" to "50g")),
            IngredientArchiveDetail("全脂牛奶", "200ml", "约 4g 糖", "4g", listOf("加热" to "200ml")),
        ),
        estimateNote = "燕麦粥按熟重和吸水比例折算，牛奶按饮用量估算；整体糖负荷较低。",
        actionAdvice = listOf(
            ArchiveAdvice("餐后步行 15 分钟", "保持轻松步速即可。"),
            ArchiveAdvice("午餐正常搭配", "可保留适量粗粮主食。"),
            ArchiveAdvice("无需药量额外提醒", "按日常方案记录即可。"),
        ),
    ),
    ArchiveMealRecord(
        id = "home_lunch",
        icon = Icons.Filled.Restaurant,
        iconColor = ArchiveGreen,
        iconBackground = Color(0xFFE8F7EE),
        meta = "今日 12:15 · 午餐",
        title = "米饭鸡胸肉",
        carbs = "45g",
        calories = "550千卡",
        sugarLevel = "偏高",
        confidence = "93%",
        badgeColor = Color(0xFFFFEAA8),
        methods = "米饭：蒸煮 150g；鸡胸肉：烤制 120g；西兰花：焯水 80g",
        advice = "建议餐后步行 25 分钟，并按个人方案核对药量。",
        ingredients = listOf(
            IngredientArchiveDetail("米饭", "150g", "约 39g 糖", "39g", listOf("蒸煮" to "150g")),
            IngredientArchiveDetail("鸡胸肉", "120g", "约 0g 糖", "0g", listOf("烤制" to "120g")),
            IngredientArchiveDetail("西兰花", "80g", "约 6g 糖", "6g", listOf("焯水" to "80g")),
        ),
        estimateNote = "米饭是本餐主要糖来源，鸡胸肉不贡献明显糖分，西兰花按焯水重量计入少量可吸收糖。",
        actionAdvice = listOf(
            ArchiveAdvice("餐后步行 25 分钟", "帮助平稳餐后糖吸收。"),
            ArchiveAdvice("晚餐减少主食", "建议晚餐主食减半或换成绿叶菜。"),
            ArchiveAdvice("药量核对：4.5 单位", "仅作核对参考，请遵循医生方案。"),
        ),
    ),
    ArchiveMealRecord(
        id = "home_snack",
        icon = Icons.Filled.Restaurant,
        iconColor = ArchiveGreen,
        iconBackground = Color(0xFFE8F7EE),
        meta = "今日 15:30 · 加餐",
        title = "苹果",
        carbs = "19g",
        calories = "95千卡",
        sugarLevel = "适中",
        confidence = "90%",
        badgeColor = Color(0xFFBDFED1),
        methods = "苹果：生食 1个",
        advice = "无需额外运动，晚餐保持清淡即可。",
        ingredients = listOf(
            IngredientArchiveDetail("苹果", "1个", "约 19g 糖", "19g", listOf("生食" to "1个")),
        ),
        estimateNote = "水果按中等大小单个估算，生食状态下不额外增加烹饪系数。",
        actionAdvice = listOf(
            ArchiveAdvice("无需额外运动", "保持日常活动即可。"),
            ArchiveAdvice("晚餐清淡", "避免叠加甜饮或甜品。"),
            ArchiveAdvice("无需药量提醒", "按日常方案记录即可。"),
        ),
    ),
)
