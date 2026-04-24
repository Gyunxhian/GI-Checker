package com.example.diabetesassistant.camera

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.diabetesassistant.R
import com.example.diabetesassistant.ui.components.GlassCard
import com.example.diabetesassistant.ui.components.GreenGlassBackground

private val ResultBackground = Color(0xFFF3F8F4)
private val ResultGreen = Color(0xFF008A3D)
private val ResultBrightGreen = Color(0xFF78FF9D)
private val ResultInk = Color(0xFF17251E)
private val ResultMuted = Color(0xFF6B7770)
private val ResultTrack = Color(0xFFE1ECE6)
private val ResultRed = Color(0xFFFF5A66)

@Composable
fun RecognitionConfirmScreen(
    viewModel: CameraRecognitionViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val ingredients by viewModel.recognizedIngredients.collectAsStateWithLifecycle()
    val calories = viewModel.estimatedCalories()

    GreenGlassBackground(modifier = modifier) {
        Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(top = 12.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        ResultTopBar(
            title = "识别结果确认",
            onBackClick = { navController.popBackStack(CameraRoute.Capture, inclusive = false) },
            trailingIcon = null,
        )

        RecognitionSummaryCard(count = ingredients.size)

        ingredients.forEachIndexed { index, ingredient ->
            IngredientConfirmCard(
                ingredient = ingredient,
                index = index,
                onDecrease = { viewModel.adjustIngredientWeight(index, -10) },
                onIncrease = { viewModel.adjustIngredientWeight(index, 10) },
                onDelete = { viewModel.removeIngredient(index) },
                onCookingMethodSelected = { method ->
                    viewModel.updateIngredientCookingMethod(index, method)
                },
                onSplitCooking = { viewModel.splitIngredientCooking(index) },
            )
        }

        Surface(
            onClick = { viewModel.addIngredient("手动食材") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = Color.White.copy(alpha = 0.58f),
        ) {
            Row(
                modifier = Modifier.padding(vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = ResultGreen,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "手动添加食材",
                    style = MaterialTheme.typography.titleSmall,
                    color = ResultGreen,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "预估总热量",
                style = MaterialTheme.typography.titleSmall,
                color = ResultMuted,
                fontWeight = FontWeight.Bold,
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = calories.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = ResultInk,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = " 千卡",
                    modifier = Modifier.padding(bottom = 5.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ResultMuted,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Surface(
            onClick = { navController.navigate(CameraRoute.Insights) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = ResultBrightGreen,
        ) {
            Row(
                modifier = Modifier.padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = ResultGreen,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(modifier = Modifier.width(9.dp))
                Text(
                    text = "确认分析",
                    style = MaterialTheme.typography.titleMedium,
                    color = ResultGreen,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
        }
    }
}

@Composable
private fun RecognitionSummaryCard(count: Int) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White.copy(alpha = 0.66f),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            IconBubble(Icons.Filled.CheckCircle, ResultGreen, Color(0xFFDFFCEC))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "智能识别出 $count 条食材做法",
                    style = MaterialTheme.typography.titleMedium,
                    color = ResultInk,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                text = "请分别校对每种食材的份量和烹饪方式。同一种食材若用了多种做法，可以拆分成多条记录。",
                    style = MaterialTheme.typography.bodySmall,
                    color = ResultMuted,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun IngredientConfirmCard(
    ingredient: RecognizedIngredient,
    index: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    onDelete: () -> Unit,
    onCookingMethodSelected: (CookingMethod) -> Unit,
    onSplitCooking: () -> Unit,
) {
    val colors = listOf(Color(0xFFEEE6D4), Color(0xFFFFF2DC), Color(0xFFE4F1DD), Color(0xFFE9EEF3))
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White.copy(alpha = 0.66f),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            FoodThumb(color = colors[index % colors.size])
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = ingredient.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = ResultInk,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Surface(onClick = onDelete, shape = CircleShape, color = Color.Transparent) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "删除",
                            tint = ResultMuted.copy(alpha = 0.62f),
                            modifier = Modifier
                                .padding(4.dp)
                                .size(20.dp),
                        )
                    }
                }
                Text(
                    text = "约 ${ingredient.caloriesPer100g} 千卡/100g · ${"%.1f".format(ingredient.sugarPer100g)}g 糖/100g",
                    style = MaterialTheme.typography.labelMedium,
                    color = ResultMuted,
                    fontWeight = FontWeight.Bold,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    WeightButton(icon = Icons.Filled.Remove, onClick = onDecrease)
                    Text(
                        text = "${ingredient.portionGrams}g",
                        style = MaterialTheme.typography.titleMedium,
                        color = ResultInk,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    WeightButton(icon = Icons.Filled.Add, onClick = onIncrease)
                }
                Text(
                    text = "烹饪方式",
                    style = MaterialTheme.typography.labelMedium,
                    color = ResultMuted,
                    fontWeight = FontWeight.Bold,
                )
                MethodChipRows(
                    selected = ingredient.cookingMethod,
                    onMethodSelected = onCookingMethodSelected,
                )
                Surface(
                    onClick = onSplitCooking,
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFEAF2EE),
                ) {
                    Text(
                        text = "拆分另一种做法",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = ResultGreen,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun CookingMethodCard(
    selected: CookingMethod,
    onMethodSelected: (CookingMethod) -> Unit,
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        containerColor = Color.White.copy(alpha = 0.62f),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "烹饪方式",
                style = MaterialTheme.typography.titleMedium,
                color = ResultInk,
                fontWeight = FontWeight.ExtraBold,
            )
            MethodChipRows(selected = selected, onMethodSelected = onMethodSelected)
        }
    }
}

@Composable
private fun MethodChipRows(
    selected: CookingMethod,
    onMethodSelected: (CookingMethod) -> Unit,
) {
    val rows = listOf(
        listOf(CookingMethod.Steam, CookingMethod.StirFry, CookingMethod.Boil),
        listOf(CookingMethod.Braise, CookingMethod.Roast, CookingMethod.ColdDish),
    )
    rows.forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { method ->
                MethodChip(
                    method = method,
                    selected = selected == method,
                    onClick = { onMethodSelected(method) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MethodChip(
    method: CookingMethod,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = if (selected) ResultBrightGreen.copy(alpha = 0.38f) else Color(0xFFEAF2EE),
    ) {
        Text(
            text = stringResource(method.labelRes),
            modifier = Modifier.padding(vertical = 9.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) ResultGreen else ResultMuted,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Composable
fun AiInsightsScreen(
    viewModel: CameraRecognitionViewModel,
    navController: NavHostController,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val ingredients by viewModel.recognizedIngredients.collectAsStateWithLifecycle()
    val sugar by viewModel.estimatedSugarGrams.collectAsStateWithLifecycle()
    val insulin by viewModel.suggestedInsulinUnits.collectAsStateWithLifecycle()
    val calories = viewModel.estimatedCalories()
    val sugarLevel = if (sugar >= 42f) "偏高" else "适中"
    val walkMinutes = if (sugar >= 42f) 25 else 15

    GreenGlassBackground(modifier = modifier) {
        Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(top = 12.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        ResultTopBar(
            title = "智能分析",
            onBackClick = { navController.popBackStack() },
            trailingIcon = Icons.Filled.MoreVert,
        )

        InsightsHeroCard(
            sugar = sugar,
            sugarLevel = sugarLevel,
        )

        Text(
            text = "成分拆解",
            style = MaterialTheme.typography.titleLarge,
            color = ResultInk,
            fontWeight = FontWeight.ExtraBold,
        )
        IngredientBreakdownGrid(ingredients = ingredients)

        Text(
            text = "行动建议",
            style = MaterialTheme.typography.titleLarge,
            color = ResultInk,
            fontWeight = FontWeight.ExtraBold,
        )
        ActionAdviceList(
            walkMinutes = walkMinutes,
            insulin = insulin,
        )

        Surface(
            onClick = {
                Toast.makeText(context, R.string.camera_saved_toast, Toast.LENGTH_SHORT).show()
                onFinish()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF20D45D),
        ) {
            Row(
                modifier = Modifier.padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = ResultGreen,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "加入今日记录",
                    style = MaterialTheme.typography.titleMedium,
                    color = ResultGreen,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }

        Text(
            text = "本次算法：总糖摄入量 = Σ(食材重量 × 每100g可吸收糖 × 该食材烹饪方式系数)；估算热量 ${calories} 千卡。药量仅作核对参考，请遵循医生方案。",
            style = MaterialTheme.typography.labelSmall,
            color = ResultMuted,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
        )
        }
    }
}

@Composable
private fun InsightsHeroCard(
    sugar: Float,
    sugarLevel: String,
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        containerColor = Color.White.copy(alpha = 0.68f),
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "本餐预计糖摄入",
                style = MaterialTheme.typography.titleSmall,
                color = ResultInk,
                fontWeight = FontWeight.ExtraBold,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "%.0f".format(sugar),
                    style = MaterialTheme.typography.displayMedium,
                    color = ResultInk,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = "g",
                    modifier = Modifier.padding(start = 6.dp, top = 18.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = ResultInk,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (sugarLevel == "偏高") Color(0xFFFFDDE3) else Color(0xFFDFFCEC),
                ) {
                    Text(
                        text = sugarLevel,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (sugarLevel == "偏高") ResultRed else ResultGreen,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFEAF2EE),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                        contentDescription = null,
                        tint = ResultGreen,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = "建议增加膳食纤维以延缓糖吸收速度。",
                        style = MaterialTheme.typography.bodySmall,
                        color = ResultInk,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun IngredientBreakdownGrid(ingredients: List<RecognizedIngredient>) {
    val rice = ingredients.maxByOrNull { estimatedSugarForIngredient(it) } ?: return
    val others = ingredients.filter { it != rice }.take(2)
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        GlassCard(
            modifier = Modifier
                .weight(1f)
                .height(170.dp),
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White.copy(alpha = 0.68f),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                IconBubble(Icons.Filled.Restaurant, ResultMuted, Color(0xFFEAF2EE))
                Text(
                    text = "${rice.name.substringBefore("（").substringBefore(" ")} · ${stringResource(rice.cookingMethod.labelRes)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = ResultMuted,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "%.0fg".format(estimatedSugarForIngredient(rice)),
                    style = MaterialTheme.typography.headlineSmall,
                    color = ResultInk,
                    fontWeight = FontWeight.ExtraBold,
                )
                LinearProgressIndicator(
                    progress = { 0.68f },
                    modifier = Modifier.fillMaxWidth(),
                    color = ResultRed,
                    trackColor = ResultTrack,
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            others.forEach { item ->
                BreakdownSmallItem(item)
            }
        }
    }
}

@Composable
private fun BreakdownSmallItem(item: RecognizedIngredient) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp),
        shape = RoundedCornerShape(22.dp),
        containerColor = Color.White.copy(alpha = 0.68f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            IconBubble(Icons.Filled.LocalDining, ResultMuted, Color(0xFFEAF2EE), size = 40.dp)
            Text(
                text = "${item.name.substringBefore("（").substringBefore(" ")} · ${stringResource(item.cookingMethod.labelRes)}",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
                color = ResultInk,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = "%.0fg".format(estimatedSugarForIngredient(item)),
                style = MaterialTheme.typography.titleMedium,
                color = ResultInk,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}

private fun estimatedSugarForIngredient(item: RecognizedIngredient): Float {
    val factor = when (item.cookingMethod) {
        CookingMethod.Braise -> 1.18f
        CookingMethod.Roast -> 1.12f
        CookingMethod.StirFry -> 1.08f
        CookingMethod.Boil -> 0.95f
        CookingMethod.ColdDish -> 1.05f
        CookingMethod.Steam -> 1f
        CookingMethod.Other -> 1.1f
    }
    return item.sugarPer100g * item.portionGrams / 100f * factor
}

@Composable
private fun ActionAdviceList(
    walkMinutes: Int,
    insulin: Float,
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White.copy(alpha = 0.68f),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            AdviceRow(
                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                title = "餐后步行 $walkMinutes 分钟",
                body = "餐后散步有助于平稳餐后糖吸收。",
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(ResultTrack),
            )
            AdviceRow(
                icon = Icons.Filled.LocalDining,
                title = "晚餐减少主食",
                body = "建议晚餐主食减半，或替换为粗粮。",
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(ResultTrack),
            )
            AdviceRow(
                icon = Icons.Filled.WaterDrop,
                title = "药量核对：%.1f 单位".format(insulin),
                body = "根据碳水系数估算，仅用于和医生方案核对。",
            )
        }
    }
}

@Composable
private fun AdviceRow(
    icon: ImageVector,
    title: String,
    body: String,
) {
    Row(
        modifier = Modifier.padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        IconBubble(icon, ResultGreen, Color(0xFFDFFCEC), size = 42.dp)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = ResultInk,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = ResultMuted,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ResultTopBar(
    title: String,
    onBackClick: () -> Unit,
    trailingIcon: ImageVector?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Surface(onClick = onBackClick, shape = CircleShape, color = Color.Transparent) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                tint = ResultGreen,
                modifier = Modifier
                    .padding(6.dp)
                    .size(28.dp),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = ResultInk,
            fontWeight = FontWeight.ExtraBold,
        )
        if (trailingIcon == null) {
            Spacer(modifier = Modifier.size(40.dp))
        } else {
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = ResultGreen,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun WeightButton(icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(34.dp),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.80f),
        shadowElevation = 4.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ResultMuted,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun FoodThumb(color: Color) {
    Box(
        modifier = Modifier
            .size(width = 72.dp, height = 72.dp)
            .background(color, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Restaurant,
            contentDescription = null,
            tint = ResultGreen.copy(alpha = 0.72f),
            modifier = Modifier.size(30.dp),
        )
    }
}

@Composable
private fun IconBubble(
    icon: ImageVector,
    tint: Color,
    background: Color,
    size: androidx.compose.ui.unit.Dp = 44.dp,
) {
    Box(
        modifier = Modifier
            .size(size)
            .background(background, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.52f),
        )
    }
}

@Immutable
private data class BreakdownUi(
    val label: String,
    val grams: String,
)
