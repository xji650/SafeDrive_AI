package com.example.safedriveai.ui.dashboard

import android.content.res.Configuration
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp


val DarkBackground = Color(0xFF0F172A)
val CardBackground = Color(0xFF1E293B)
val NeonGreen = Color(0xFF10B981)
val EmergencyRed = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DashboardApp(isLandscape: Boolean) {
    Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground) {
        if (isLandscape) {
            // --- MODO HORIZONTAL PROFESIONAL (3 COLUMNAS) ---
            Row(modifier = Modifier.fillMaxSize().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Columna 1: Velocidad y SOS
                Column(modifier = Modifier.weight(0.25f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SpeedometerCard()
                    EmergencyButton()
                }
                // Columna 2: MAPA GRANDE (Protagonista DGT 3.0)
                Column(modifier = Modifier.weight(0.5f)) {
                    MapSecurityCard()
                }
                // Columna 3: IA y Sensores
                Column(modifier = Modifier.weight(0.25f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    GForceCard()
                    AudioAuraCard()
                }
            }
        } else {
            // --- MODO VERTICAL (Optimizando espacio) ---
            Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                HeaderCard()
                MapSecurityCard(height = 250.dp) // Mapa protagonista
                SpeedometerCard()
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f)) { GForceCard() }
                    Box(modifier = Modifier.weight(1f)) { AudioAuraCard() }
                }
                EmergencyButton()
            }
        }
    }
}