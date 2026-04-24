package com.example.diabetesassistant.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.diabetesassistant.Qwen3VL

// 🔥 这是最终无参数版，能直接显示
@Composable
fun testQwen() {
    val context = LocalContext.current
    val activity = context as ComponentActivity

    val qwenVL = remember { Qwen3VL(activity) }

    var aiResult by remember { mutableStateOf("点击按钮拍照分析") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {
                isLoading = true
                qwenVL.startAnalyzePhoto { result ->
                    isLoading = false
                    aiResult = result ?: "分析失败"
                }
            },
            enabled = !isLoading
        ) {
            Text(if (isLoading) "AI分析中..." else "📷 拍照并分析")
        }

        Text(
            text = aiResult,
            modifier = Modifier.padding(top = 20.dp)
        )
    }
}