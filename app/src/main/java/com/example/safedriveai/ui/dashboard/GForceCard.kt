package com.example.safedriveai.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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

@Composable
fun GForceCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("INERCIA (G)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)

            Spacer(modifier = Modifier.height(8.dp))

            // El G-Meter Visual
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // 1. Círculo de referencia (ITV del diseño)
                    // NOTA: Usamos .toPx() para convertir Dp a Float
                    drawCircle(
                        color = Color.Gray.copy(alpha = 0.5f),
                        radius = size.minDimension / 2,
                        style = Stroke(width = 1.dp.toPx())
                    )

                    // 2. Ejes en cruz
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = Offset(size.width / 2, 0f),
                        end = Offset(size.width / 2, size.height),
                        strokeWidth = 1.dp.toPx()
                    )

                    // 3. LA BOLA (Calculamos posición)
                    val centerX = size.width / 2
                    val centerY = size.height / 2

                    // Simulación de movimiento (aquí irán tus variables del sensor)
                    val ballOffset = Offset(centerX + 15f, centerY - 10f)

                    // Brillo neón (círculo exterior suave)
                    drawCircle(
                        color = NeonGreen.copy(alpha = 0.3f),
                        radius = 10.dp.toPx(),
                        center = ballOffset
                    )

                    // Bola sólida
                    drawCircle(
                        color = NeonGreen,
                        radius = 5.dp.toPx(),
                        center = ballOffset
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Datos rápidos debajo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("LATERAL", color = Color.Gray, fontSize = 8.sp)
                    Text("0.12 G", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("LONG.", color = Color.Gray, fontSize = 8.sp)
                    Text("-0.05 G", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}