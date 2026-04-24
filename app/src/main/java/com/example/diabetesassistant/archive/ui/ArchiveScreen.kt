package com.example.diabetesassistant.archive.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.diabetesassistant.archive.model.DishAnalysis
import com.example.diabetesassistant.archive.model.MealRecord
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ArchiveScreen(modifier: Modifier = Modifier) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        HorizontalCalendar(
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it },
        )

        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        )

        MealHistoryList(
            selectedDate = selectedDate,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun HorizontalCalendar(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val days = remember {
        (-14..14).map { LocalDate.now().plusDays(it.toLong()) }
    }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(days) { date ->
            CalendarDayItem(
                date = date,
                isSelected = date == selectedDate,
                onClick = { onDateSelected(date) },
            )
        }
    }
}

@Composable
fun CalendarDayItem(
    date: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINESE)
    val dayOfMonth = date.dayOfMonth.toString()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = dayOfWeek,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        Text(
            text = dayOfMonth,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
    }
}

@Composable
fun MealHistoryList(
    selectedDate: LocalDate,
    modifier: Modifier = Modifier,
) {
    val meals = remember(selectedDate) { getMockMealsForDate(selectedDate) }

    if (meals.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text("当天暂无记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(meals) { meal ->
                MealRecordItem(meal = meal)
            }
        }
    }
}

@Composable
fun MealRecordItem(meal: MealRecord) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "rotation",
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("餐照", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = meal.mealType,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = meal.timestamp.toLocalTime().toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "热量: ${meal.totalCalories.toInt()} kcal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = "展开",
                    modifier = Modifier.rotate(rotationState),
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "营养详情",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )

                    meal.dishes.forEach { dish ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = dish.name,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1.5f),
                            )
                            Text(
                                text = "${dish.calories.toInt()} kcal",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = "${dish.carbs.toInt()}g 碳水",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1.2f),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getMockMealsForDate(date: LocalDate): List<MealRecord> {
    return listOf(
        MealRecord(
            id = 1,
            timestamp = date.atTime(8, 30),
            date = date,
            imageUrl = "",
            mealType = "早餐",
            totalCalories = 450.0,
            totalCarbs = 60.0,
            totalProtein = 15.0,
            totalFat = 10.0,
            dishes = listOf(
                DishAnalysis("全麦面包", 100.0, 250.0, 45.0, 8.0, 3.0),
                DishAnalysis("牛奶", 200.0, 130.0, 14.0, 7.0, 6.0),
            ),
        ),
        MealRecord(
            id = 2,
            timestamp = date.atTime(12, 15),
            date = date,
            imageUrl = "",
            mealType = "午餐",
            totalCalories = 720.0,
            totalCarbs = 90.0,
            totalProtein = 35.0,
            totalFat = 25.0,
            dishes = listOf(
                DishAnalysis("糙米饭", 150.0, 180.0, 40.0, 4.0, 1.0),
                DishAnalysis("西兰花炒鸡胸肉", 200.0, 350.0, 15.0, 25.0, 15.0),
            ),
        ),
    )
}
