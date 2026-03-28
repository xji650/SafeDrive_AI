package com.example.safedriveai.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SpeedometerCard() {
    Card(
        modifier = Modifier.fillMaxWidth().height(120.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("VELOCIDAD GPS", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Row(verticalAlignment = Alignment.Bottom) {
                Text("112", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Black, color = Color.White)
                Text("km/h", modifier = Modifier.padding(bottom = 12.dp), color = Color.Gray)
            }
            // Indicador de confianza de la IA
            LinearProgressIndicator(
                progress = { 0.1f }, // Nivel de riesgo bajo
                modifier = Modifier.width(100.dp).height(4.dp),
                color = NeonGreen,
                trackColor = Color.DarkGray
            )
        }
    }
}