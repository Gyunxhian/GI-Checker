package com.example.diabetesassistant.camera

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File
import com.example.diabetesassistant.data.local.BuiltInCarbFallback
import com.example.diabetesassistant.data.model.DishRecognitionResult
import com.example.diabetesassistant.data.model.FoodCategory
import com.example.diabetesassistant.data.model.Ingredient
import com.example.diabetesassistant.data.model.InsulinType
import com.example.diabetesassistant.domain.algorithm.BloodSugarPoint
import com.example.diabetesassistant.domain.algorithm.FocusLevel
import com.example.diabetesassistant.domain.algorithm.FocusPeriod
import com.example.diabetesassistant.domain.algorithm.InsulinBandResult
import com.example.diabetesassistant.ui.theme.HealthPalette

private val BandBlue = Color(0xFF4A90E2)
private val BandGreen = Color(0xFF27AE60)
private val BandOrange = Color(0xFFE67E22)
private val BandRed = Color(0xFFE74C3C)

private val ThresholdHigh = Color(0xFFE74C3C)
private val ThresholdElevated = Color(0xFFE67E22)
private val ThresholdLow = Color(0xFF3498DB)

@Composable
fun ArchiveDetailScreen(
    uiState: ArchiveDetailUiState,
    onStapleIntakeChange: (Float) -> Unit = {},
    onVegIntakeChange: (Float) -> Unit = {},
    onMeatIntakeChange: (Float) -> Unit = {},
    onBloodSugarChange: (Float) -> Unit,
    onInsulinTypeChange: (InsulinType) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onMeatRatioChange: (Float) -> Unit = {},
    onIntakePercentChange: (Float) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // 顶部固定导航栏，适配状态栏安全区域
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    onClick = onBack,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(38.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Text(
                    text = if (uiState.savedFoodRecordId != null && uiState.savedFoodRecordId > 0L) "餐食分析档案详情" else "调整餐盘与测算结果",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp),
            ) {
                item {
                    Text(
                        text = "本分析基于国家营养库与代谢动力学算法，仅供参考，不构成医疗建议。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }

            if (uiState.error != null) {
                item {
                    Text(
                        text = "错误: ${uiState.error}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
            }

            item { DishImageSection(uiState.imageUri) }

            item {
                IngredientsBreakdownCard(
                    recognitionResult = uiState.recognitionResult,
                    carbMap = uiState.ingredientCarbsMap,
                )
            }

            item {
                BloodSugarInputSection(value = "%.1f".format(uiState.bloodSugarStart), onValueChange = onBloodSugarChange)
            }

            item {
                Text(
                    "211 控糖三元组分摄入调节",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 4.dp),
                )
            }

            item {
                NutrientComponentSlider(
                    title = "主食摄入",
                    icon = "🍚",
                    subtitle = "米面、根茎淀粉类",
                    value = uiState.stapleIntake,
                    onValueChange = onStapleIntakeChange,
                    accentColor = Color(0xFFE67E22),
                )
            }

            item {
                NutrientComponentSlider(
                    title = "蔬菜摄入",
                    icon = "🥦",
                    subtitle = "绿叶菜、高纤菌菇",
                    value = uiState.vegIntake,
                    onValueChange = onVegIntakeChange,
                    accentColor = HealthPalette.Secondary,
                )
            }

            item {
                NutrientComponentSlider(
                    title = "荤菜摄入",
                    icon = "🥩",
                    subtitle = "肉、蛋、水产蛋白质",
                    value = uiState.meatIntake,
                    onValueChange = onMeatIntakeChange,
                    accentColor = Color(0xFFE76F51),
                )
            }

            item { InsulinTypeSelector(selectedType = uiState.insulinType, onTypeChange = onInsulinTypeChange) }

            item {
                Text(
                    "碳水摄入: ${"%.1f".format(uiState.totalCarbIntake)}g  |  升糖指数: ${"%.1f".format(uiState.weightedGi)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
                )
            }

            item {
                androidx.compose.animation.AnimatedVisibility(visible = uiState.isRecalculating) {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "正在依据新比例实时测算代谢曲线...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }

            item { GlucoseCurveSection(uiState.bloodSugarPrediction?.points ?: emptyList()) }

            item { InsulinBandSection(uiState.insulinBand) }

            if (uiState.focusPeriods.isNotEmpty()) {
                item {
                    Text(
                        "餐后重点关注时段",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
                    )
                }
                items(uiState.focusPeriods) { period ->
                    FocusPeriodCard(
                        period = period,
                        reminder = uiState.reminders.getOrNull(uiState.focusPeriods.indexOf(period)),
                    )
                }
            }
        }

        SaveButton(
            isSaving = uiState.isSaving,
            onClick = onSave,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp),
        )
    }
}
}

@Composable
private fun DishImageSection(imageUri: String) {
    if (imageUri.isBlank()) return
    val images = remember(imageUri) {
        imageUri.split("|").filter { it.isNotBlank() }
    }

    if (images.size == 1) {
        val singleUri = images.first()
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            val model = remember(singleUri) { if (singleUri.startsWith("/")) File(singleUri) else singleUri }
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(model)
                    .crossfade(true)
                    .build(),
                contentDescription = "餐盘照片",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop,
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = "已拍摄餐盘 (${images.size}张)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(images) { uri ->
                    val model = remember(uri) { if (uri.startsWith("/")) File(uri) else uri }
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.size(width = 160.dp, height = 120.dp),
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(model)
                                .crossfade(true)
                                .build(),
                            contentDescription = "餐盘子图",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NutrientComponentSlider(
    title: String,
    icon: String,
    subtitle: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    accentColor: Color,
) {
    val safeValue = value.coerceIn(0f, 1f)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = icon, fontSize = 16.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "($subtitle)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = accentColor.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
                ) {
                    Text(
                        text = "${(safeValue * 100).toInt()}%",
                        color = accentColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }

            Slider(
                value = safeValue,
                onValueChange = onValueChange,
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = accentColor,
                    activeTrackColor = accentColor,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                modifier = Modifier.padding(top = 2.dp, bottom = 0.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(0f to "不吃 (0%)", 0.5f to "半份 (50%)", 1.0f to "全吃 (100%)").forEach { (presetVal, label) ->
                    val isCurrent = kotlin.math.abs(safeValue - presetVal) < 0.05f
                    Surface(
                        onClick = { onValueChange(presetVal) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isCurrent) accentColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, if (isCurrent) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isCurrent) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 4.dp),
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BloodSugarInputSection(
    value: String,
    onValueChange: (Float) -> Unit,
) {
    var rawText by remember(value) { mutableStateOf(value) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    "餐前血糖",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "作为餐后吸收曲线的起始基准",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedTextField(
                value = rawText,
                onValueChange = { newStr ->
                    rawText = newStr
                    val parsed = newStr.toFloatOrNull()
                    if (parsed != null) {
                        onValueChange(parsed.coerceIn(2.0f, 20.0f))
                    }
                },
                modifier = Modifier.width(100.dp),
                suffix = { Text("mmol/L", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
            )
        }
    }
}

@Composable
private fun InsulinTypeSelector(
    selectedType: InsulinType,
    onTypeChange: (InsulinType) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                "胰岛素类型",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InsulinType.entries.forEach { type ->
                    val isSelected = type == selectedType
                    val label = when (type) {
                        InsulinType.RAPID -> "速效"
                        InsulinType.SHORT -> "短效"
                        InsulinType.INTERMEDIATE -> "中效"
                        InsulinType.LONG -> "长效"
                    }
                    Surface(
                        onClick = { onTypeChange(type) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GlucoseCurveSection(points: List<BloodSugarPoint>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Text(
                    "餐后血糖波动预测 (240分钟)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (points.isNotEmpty()) {
                val peak = points.maxByOrNull { it.bloodSugar }
                val trough = points.minByOrNull { it.bloodSugar }
                Text(
                    "预计吸收峰值: ${"%.1f".format(peak?.bloodSugar ?: 0f)} mmol/L (餐后约 ${peak?.minute ?: 0} 分钟) | 谷值: ${"%.1f".format(trough?.bloodSugar ?: 0f)} mmol/L",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )

                val maxBg = (points.maxOf { it.bloodSugar } + 1f).coerceAtLeast(12f)
                val minBg = (points.minOf { it.bloodSugar } - 1f).coerceAtLeast(0f)
                val axisTextColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
                val primaryColor = MaterialTheme.colorScheme.primary

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(top = 10.dp),
                ) {
                    val padLeft = 40f
                    val padBottom = 24f
                    val padTop = 8f
                    val padRight = 8f
                    val w = size.width - padLeft - padRight
                    val h = size.height - padBottom - padTop
                    val toX: (Int) -> Float = { min -> padLeft + (min / 240f) * w }
                    val toY: (Float) -> Float = { bg -> padTop + ((maxBg - bg) / (maxBg - minBg)) * h }

                    val thresholdPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(80, 100, 110, 120)
                        pathEffect = android.graphics.DashPathEffect(floatArrayOf(8f, 8f), 0f)
                        isAntiAlias = true
                    }
                    val thresholdLabelPaint = android.graphics.Paint().apply {
                        color = axisTextColor
                        textSize = 22f
                        isAntiAlias = true
                    }
                    listOf(10.0f to Color(0x55E74C3C), 7.8f to Color(0x55E67E22), 3.9f to Color(0x553498DB)).forEach { (threshold, bgColor) ->
                        val y = toY(threshold)
                        drawLine(bgColor, Offset(padLeft, y), Offset(padLeft + w, y), strokeWidth = 1f)
                        drawContext.canvas.nativeCanvas.drawLine(padLeft, y, padLeft + w, y, thresholdPaint)
                        drawContext.canvas.nativeCanvas.drawText("%.1f".format(threshold), 2f, y + 4f, thresholdLabelPaint)
                    }

                    if (points.size >= 2) {
                        val fillPath = Path()
                        val linePath = Path()
                        points.forEachIndexed { i, point ->
                            val x = toX(point.minute)
                            val y = toY(point.bloodSugar)
                            if (i == 0) {
                                fillPath.moveTo(x, y)
                                linePath.moveTo(x, y)
                            } else {
                                fillPath.lineTo(x, y)
                                linePath.lineTo(x, y)
                            }
                        }
                        val lastX = toX(points.last().minute)
                        val bottomY = padTop + h
                        fillPath.lineTo(lastX, bottomY)
                        fillPath.lineTo(toX(points.first().minute), bottomY)
                        fillPath.close()

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.35f),
                                    primaryColor.copy(alpha = 0.08f),
                                    Color.Transparent,
                                ),
                                startY = padTop,
                                endY = bottomY,
                            ),
                        )

                        drawPath(linePath, primaryColor, style = Stroke(width = 4f, cap = StrokeCap.Round))
                    }

                    val xPaint = android.graphics.Paint().apply {
                        color = axisTextColor
                        textSize = 22f
                        isAntiAlias = true
                    }
                    listOf(0, 60, 120, 180, 240).forEach { min ->
                        val x = toX(min)
                        drawContext.canvas.nativeCanvas.drawText("${min}分", x - 12f, size.height - 2f, xPaint)
                    }

                    points.maxByOrNull { it.bloodSugar }?.let { peakPoint ->
                        drawCircle(primaryColor, 6f, Offset(toX(peakPoint.minute), toY(peakPoint.bloodSugar)))
                    }
                }
            } else {
                Text("暂无数据", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/**
 * 动态按比例 Scale 且区域平滑过渡的胰岛素推荐剂量色带（去除突兀白杠，改用底部流体发光游标）
 */
@Composable
private fun InsulinBandSection(band: InsulinBandResult?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BandBlue.copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = BandBlue,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Text(
                        "胰岛素推荐剂量色带",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                }
                if (band != null) {
                    Text(
                        "建议基准: ${"%.1f".format(band.baseIU)} 单位",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            if (band != null) {
                Text(
                    "参考范围: ${"%.1f".format(band.minIU)} ~ ${"%.1f".format(band.maxIU)} 单位 (IU)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )

                Spacer(Modifier.height(12.dp))

                // 核心：基于实际输出的 blueRatio 与 greenRatio 动态按比例计算渐变锚点（普遍偏少时整体左翼）
                val totalSpan = (band.maxIU - band.minIU).coerceAtLeast(1e-6f)
                val blueRatio = ((band.blue.maxIU - band.blue.minIU) / totalSpan).coerceIn(0.04f, 0.85f)
                val greenRatio = ((band.green.maxIU - band.green.minIU) / totalSpan).coerceIn(0.04f, 0.85f)

                val b1 = (blueRatio * 0.70f).coerceIn(0f, 0.95f)
                val b2 = blueRatio.coerceIn(0f, 0.96f)
                val g1 = (blueRatio + greenRatio * 0.5f).coerceIn(0f, 0.97f)
                val g2 = (blueRatio + greenRatio).coerceIn(0f, 0.98f)

                val dynamicGradient = Brush.horizontalGradient(
                    0.0f to BandBlue,
                    b1 to BandBlue,
                    b2 to Color(0xFF36B39C),
                    g1 to BandGreen,
                    g2 to BandOrange,
                    1.0f to BandRed,
                )

                val targetRatio = ((band.baseIU - band.minIU) / totalSpan).coerceIn(0.05f, 0.95f)
                val animatedRatio by animateFloatAsState(
                    targetValue = targetRatio,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "doseCursorPos",
                )

                // 动态全光谱色带：平滑过渡，无生硬垂直切断白条
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(brush = dynamicGradient),
                ) {
                    val cursorCenter = maxWidth * animatedRatio

                    // 在色带内部放置柔和指示光环（而非生硬白块），保持色条连续过渡
                    Box(
                        modifier = Modifier
                            .offset(x = cursorCenter - 9.dp, y = 5.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.92f))
                            .padding(3.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                // 底部动态刻度文字，随基准自然呼应
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "${"%.1f".format(band.minIU)} 单位 (偏保守)",
                        style = MaterialTheme.typography.labelSmall,
                        color = BandBlue,
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                        modifier = Modifier.padding(vertical = 1.dp),
                    ) {
                        Text(
                            text = "基准 ${"%.1f".format(band.baseIU)} IU",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                    Text(
                        "${"%.1f".format(band.maxIU)} 单位 (高负荷上限)",
                        style = MaterialTheme.typography.labelSmall,
                        color = BandRed,
                    )
                }

                Spacer(Modifier.height(10.dp))

                BandSegmentSuggestion(
                    color = BandBlue,
                    label = "保守起始范围",
                    range = "${"%.1f".format(band.blue.minIU)}~${"%.1f".format(band.blue.maxIU)} 单位",
                    advice = "注射剂量偏保守，餐后血糖可能偏高，注意餐后 2 小时复测",
                )
                BandSegmentSuggestion(
                    color = BandGreen,
                    label = "推荐基准范围",
                    range = "${"%.1f".format(band.green.minIU)}~${"%.1f".format(band.green.maxIU)} 单位",
                    advice = "建议注射剂量在基准附近，通常控糖平稳达标",
                )
                BandSegmentSuggestion(
                    color = BandRed,
                    label = "高负荷警戒范围",
                    range = "${"%.1f".format(band.red.minIU)}~${"%.1f".format(band.red.maxIU)} 单位",
                    advice = "剂量已至上限，注意预防低血糖，请严格遵医嘱执行",
                )
            } else {
                Text("暂无数据", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun BandSegmentSuggestion(
    color: Color,
    label: String,
    range: String,
    advice: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.width(6.dp))
                Text(range, style = MaterialTheme.typography.labelSmall, color = color)
            }
            Text(
                text = advice,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun FocusPeriodCard(period: FocusPeriod, reminder: String?) {
    val levelColor = when (period.level) {
        FocusLevel.HIGH -> ThresholdHigh
        FocusLevel.ELEVATED -> ThresholdElevated
        FocusLevel.LOW -> ThresholdLow
    }
    val levelText = when (period.level) {
        FocusLevel.HIGH -> "高风险"
        FocusLevel.ELEVATED -> "偏高"
        FocusLevel.LOW -> "低血糖"
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, levelColor.copy(alpha = 0.4f)),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(levelColor.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(levelColor))
                }
                Spacer(Modifier.width(8.dp))
                Text(levelText, style = MaterialTheme.typography.labelSmall, color = levelColor, fontWeight = FontWeight.Bold)
            }
            Text(
                "${period.timeLabel}  ${"%.1f".format(period.bloodSugar)} mmol/L",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp),
            )
            if (!reminder.isNullOrBlank()) {
                Text(
                    reminder,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 2,
                )
            }
        }
    }
}

@Composable
private fun IngredientsBreakdownCard(
    recognitionResult: DishRecognitionResult?,
    carbMap: Map<String, Float>,
) {
    if (recognitionResult == null || recognitionResult.dishes.isEmpty()) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Restaurant,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Text(
                        text = "AI 识别食材与国家营养库核验",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = HealthPalette.pastelMint(),
                    modifier = Modifier.wrapContentWidth(Alignment.End),
                ) {
                    Text(
                        text = "智能分量估算",
                        style = MaterialTheme.typography.labelSmall,
                        color = HealthPalette.Dark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            recognitionResult.dishes.forEach { dish ->
                Text(
                    text = dish.dishName.ifBlank { "餐食菜品" },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 6.dp),
                )

                dish.ingredients.forEach { ingredient ->
                    val cat = FoodCategory.from(ingredient.category, ingredient.name)
                    val (badgeText, badgeColor) = when (cat) {
                        FoodCategory.STAPLE -> "🍚 主食" to Color(0xFFE67E22)
                        FoodCategory.VEGETABLE -> "🥦 素" to HealthPalette.Secondary
                        FoodCategory.MEAT -> "🥩 荤" to Color(0xFFE76F51)
                    }
                    // 权威中餐成分库优先匹配，防御历史异常值（如鸡蛋 57.7g、凉粉 98.9g、紫菜 52.4g）
                    val localStandard = BuiltInCarbFallback.lookup(ingredient.name)
                        ?: BuiltInCarbFallback.lookup(ingredient.nameEn ?: "")
                    val rawCarb = carbMap[ingredient.name]
                    val usdaCarb = localStandard ?: rawCarb

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = badgeColor.copy(alpha = 0.20f),
                                border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.45f)),
                            ) {
                                Text(
                                    text = badgeText,
                                    color = badgeColor,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = ingredient.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                )
                                Text(
                                    text = "${ingredient.weightG.toInt()}g · ${ingredient.cookingMethod} · 升糖指数 ${ingredient.giValue.toInt()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (usdaCarb != null) "基准: ${"%.1f".format(usdaCarb)}g/100g" else "基准: 查询中",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (usdaCarb != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                            )
                            if (usdaCarb != null) {
                                val estCarb = usdaCarb * ingredient.weightG / 100f
                                Text(
                                    text = "含碳水约 ${"%.1f".format(estCarb)}g",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SaveButton(
    isSaving: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = !isSaving,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Color.White,
            )
            Spacer(Modifier.width(8.dp))
            Text("正在持久化存储...", color = Color.White, fontWeight = FontWeight.Bold)
        } else {
            Icon(Icons.Filled.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "保存修改并更新档案",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
            )
        }
    }
}
