package com.example.safedriveai.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SpeedometerCard(speed: Float, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(), // Usamos el modifier que entra por parámetro
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp) // Añadido para mantener el estilo
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Speed, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("VELOCIDAD GPS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                // AQUÍ ESTÁ LA MAGIA: Pasamos la velocidad real a entero
                Text(
                    text = "${speed.toInt()}",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "km/h",
                    modifier = Modifier.padding(bottom = 12.dp, start = 4.dp),
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Indicador de confianza de la IA
            LinearProgressIndicator(
                progress = { 0.1f }, // Puedes animar esto después si quieres
                modifier = Modifier.width(100.dp).height(4.dp),
                color = NeonGreen,
                trackColor = Color.DarkGray
            )
        }
    }
}