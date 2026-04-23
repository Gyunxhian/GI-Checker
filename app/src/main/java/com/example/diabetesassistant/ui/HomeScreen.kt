package com.example.diabetesassistant.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("你好, 控糖达人", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        // Recent Blood Sugar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("最近一次血糖测量值", color = Color.Gray)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("5.6", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                        Text(" mmol/L", fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
                        Spacer(modifier = Modifier.weight(1f))
                        Text("1小时前", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }

        // Blood Sugar Change (Placeholder for Chart)
        item {
            Card(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("血糖变化趋势图 (图表占位)", color = Color.Gray)
                }
            }
        }

        // Today's Intake
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("今日摄入统计", color = Color.Gray)
                        Text("1250 kcal", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    // Simple progress circle or icon could go here
                }
            }
        }

        // Suggestions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("控糖建议", fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                    Text("午餐建议增加绿叶蔬菜摄入，减少精制碳水。", fontSize = 14.sp)
                }
            }
        }

        // Quick Feedback
        item {
            Text("快捷反馈", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)) {
                    Text("感觉良好")
                }
                Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)) {
                    Text("有点头晕")
                }
            }
        }
    }
}
