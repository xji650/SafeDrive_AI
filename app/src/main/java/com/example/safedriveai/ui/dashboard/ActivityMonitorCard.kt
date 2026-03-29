package com.example.safedriveai.ui.dashboard

import android.annotation.SuppressLint
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
import androidx.compose.material.icons.filled.Speed
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
fun ActivityMonitorCard(location: android.location.Location?, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(5.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Cabecera compacta
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("ACTIVITY MONITOR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text("Estado: IN_VEHICLE", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)



            // ELIMINAMOS EL DIVIDER PARA GANAR ESPACIO VERTICAL

            // Coordenadas abreviadas y fuente ligeramente ajustada
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("LAT", color = Color.Gray, fontSize = 9.sp)
                    Text(String.format(Locale.US, "%.3f", location?.latitude ?: 0.0), color = Color.White, fontSize = 12.sp)
                }
                Column {
                    Text("LON", color = Color.Gray, fontSize = 9.sp)
                    Text(String.format(Locale.US, "%.3f", location?.longitude ?: 0.0), color = Color.White, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("ERR", color = Color.Gray, fontSize = 9.sp) // Margen de error
                    Text("±${location?.accuracy?.toInt() ?: 0}m", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}