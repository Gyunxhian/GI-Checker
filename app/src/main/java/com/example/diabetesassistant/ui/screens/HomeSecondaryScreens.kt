package com.example.diabetesassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/** AI advice screen. */
@Composable
fun AiAdviceScreen(
    onBackClick: () -> Unit,
    onRecipeClick: () -> Unit = {},
    onExerciseClick: () -> Unit = {},
    onMedicationClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("AI Advice", style = MaterialTheme.typography.headlineSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onBackClick) { Text("Back") }
                Button(onClick = onRecipeClick) { Text("Recipe") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onExerciseClick) { Text("Exercise") }
                Button(onClick = onMedicationClick) { Text("Medication") }
            }
        }
    }
}

/** Food diary detail screen. */
@Composable
fun FoodDiaryDetailScreen(
    onBackClick: () -> Unit,
    onAddMealClick: () -> Unit = {},
    onMealClick: (String) -> Unit = {},
    onEditMealClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Food Diary", style = MaterialTheme.typography.headlineSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onBackClick) { Text("Back") }
                Button(onClick = onAddMealClick) { Text("Add Meal") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onMealClick("meal-1") }) { Text("Open Meal") }
                Button(onClick = onEditMealClick) { Text("Edit Meal") }
            }
        }
    }
}

/** Recipe suggestion screen. */
@Composable
fun RecipeSuggestionScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SecondarySimpleScreen(title = "Recipe Suggestion", onBackClick = onBackClick, modifier = modifier)
}

/** Exercise plan screen. */
@Composable
fun ExercisePlanScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SecondarySimpleScreen(title = "Exercise Plan", onBackClick = onBackClick, modifier = modifier)
}

/** Medication review screen. */
@Composable
fun MedicationReviewScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SecondarySimpleScreen(title = "Medication Review", onBackClick = onBackClick, modifier = modifier)
}

/** Meal edit screen. */
@Composable
fun MealEditScreen(
    onBackClick: () -> Unit,
    onRescanClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Meal Edit", style = MaterialTheme.typography.headlineSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onBackClick) { Text("Back") }
                Button(onClick = onRescanClick) { Text("Rescan") }
            }
        }
    }
}

@Composable
private fun SecondarySimpleScreen(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Button(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) {
                Text("Back")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AiAdviceScreenPreview() {
    AiAdviceScreen(onBackClick = {})
}
