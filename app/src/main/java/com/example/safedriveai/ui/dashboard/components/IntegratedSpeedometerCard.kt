package com.example.safedriveai.ui.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.safedriveai.ui.dashboard.CardBackground
import com.example.safedriveai.ui.dashboard.NeonGreen
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun IntegratedSpeedometerCard(speed: Float, modifier: Modifier = Modifier) {
    var currentTime by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        while (true) {
            currentTime = formatter.format(Date())
            delay(1000)
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            // Reducimos el padding a 12.dp para que no se aplaste
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // --- 1. CABECERA ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text("SAFEDRIVE AI", color = Color(0xFF3B82F6), fontWeight = FontWeight.Black, fontSize = 14.sp)
                    Text("v2.6 EDGE ENGINE", color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusDot("GPS", NeonGreen)
                    StatusDot("AI", NeonGreen)
                    StatusDot("DGT", Color(0xFFFBBF24))
                }
            }

            // --- 2. VELOCIDAD LIMPIA (Sin arco) ---
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = speed.toInt().toString(),
                    fontSize = 72.sp, // Más grande ya que no hay círculo
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text("km/h", fontSize = 14.sp, color = Color.Gray)
            }

            // --- 3. RELOJ INFERIOR ---
            Text(
                text = currentTime,
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}