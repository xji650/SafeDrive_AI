package com.example.safedriveai.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DashboardLandscapeLayout(x: Float, y: Float, location: android.location.Location?, speed: Float,) {
    // SIN TopStatusBar() aquí
    Row(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- COLUMNA IZQUIERDA ---
        Column(modifier = Modifier.weight(0.3f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Esta tarjeta ya lleva el título y la hora
            IntegratedSpeedometerCard(speed = speed, modifier = Modifier.weight(1.3f))
            ActivityMonitorCard(location = location, modifier = Modifier.weight(0.7f))
        }

        // --- COLUMNA CENTRAL ---
        Column(modifier = Modifier.weight(0.4f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MapSecurityCard(currentLocation = location, modifier = Modifier.weight(1f))
            Box(modifier = Modifier.height(50.dp)) { EmergencyButton() }
        }

        // --- COLUMNA DERECHA ---
        Column(modifier = Modifier.weight(0.3f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            GForceCard(accelX = x, accelY = y, modifier = Modifier.weight(1.2f))
            AudioAuraCard(modifier = Modifier.weight(0.8f)) // Asegúrate de quitar la altura fija en AudioAuraCard
        }
    }
}