package com.example.safedriveai.ui.dashboard

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    val speed by sensorData.speed.collectAsState()
    val location by sensorData.currentLocation.collectAsState()

    DisposableEffect(Unit) {
        sensorData.startListening() // Arrancamos los sensores al entrar
        onDispose { sensorData.stopListening()}
    }

    Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground) {
        if (isLandscape) {
            // --- MODO HORIZONTAL PROFESIONAL (3 COLUMNAS) ---
            DashboardLandscapeLayout(x, y, location, speed )

        } else {
            // --- MODO VERTICAL (Optimizando espacio) ---
            DashboardPortraitLayout(x, y, location, speed)
        }
    }
}