package com.example.safedriveai.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GForceCard(accelX: Float, accelY: Float) { // Recibe los datos reales
    Card(
        modifier = Modifier.fillMaxWidth().height(220.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("INERCIA EN TIEMPO REAL", color = Color.Gray, fontSize = 10.sp)

            Box(modifier = Modifier.size(120.dp).padding(8.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2
                    val centerX = size.width / 2
                    val centerY = size.height / 2

                    // Dibujo del radar de Gs
                    drawCircle(color = Color.Gray.copy(0.3f), style = Stroke(1.dp.toPx()))

                    // Lógica de movimiento:
                    // Multiplicamos por el radio para que la bola no se salga del círculo
                    // Usamos -accelX porque el sensor de Android es invertido al movimiento físico
                    val ballX = centerX + (-accelX * radius).coerceIn(-radius, radius)
                    val ballY = centerY + (accelY * radius).coerceIn(-radius, radius)

                    // La bola con "Glow"
                    drawCircle(color = NeonGreen.copy(0.3f), radius = 12.dp.toPx(), center = Offset(ballX, ballY))
                    drawCircle(color = NeonGreen, radius = 6.dp.toPx(), center = Offset(ballX, ballY))
                }
            }

            // Texto dinámico con los valores
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Text("LAT: ${String.format("%.2f", accelX)}G", color = Color.White, fontSize = 12.sp)
                Text("LON: ${String.format("%.2f", accelY)}G", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}