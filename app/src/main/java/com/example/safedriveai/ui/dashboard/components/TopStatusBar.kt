package com.example.safedriveai.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.safedriveai.ui.dashboard.NeonGreen
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TopStatusBar() {
    // Reloj en tiempo real
    var currentTime by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        while (true) {
            currentTime = formatter.format(Date())
            delay(1000) // Actualiza cada segundo
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp, start = 8.dp, end = 8.dp), // Un poco de padding lateral
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Izquierda: Título y Versión
        Column {
            Text(
                text = "SAFEDRIVE AI",
                color = Color(0xFF3B82F6), // Azul tecnología
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
            Text(
                text = "v2.6 EDGE ENGINE",
                color = Color.Gray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Centro: Indicadores de estado (Luces de sistema)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatusDot("GPS", NeonGreen)
            StatusDot("AI", NeonGreen)
            StatusDot("DGT", Color(0xFFFBBF24)) // Amarillo si está buscando datos
        }

        // Derecha: Hora
        Text(
            text = currentTime,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp // Un poco más grande para mejor lectura
        )
    }
}

@Composable
fun StatusDot(label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Tu efecto de brillo original, ¡que estaba genial!
        Box(
            modifier = Modifier
                .size(10.dp) // Un pelín más grande
                .background(color.copy(alpha = 0.3f), CircleShape) // Aura
                .padding(2.dp)
                .background(color, CircleShape) // Núcleo
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            color = Color.LightGray,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}