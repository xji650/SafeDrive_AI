package com.example.safedriveai.ui.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.foundation.layout.fillMaxSize

@Composable
fun AudioAuraCard(modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        // 1. El Card recibe el modifier (con el weight del padre)
        modifier = modifier.fillMaxWidth()
    ) {
        // 2. ¡AQUÍ ESTÁ LA SOLUCIÓN! fillMaxSize() antes del padding
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {

            // --- Cabecera ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("ANÁLISIS ACÚSTICO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- Visualizador de ondas ---
            Row(
                modifier = Modifier.fillMaxWidth().height(40.dp), // Un poco menos alto para ahorrar espacio
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val barHeights = listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 0.3f, 0.8f)
                barHeights.forEach { height ->
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .fillMaxHeight(height)
                            .background(Color(0xFF3B82F6), RoundedCornerShape(3.dp))
                    )
                }
            }

            // --- Espaciador flexible ---
            // Ahora que la Column tiene fillMaxSize, esto empujará el contenido inferior
            // justo hasta el borde de abajo de la tarjeta.
            Spacer(modifier = Modifier.weight(1f))

            // --- Monitor de Riesgo (Fondo) ---
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("RIESGO DE IMPACTO", color = Color.Gray, fontSize = 10.sp)
                    Text("2% - BAJO", color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = { 0.02f },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = NeonGreen,
                    trackColor = Color(0xFF0F172A)
                )

                Text(
                    "Monitorización TFLite activa en local",
                    color = Color.Gray.copy(alpha = 0.6f),
                    fontSize = 8.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}