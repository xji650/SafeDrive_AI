package com.example.safedriveai.ui.edr.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.safedriveai.data.model.EdrModel

@Composable
fun TelemetryGraph(data: List<EdrModel>) {
    Card(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        val lineColor = MaterialTheme.colorScheme.error

        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            if (data.isEmpty()) return@Canvas

            val width = size.width
            val height = size.height

            // Buscamos el pico máximo para que la gráfica no se salga por arriba
            val maxG = data.maxOf { it.gForce }.coerceAtLeast(1f) // Mínimo 1G para que no se aplane si estás parado

            val path = Path()

            // Distancia en X entre cada punto de datos
            val stepX = width / (data.size - 1).toFloat()

            data.forEachIndexed { index, snapshot ->
                val x = index * stepX

                // Calculamos la Y: 0G abajo, maxG arriba. (Invertimos porque en Canvas el 0 es arriba)
                val normalizedY = 1f - (snapshot.gForce / maxG)
                val y = normalizedY * height

                if (index == 0) {
                    path.moveTo(x, y) // Empezamos a trazar
                } else {
                    path.lineTo(x, y) // Unimos el punto anterior con el nuevo
                }
            }

            // Dibujamos la línea matemática
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}