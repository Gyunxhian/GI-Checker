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

/** Life/profile root screen. */
@Composable
fun LifeScreen(
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    onAnalysisPlanClick: () -> Unit = {},
    onMedicationClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onDarkModeClick: () -> Unit = {},
    onSyncClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {},
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Life", style = MaterialTheme.typography.headlineSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onProfileClick) { Text("Profile") }
                Button(onClick = onAnalysisPlanClick) { Text("Plan") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onMedicationClick) { Text("Medication") }
                Button(onClick = onNotificationClick) { Text("Notify") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onDarkModeClick) { Text("Dark") }
                Button(onClick = onSyncClick) { Text("Sync") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onPrivacyClick) { Text("Privacy") }
                Button(onClick = onSignOutClick) { Text("Sign Out") }
            }
        }
    }
}

/** Profile detail screen. */
@Composable
fun ProfileDetailScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    LifeSecondaryScreen("Profile Detail", onBackClick, modifier)
}

/** Analysis plan screen. */
@Composable
fun AnalysisPlanScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    LifeSecondaryScreen("Analysis Plan", onBackClick, modifier)
}

/** Medication plan screen. */
@Composable
fun MedicationPlanScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    LifeSecondaryScreen("Medication Plan", onBackClick, modifier)
}

/** Notification settings screen. */
@Composable
fun NotificationSettingsScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    LifeSecondaryScreen("Notification Settings", onBackClick, modifier)
}

/** Dark mode settings screen. */
@Composable
fun DarkModeSettingsScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    LifeSecondaryScreen("Dark Mode Settings", onBackClick, modifier)
}

/** Sync archive screen. */
@Composable
fun SyncArchiveScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    LifeSecondaryScreen("Sync Archive", onBackClick, modifier)
}

/** Privacy/data screen. */
@Composable
fun PrivacyDataScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    LifeSecondaryScreen("Privacy Data", onBackClick, modifier)
}

@Composable
private fun LifeSecondaryScreen(
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
private fun LifeScreenPreview() {
    LifeScreen()
}