package com.example.diabetesassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/** Manual test entry for Qwen flow placeholder. */
@Composable
fun testQwen() {
    val result = remember { mutableStateOf("Ready") }
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Qwen Test", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = { result.value = "Not configured in reset state" }, modifier = Modifier.fillMaxWidth()) {
                Text("Run")
            }
            Text(result.value)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TestQwenPreview() {
    testQwen()
}