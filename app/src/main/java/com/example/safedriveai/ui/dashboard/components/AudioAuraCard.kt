package com.example.safedriveai.ui.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import com.example.safedriveai.ui.dashboard.CardBackground
import com.example.safedriveai.ui.dashboard.EmergencyRed
import com.example.safedriveai.ui.dashboard.NeonGreen

@Composable
fun AudioAuraCard(amplitude: Float, modifier: Modifier = Modifier) {
    // Lógica de estados dinámicos
    val dbValue = (30 + (amplitude * 80)).toInt()

    val statusText = when {
        amplitude > 0.8f -> "¡ESTRUENDO DETECTADO!"
        amplitude > 0.5f -> "RUIDO ELEVADO"
        else -> "AMBIENTE NORMAL"
    }

    val statusColor = when {
        amplitude > 0.8f -> EmergencyRed
        amplitude > 0.5f -> Color.Yellow
        else -> NeonGreen
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                Text("ANÁLISIS ACÚSTICO", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            // Visualizador de "Aura" reactivo
            Row(
                Modifier.fillMaxWidth().height(40.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(7) { index -> // Aumentamos a 7 barras para que se vea más denso
                    // Variamos el factor para que las barras centrales sean más altas
                    val distFromCenter = Math.abs(index - 3)
                    val factor = 1f - (distFromCenter * 0.2f)
                    val heightVal = (amplitude * 35f * factor).coerceAtLeast(4f)

                    Surface(
                        modifier = Modifier.width(3.dp).height(heightVal.dp),
                        color = statusColor,
                        shape = RoundedCornerShape(2.dp)
                    ) {}
                }
            }

            // Texto de estado dinámico
            Text(
                text = statusText + " (${dbValue}dB)",
                color = statusColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}