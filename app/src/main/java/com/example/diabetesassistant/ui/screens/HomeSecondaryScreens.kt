package com.example.diabetesassistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.diabetesassistant.ui.components.GlassCard
import com.example.diabetesassistant.ui.components.GreenGlassBackground

private val SecondaryBackground = Color(0xFFF3F8F4)
private val SecondaryGreen = Color(0xFF008A3D)
private val SecondaryBrightGreen = Color(0xFF78FF9D)
private val SecondaryInk = Color(0xFF17251E)
private val SecondaryMuted = Color(0xFF6B7770)
private val SecondaryTrack = Color(0xFFE1ECE6)

@Composable
fun AiAdviceScreen(
    onBackClick: () -> Unit,
    onRecipeClick: () -> Unit = {},
    onExerciseClick: () -> Unit = {},
    onMedicationClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    GreenGlassBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            SecondaryTopBar(
            title = "智能分析建议",
                onBackClick = onBackClick,
                trailingIcon = Icons.Filled.SelfImprovement,
            )
            TodayInsightCard()
            ExerciseAdviceCard()
            DietAdviceCard(onRecipeClick = onRecipeClick)
            RiskReminderCard()
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                HabitMiniCard(
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                    title = "餐后运动",
                    subtitle = "目标 30 分钟",
                    footer = "建议餐后 20 分钟开始",
                    modifier = Modifier.weight(1f),
                    onClick = onExerciseClick,
                )
                HabitMiniCard(
                    icon = Icons.Filled.WaterDrop,
                    title = "药量核对",
                subtitle = "参考 4.5 单位",
                    footer = "请结合个人医嘱确认",
                    modifier = Modifier.weight(1f),
                    onClick = onMedicationClick,
                )
            }
        }
    }
}

@Composable
private fun TodayInsightCard() {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White.copy(alpha = 0.66f),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                IconBubble(Icons.Filled.SelfImprovement, SecondaryGreen, Color(0xFFDFFCEC))
                Column {
                    Text(
                        text = "午餐分析",
                        style = MaterialTheme.typography.titleMedium,
                        color = SecondaryInk,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = "基于今日 12:45 拍照识别",
                        style = MaterialTheme.typography.labelMedium,
                        color = SecondaryMuted,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Text(
                text = "本餐识别为米饭、鸡胸肉和西兰花，估算可吸收糖分约 45g。建议餐后低强度步行，并按医嘱核对餐前或餐后用药剂量。",
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryInk,
                fontWeight = FontWeight.SemiBold,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
            )
        }
    }
}

