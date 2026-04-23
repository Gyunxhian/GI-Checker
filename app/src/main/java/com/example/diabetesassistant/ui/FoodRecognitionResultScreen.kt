package com.example.diabetesassistant.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FoodRecognitionResultScreen() {
    var vegMeatRatio by remember { mutableStateOf(0.3f) }
    var totalIntake by remember { mutableStateOf(0.5f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Image Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "图片", fontSize = 48.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Veg/Meat Slider
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("素", fontSize = 18.sp)
            Slider(
                value = vegMeatRatio,
                onValueChange = { vegMeatRatio = it },
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Color.Gray,
                    activeTrackColor = Color.Gray
                )
            )
            Text("荤", fontSize = 18.sp)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("0", fontSize = 12.sp, color = Color.Gray)
            Text("0.5", fontSize = 12.sp, color = Color.Gray)
            Text("1", fontSize = 12.sp, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Total Intake Slider
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("总体摄入量!", fontSize = 16.sp)
            Slider(
                value = totalIntake,
                onValueChange = { totalIntake = it },
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Color.Gray,
                    activeTrackColor = Color.Gray
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Blood Sugar Prediction Chart
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("血糖波动预测!", fontSize = 16.sp)
            Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                val path = Path()
                path.moveTo(0f, size.height * 0.7f)
                path.quadraticTo(size.width * 0.2f, size.height * 0.9f, size.width * 0.4f, size.height * 0.7f)
                path.quadraticTo(size.width * 0.6f, size.height * 0.2f, size.width * 0.8f, size.height * 0.6f)
                path.lineTo(size.width, size.height * 0.8f)
                
                drawPath(
                    path = path,
                    color = Color.Gray,
                    style = Stroke(width = 4f)
                )
                drawLine(Color.Gray, start = androidx.compose.ui.geometry.Offset(0f, size.height), end = androidx.compose.ui.geometry.Offset(size.width, size.height))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Insulin Dosage
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("胰岛素用量!", fontSize = 16.sp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Cyan, Color.Green, Color.Yellow, Color.Red)
                        )
                    )
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Text("XX~XX", fontSize = 12.sp)
                Text("XX~XX", fontSize = 12.sp)
                Text("XX~XX", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Key Focus Periods
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("重点关注时段!", fontSize = 16.sp, modifier = Modifier.weight(0.3f))
            Column(modifier = Modifier.weight(0.7f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    InfoBox("xx小时后可能会出现xx的症状")
                    InfoBox("xx小时后可能会出现xx的症状")
                    InfoBox("xx小时后可能会出现xx的症状")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(4) {
                IconButton(onClick = {}, modifier = Modifier.size(48.dp).background(Color.White, RoundedCornerShape(8.dp))) {
                    Text("icon", fontSize = 10.sp)
                }
            }
            Button(
                onClick = { },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
            ) {
                Text("确定", fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun InfoBox(text: String) {
    Box(
        modifier = Modifier
            .width(80.dp)
            .padding(2.dp)
    ) {
        Text(text = text, fontSize = 10.sp, color = Color.Gray)
    }
}
