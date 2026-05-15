package com.example.safedriveai.ui.dashboard.components

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safedriveai.ui.dashboard.CardBackground
import com.example.safedriveai.ui.dashboard.NeonGreen

@SuppressLint("DefaultLocale")
@Composable
fun GForceCardL(
    accelX: Float,
    accelY: Float,
    jerk: Float,
    angularVelocity: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Radar, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("INERCIA Y DINÁMICA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Radar a la izquierda
                Box(modifier = Modifier.size(100.dp).padding(4.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val radius = size.minDimension / 2
                        val centerX = size.width / 2
                        val centerY = size.height / 2

                        drawCircle(color = Color.Gray.copy(0.3f), style = Stroke(1.dp.toPx()))

                        val ballX = centerX + (-accelX * radius / 2f).coerceIn(-radius, radius)
                        val ballY = centerY + (accelY * radius / 2f).coerceIn(-radius, radius)

                        drawCircle(color = NeonGreen.copy(0.3f), radius = 8.dp.toPx(), center = Offset(ballX, ballY))
                        drawCircle(color = NeonGreen, radius = 4.dp.toPx(), center = Offset(ballX, ballY))
                    }
                }

                // Datos a la derecha
                Column(
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MetricRow("Jerk", String.format("%.1f", jerk), "G/s")
                    MetricRow("Giro", String.format("%.0f", angularVelocity), "º/s")
                }
            }

            // Valores X/Y abajo en pequeñito
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Text("X: ${String.format("%.2f", accelX)}G", color = Color.White.copy(0.7f), fontSize = 10.sp)
                Text("Y: ${String.format("%.2f", accelY)}G", color = Color.White.copy(0.7f), fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String, unit: String) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(label, color = Color.White.copy(0.6f), fontSize = 9.sp, modifier = Modifier.width(30.dp))
        Text(value, color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(2.dp))
        Text(unit, color = Color.White.copy(0.6f), fontSize = 8.sp)
    }
}
