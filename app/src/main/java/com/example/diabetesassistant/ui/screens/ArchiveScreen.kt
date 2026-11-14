package com.example.diabetesassistant.ui.screens

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.diabetesassistant.data.repository.FoodRecordRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private val WarningAmber = Color(0xFFE76F51)

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val foodRecordRepository: FoodRecordRepository,
) : ViewModel() {
    val allRecords: StateFlow<List<FoodRecord>> = foodRecordRepository.observeAll().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    fun deleteRecord(record: FoodRecord) {
        viewModelScope.launch {
            foodRecordRepository.delete(record)
        }
    }
}

/**
 * 现代极简代谢档案夹（按真实日历分组 + 真实餐食卡片流）
 */
@Composable
fun ArchiveScreen(
    modifier: Modifier = Modifier,
    onRecordClick: (Long) -> Unit = {},
    onScanClick: () -> Unit = {},
    viewModel: ArchiveViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var recordToDelete by remember { mutableStateOf<FoodRecord?>(null) }
    val gson = remember { Gson() }

    // 将真实记录按本地日期过滤
    val recordsForDate = remember(allRecords, selectedDate) {
        allRecords.filter { record ->
            val recordDate = Instant.ofEpochMilli(record.recordDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            recordDate == selectedDate
        }
    }

    // 计算当日真实摄入总碳水
    val dayTotalCarbs = remember(recordsForDate) {
        var total = 0f
        recordsForDate.forEach { rec ->
            try {
                val parsed = gson.fromJson(rec.aiJson, AnalysisResultJson::class.java)
                total += (parsed?.totalCarbIntake ?: 0f)
            } catch (_: Exception) {}
        }
        total
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. 顶部标题栏
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                Text(
                    text = "代谢档案日历",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                )
                Text(
                    text = "回溯每日膳食对血糖波动的真实影响",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // 2. 现代水平滚动日历条
            HorizontalCalendarStrip(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                allRecords = allRecords,
            )

            // 3. 当日统计摘要横条
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val dateTitle = if (selectedDate == LocalDate.now()) "今天" else "${selectedDate.monthValue}月${selectedDate.dayOfMonth}日"
                Text(
                    text = "$dateTitle · ${recordsForDate.size} 次餐食记录",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "预估碳水: ${"%.1f".format(dayTotalCarbs)} g",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            // 4. 当日餐食卡片流
            if (recordsForDate.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EventNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                        Text(
                            text = "该日暂无餐食分析记录",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "拍摄或上传餐盘照片，即可在此查看升糖测算回溯。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(6.dp))
                        Button(
                            onClick = onScanClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("拍照测算餐后血糖", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(recordsForDate, key = { it.id }) { record ->
                        val parsed = remember(record.aiJson) {
                            try {
                                gson.fromJson(record.aiJson, AnalysisResultJson::class.java)
                            } catch (_: Exception) {
                                null
                            }
                        }
                        MealRecordCard(
                            record = record,
                            parsed = parsed,
                            onClick = { onRecordClick(record.id) },
                            onDelete = { recordToDelete = record },
                        )
                    }
                }
            }
        }
    }

    // 删除确认弹窗
    if (recordToDelete != null) {
        val target = recordToDelete!!
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text("删除该条餐食记录？", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("确定要删除「${target.dishName}」吗？此操作将永久移除该条本地计算数据。", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val dishName = target.dishName
                        viewModel.deleteRecord(target)
                        recordToDelete = null
                        Toast.makeText(context, "已删除「$dishName」", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("删除", color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text("取消")
                }
            },
        )
    }
}

@Composable
private fun HorizontalCalendarStrip(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    allRecords: List<FoodRecord>,
) {
    // 渲染最近 14 天到今天
    val days = remember {
        (13 downTo 0).map { LocalDate.now().minusDays(it.toLong()) }
    }

    val recordDates = remember(allRecords) {
        allRecords.map {
            Instant.ofEpochMilli(it.recordDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }.toSet()
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(days) { date ->
                val isSelected = date == selectedDate
                val hasRecords = recordDates.contains(date)
                val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINESE)
                val isToday = date == LocalDate.now()

                Column(
                    modifier = Modifier
                        .width(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else if (isToday) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                        .clickable { onDateSelected(date) }
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = if (isToday) "今" else dayOfWeek,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                    )
                    Text(
                        text = date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    )

                    // 记录标记圆点
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    hasRecords && isSelected -> MaterialTheme.colorScheme.onPrimary
                                    hasRecords -> MaterialTheme.colorScheme.primary
                                    else -> Color.Transparent
                                }
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun MealRecordCard(
    record: FoodRecord,
    parsed: AnalysisResultJson?,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val peak = parsed?.curvePoints?.maxOfOrNull { it.bloodSugar } ?: 0f
    val peakMin = parsed?.curvePoints?.maxByOrNull { it.bloodSugar }?.minute ?: 0
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(record.recordDate))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            val images = remember(record.imagePath) {
                record.imagePath.split("|").filter { it.isNotBlank() }
            }
            Box {
                AsyncImage(
                    model = images.firstOrNull().orEmpty(),
                    contentDescription = record.dishName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
                if (images.size > 1) {
                    Surface(
                        shape = RoundedCornerShape(bottomStart = 6.dp, topEnd = 6.dp),
                        color = Color.Black.copy(alpha = 0.65f),
                        modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp),
                    ) {
                        Text(
                            text = "${images.size}图",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = record.dishName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "碳水 ${"%.1f".format(parsed?.totalCarbIntake ?: 0f)}g",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (peak > 0f) {
                        Text(
                            text = "峰值 ${"%.1f".format(peak)} (${peakMin}分)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (peak >= 7.8f) WarningAmber else MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                if (!parsed?.reminders.isNullOrEmpty()) {
                    Text(
                        text = parsed?.reminders?.first() ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "删除记录",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ArchiveScreenPreview() {
    ArchiveScreen()
}
