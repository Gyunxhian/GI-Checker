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

/** Archive entry screen. */
@Composable
fun ArchiveScreen(
    modifier: Modifier = Modifier,
    onViewAllClick: () -> Unit = {},
    onDayClick: () -> Unit = {},
    onRecordClick: (String) -> Unit = {},
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Archive", style = MaterialTheme.typography.headlineSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onViewAllClick) { Text("View All") }
                Button(onClick = onDayClick) { Text("Day Detail") }
            }
            Button(onClick = { onRecordClick("meal-1") }, modifier = Modifier.fillMaxWidth()) {
                Text("Open Record")
            }
        }
    }
}

/** Archive all analyses screen. */
@Composable
fun ArchiveAllAnalysesScreen(
    onBackClick: () -> Unit,
    onRecordClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("All Analyses", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = onBackClick) { Text("Back") }
            Button(onClick = { onRecordClick("meal-1") }) { Text("Open Record") }
        }
    }
}

/** Archive daily detail screen. */
@Composable
fun ArchiveDayDetailScreen(
    onBackClick: () -> Unit,
    onRecordClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Day Detail", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = onBackClick) { Text("Back") }
            Button(onClick = { onRecordClick("meal-1") }) { Text("Open Record") }
        }
    }
}

/** Archive meal detail screen. */
@Composable
fun ArchiveMealDetailScreen(
    onBackClick: () -> Unit,
    mealId: String,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Meal Detail", style = MaterialTheme.typography.headlineSmall)
            Text("Meal ID: $mealId")
            Button(onClick = onBackClick) { Text("Back") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ArchiveScreenPreview() {
    ArchiveScreen()
}