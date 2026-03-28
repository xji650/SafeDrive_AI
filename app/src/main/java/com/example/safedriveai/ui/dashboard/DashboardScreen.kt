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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.safedriveai.sensors.SensorDataManager


val DarkBackground = Color(0xFF0F172A)
val CardBackground = Color(0xFF1E293B)
val NeonGreen = Color(0xFF10B981)
val EmergencyRed = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DashboardApp(isLandscape: Boolean) {
    val context = LocalContext.current
    val sensorData = remember { SensorDataManager(context) }
    val x by sensorData.accelX.collectAsState()
    val y by sensorData.accelY.collectAsState()

    DisposableEffect(Unit) {
        sensorData.startListening() // Arrancamos los sensores al entrar

        onDispose {
            sensorData.stopListening() // Los apagamos al salir para no gastar batería
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground) {
        if (isLandscape) {
            // --- MODO HORIZONTAL PROFESIONAL (3 COLUMNAS) ---
            DashboardLandscapeLayout(x, y)

        } else {
            // --- MODO VERTICAL (Optimizando espacio) ---
            DashboardPortraitLayout(x, y)
        }
    }
}

@Composable
fun DashboardLandscapeLayout(x: Float, y: Float) {
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
            GForceCard(x,y)
            AudioAuraCard()
        }
    }
}

@Composable
fun DashboardPortraitLayout(x: Float, y: Float) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        HeaderCard()
        MapSecurityCard(height = 250.dp) // Mapa protagonista
        SpeedometerCard()
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f)) { GForceCard(x,y) }
            Box(modifier = Modifier.weight(1f)) { AudioAuraCard() }
        }
        EmergencyButton()
    }
}