@Composable
private fun ExerciseAdviceCard() {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        containerColor = Color.White.copy(alpha = 0.62f),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AdviceHeader(
                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                chip = "今日",
                tint = SecondaryGreen,
            )
            Text(
                text = "餐后运动建议",
                style = MaterialTheme.typography.titleMedium,
                color = SecondaryInk,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = "步行 30 分钟，强度保持可交谈",
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryMuted,
                fontWeight = FontWeight.Bold,
            )
            LinearProgressIndicator(
                progress = { 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp),
                color = SecondaryGreen,
                trackColor = SecondaryTrack,
            )
            Text(
                text = "已达成 0/30 分钟",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelSmall,
                color = SecondaryMuted,
                textAlign = TextAlign.End,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun DietAdviceCard(onRecipeClick: () -> Unit) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        containerColor = Color.White.copy(alpha = 0.62f),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AdviceHeader(
                icon = Icons.Filled.LocalDining,
                chip = "晚餐",
                tint = SecondaryGreen,
            )
            Text(
                text = "下餐搭配建议",
                style = MaterialTheme.typography.titleMedium,
                color = SecondaryInk,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = "晚餐减少精制主食，增加蔬菜和蛋白质",
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryMuted,
                fontWeight = FontWeight.Bold,
            )
            Surface(
                onClick = onRecipeClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFEAF2EE),
            ) {
                Text(
                    text = "查看食谱",
                    modifier = Modifier.padding(vertical = 10.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = SecondaryGreen,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

@Composable
private fun RiskReminderCard() {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        containerColor = Color.White.copy(alpha = 0.62f),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            IconBubble(Icons.Filled.Warning, Color(0xFFE85252), Color(0xFFFFE1E1))
            Column {
                Text(
                    text = "药量核对提醒",
                    style = MaterialTheme.typography.titleMedium,
                    color = SecondaryInk,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = "本餐糖负荷偏中等，若使用胰岛素或口服药，请按医生设定的碳水系数核对剂量。",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryMuted,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun HabitMiniCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    footer: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    GlassCard(
        modifier = modifier
            .height(150.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        containerColor = Color.White.copy(alpha = 0.66f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            IconBubble(icon, Color(0xFF3E8BFF), Color(0xFFE4ECFF))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = SecondaryInk,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = SecondaryMuted,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = footer,
                style = MaterialTheme.typography.labelSmall,
                color = SecondaryMuted,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun AdviceHeader(
    icon: ImageVector,
    chip: String,
    tint: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconBubble(icon, tint, Color(0xFFE1F8EA))
        Surface(
            shape = RoundedCornerShape(50),
            color = SecondaryBrightGreen.copy(alpha = 0.28f),
        ) {
            Text(
                text = chip,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                style = MaterialTheme.typography.labelLarge,
                color = SecondaryGreen,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}

@Composable
fun FoodDiaryDetailScreen(
    onBackClick: () -> Unit,
    onAddMealClick: () -> Unit,
    onMealClick: (String) -> Unit = {},
    onEditMealClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    GreenGlassBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
                .padding(top = 12.dp, bottom = 18.dp),
        ) {
            SecondaryTopBar(
                title = "今日餐食分析详情",
                onBackClick = onBackClick,
                trailingIcon = Icons.Filled.MoreVert,
            )
            Spacer(modifier = Modifier.height(24.dp))
            FoodDiarySummary()
            Spacer(modifier = Modifier.height(20.dp))
            MealTimeline(
                modifier = Modifier.weight(1f),
                onMealClick = onMealClick,
                onEditMealClick = onEditMealClick,
            )
            Surface(
                onClick = onAddMealClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = SecondaryBrightGreen,
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddCircleOutline,
                        contentDescription = null,
                        tint = SecondaryGreen,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "拍照分析新餐",
                        style = MaterialTheme.typography.titleSmall,
                        color = SecondaryGreen,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun FoodDiarySummary() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(50),
            color = SecondaryBrightGreen,
        ) {
            Text(
                text = "2023年10月24日 星期二",
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
                style = MaterialTheme.typography.labelLarge,
                color = SecondaryGreen,
                fontWeight = FontWeight.ExtraBold,
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            SummaryMetric("总热量", "1850", "千卡")
            Spacer(modifier = Modifier.width(48.dp))
            SummaryMetric("可吸收糖分", "45", "g")
        }
    }
}

@Composable
private fun SummaryMetric(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = SecondaryMuted,
            fontWeight = FontWeight.Bold,
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = SecondaryInk,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = unit,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryMuted,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun MealTimeline(
    modifier: Modifier = Modifier,
    onMealClick: (String) -> Unit,
    onEditMealClick: () -> Unit,
) {
    val meals = listOf(
        MealDetail("home_breakfast", "早餐", "07:30", "燕麦粥和牛奶", "全脂牛奶 200ml，燕麦 50g", "320 千卡", "12g 糖", "步行 15 分钟", Color(0xFF1F2B35)),
        MealDetail("home_lunch", "午餐", "12:15", "米饭 鸡胸肉", "糙米饭 150g，烤鸡胸肉 120g，西兰花", "550 千卡", "45g 糖", "参考 4.5 单位", Color(0xFF2F2F2F)),
        MealDetail("home_snack", "加餐", "15:30", "苹果", "中等大小苹果 1个", "95 千卡", "19g 糖", "无需额外运动", Color(0xFF171F18)),
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        meals.forEachIndexed { index, meal ->
            TimelineMealItem(
                meal = meal,
                active = index < 2,
                showLine = index != meals.lastIndex,
                onMealClick = onMealClick,
                onEditMealClick = onEditMealClick,
            )
        }
    }
}

@Composable
private fun TimelineMealItem(
    meal: MealDetail,
    active: Boolean,
    showLine: Boolean,
    onMealClick: (String) -> Unit,
    onEditMealClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(if (active) SecondaryGreen else SecondaryBackground, CircleShape)
                    .border(2.dp, SecondaryGreen, CircleShape),
            )
            if (showLine) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(124.dp)
                        .background(SecondaryGreen.copy(alpha = 0.16f)),
                )
            }
        }
        Spacer(modifier = Modifier.width(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = meal.type,
                        style = MaterialTheme.typography.titleMedium,
                        color = SecondaryInk,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = "  ${meal.time}",
                        style = MaterialTheme.typography.labelLarge,
                        color = SecondaryMuted,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Surface(onClick = onEditMealClick, shape = CircleShape, color = Color.Transparent) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "编辑",
                        tint = SecondaryMuted.copy(alpha = 0.55f),
                        modifier = Modifier
                            .padding(4.dp)
                            .size(18.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onMealClick(meal.id) },
                shape = RoundedCornerShape(22.dp),
                elevation = 8.dp,
                containerColor = Color.White.copy(alpha = 0.70f),
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    FoodPhotoPlaceholder(color = meal.photoColor)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = meal.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = SecondaryInk,
                            fontWeight = FontWeight.ExtraBold,
                        )
                        Text(
                            text = meal.description,
                            style = MaterialTheme.typography.labelMedium,
                            color = SecondaryMuted,
                            fontWeight = FontWeight.Bold,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            NutritionChip(meal.calories, Icons.Filled.Flag)
                            NutritionChip(meal.sugar, Icons.Filled.WaterDrop)
                            NutritionChip(meal.advice, Icons.AutoMirrored.Filled.DirectionsWalk)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodPhotoPlaceholder(color: Color) {
    Box(
        modifier = Modifier
            .size(width = 78.dp, height = 72.dp)
            .background(color, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Restaurant,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.82f),
            modifier = Modifier.size(26.dp),
        )
    }
}

@Composable
private fun NutritionChip(text: String, icon: ImageVector) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color(0xFFEAF2EE),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SecondaryMuted,
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = if (text.contains("糖")) SecondaryGreen else SecondaryMuted,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun SecondaryTopBar(
    title: String,
    onBackClick: () -> Unit,
    trailingIcon: ImageVector,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Surface(
            onClick = onBackClick,
            shape = CircleShape,
            color = Color.Transparent,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                tint = SecondaryGreen,
                modifier = Modifier
                    .padding(6.dp)
                    .size(22.dp),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = SecondaryGreen,
            fontWeight = FontWeight.ExtraBold,
        )
        Icon(
            imageVector = trailingIcon,
            contentDescription = null,
            tint = SecondaryGreen,
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
private fun IconBubble(
    icon: ImageVector,
    tint: Color,
    background: Color,
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .background(background, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
fun RecipeSuggestionScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HomeDetailScaffold(
        title = "下餐食谱建议",
        onBackClick = onBackClick,
        trailingIcon = Icons.Filled.LocalDining,
        modifier = modifier,
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "晚餐搭配目标",
                    style = MaterialTheme.typography.titleMedium,
                    color = SecondaryInk,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = "午餐糖摄入约 45g，晚餐建议减少精制主食，优先选择高纤维蔬菜和优质蛋白。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryMuted,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        RecipeOptionCard("清蒸鱼配时蔬", "鱼肉 150g · 西兰花 120g · 半碗糙米", "预计糖摄入 22g")
        RecipeOptionCard("番茄豆腐鸡蛋汤", "豆腐 100g · 鸡蛋 1个 · 番茄 180g", "预计糖摄入 12g")
        RecipeOptionCard("鸡胸肉蔬菜碗", "鸡胸肉 120g · 生菜 150g · 少量藜麦", "预计糖摄入 18g")
    }
}

@Composable
fun ExercisePlanScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HomeDetailScaffold(
        title = "餐后运动计划",
        onBackClick = onBackClick,
        trailingIcon = Icons.AutoMirrored.Filled.DirectionsWalk,
        modifier = modifier,
    ) {
        ExerciseStepCard("餐后 20 分钟", "开始低强度步行，保持可以轻松说话的速度。", "5 分钟热身")
        ExerciseStepCard("主运动阶段", "连续步行 20 分钟，避免剧烈跑跳。", "20 分钟")
        ExerciseStepCard("结束放松", "放慢速度，补水并观察身体状态。", "5 分钟")
        GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("今日进度", style = MaterialTheme.typography.titleMedium, color = SecondaryInk, fontWeight = FontWeight.ExtraBold)
                LinearProgressIndicator(
                    progress = { 0.35f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = SecondaryGreen,
                    trackColor = SecondaryTrack,
                )
                Text("已完成 10/30 分钟", style = MaterialTheme.typography.labelMedium, color = SecondaryMuted, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MedicationReviewScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HomeDetailScaffold(
        title = "药量核对",
        onBackClick = onBackClick,
        trailingIcon = Icons.Filled.WaterDrop,
        modifier = modifier,
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("本餐核对参考", style = MaterialTheme.typography.titleMedium, color = SecondaryInk, fontWeight = FontWeight.ExtraBold)
                Text("估算可吸收糖分 45g，按当前碳水换算系数可参考 4.5 单位。该结果只用于和医生方案核对。", style = MaterialTheme.typography.bodyMedium, color = SecondaryMuted, fontWeight = FontWeight.SemiBold)
            }
        }
        ReviewInfoRow("碳水换算系数", "10g 糖 / 1 单位")
        ReviewInfoRow("当前药物方案", "二甲双胍 · 胰岛素剂量核对")
        ReviewInfoRow("提醒等级", "中等，建议餐后观察")
        ReviewInfoRow("安全提示", "不自动修改药量，请遵循医生方案")
    }
}

@Composable
fun MealEditScreen(
    onBackClick: () -> Unit,
    onRescanClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HomeDetailScaffold(
        title = "编辑餐食分析",
        onBackClick = onBackClick,
        trailingIcon = Icons.Filled.Edit,
        modifier = modifier,
    ) {
        ReviewInfoRow("餐次", "午餐 12:15")
        ReviewInfoRow("餐食", "米饭、鸡胸肉、西兰花")
        ReviewInfoRow("烹饪方式", "米饭蒸煮 · 鸡胸肉烤制 · 西兰花焯水")
        ReviewInfoRow("估算糖摄入", "45g")
        Surface(
            onClick = onRescanClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = SecondaryBrightGreen,
        ) {
            Row(
                modifier = Modifier.padding(vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Filled.AddCircleOutline, contentDescription = null, tint = SecondaryGreen, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("重新拍照分析", style = MaterialTheme.typography.titleSmall, color = SecondaryGreen, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun HomeDetailScaffold(
    title: String,
    onBackClick: () -> Unit,
    trailingIcon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    GreenGlassBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            SecondaryTopBar(
                title = title,
                onBackClick = onBackClick,
                trailingIcon = trailingIcon,
            )
            content()
        }
    }
}

@Composable
private fun RecipeOptionCard(
    title: String,
    body: String,
    sugar: String,
) {
    GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), elevation = 8.dp) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            IconBubble(Icons.Filled.Restaurant, SecondaryGreen, Color(0xFFE1F8EA))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = SecondaryInk, fontWeight = FontWeight.ExtraBold)
                Text(body, style = MaterialTheme.typography.bodySmall, color = SecondaryMuted, fontWeight = FontWeight.SemiBold)
                Text(sugar, style = MaterialTheme.typography.labelMedium, color = SecondaryGreen, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun ExerciseStepCard(
    title: String,
    body: String,
    badge: String,
) {
    GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), elevation = 8.dp) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            IconBubble(Icons.AutoMirrored.Filled.DirectionsWalk, SecondaryGreen, Color(0xFFE1F8EA))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = SecondaryInk, fontWeight = FontWeight.ExtraBold)
                Text(body, style = MaterialTheme.typography.bodySmall, color = SecondaryMuted, fontWeight = FontWeight.SemiBold)
            }
            Surface(shape = RoundedCornerShape(50), color = SecondaryBrightGreen.copy(alpha = 0.30f)) {
                Text(badge, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall, color = SecondaryGreen, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun ReviewInfoRow(
    label: String,
    value: String,
) {
    GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), elevation = 8.dp) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = SecondaryMuted, fontWeight = FontWeight.Bold)
            Text(value, modifier = Modifier.padding(start = 16.dp), style = MaterialTheme.typography.labelLarge, color = SecondaryInk, textAlign = TextAlign.End, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Immutable
private data class MealDetail(
    val id: String,
    val type: String,
    val time: String,
    val title: String,
    val description: String,
    val calories: String,
    val sugar: String,
    val advice: String,
    val photoColor: Color,
)
