package com.example.safedriveai.ui.dashboard

import android.location.Location
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun ActivityMonitorCard(location: Location?, activityStatus: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            // 1. Aumentamos el padding interno de 5.dp a 16.dp (igual que Speedometer)
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.Center, // 2. Centramos todo verticalmente
            horizontalAlignment = Alignment.CenterHorizontally // 3. Centramos horizontalmente
        ) {
            // Cabecera
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("ACTIVITY MONITOR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }

            Spacer(modifier = Modifier.height(5.dp)) // Más espacio de separación

            Text("Estado: $activityStatus", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(7.dp)) // Más espacio antes de las coordenadas

            // Coordenadas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly // Reparte el espacio de forma más equilibrada
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("LAT", color = Color.Gray, fontSize = 9.sp)
                    Text(String.format(Locale.US, "%.3f", location?.latitude ?: 0.0), color = Color.White, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("LON", color = Color.Gray, fontSize = 9.sp)
                    Text(String.format(Locale.US, "%.3f", location?.longitude ?: 0.0), color = Color.White, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ERR", color = Color.Gray, fontSize = 9.sp)
                    Text("±${location?.accuracy?.toInt() ?: 0}m", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}