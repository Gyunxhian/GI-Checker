package com.example.diabetesassistant.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun ArchivesScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        Text("档案库", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // Simple Calendar Placeholder
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("2023年10月", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                // Grid of days placeholder
                Text("日历占位符 (一 二 三 四 五 六 日 ...)", color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("历史记录", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(5) { index ->
                ArchiveItem(index)
            }
        }
    }
}

@Composable
fun ArchiveItem(index: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color.LightGray, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("照片", fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("记录 $index - 鱼香肉丝", fontWeight = FontWeight.Medium)
                Text("血糖贡献: +2.1 mmol/L", fontSize = 12.sp, color = Color.Gray)
                Text("建议用量: 4单位", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
