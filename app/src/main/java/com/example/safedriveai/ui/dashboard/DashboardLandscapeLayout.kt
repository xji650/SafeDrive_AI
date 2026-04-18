package com.example.safedriveai.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.safedriveai.data.model.DashboardModel
import com.example.safedriveai.ui.dashboard.components.ActivityMonitorCard
import com.example.safedriveai.ui.dashboard.components.AudioAuraCard
import com.example.safedriveai.ui.dashboard.components.EmergencyButton
import com.example.safedriveai.ui.dashboard.components.GForceCard
import com.example.safedriveai.ui.dashboard.components.IntegratedSpeedometerCard
import com.example.safedriveai.ui.dashboard.components.MapSecurityCard

@Composable
fun DashboardLandscapeLayout(
    uiState: DashboardModel
) {

    Row(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- COLUMNA IZQUIERDA ---
        Column(modifier = Modifier.weight(0.3f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Esta tarjeta ya lleva el título y la hora
            IntegratedSpeedometerCard(speed = uiState.speed, modifier = Modifier.weight(1.1f))
            ActivityMonitorCard(
                location = uiState.location,
                activityStatus = uiState.currentActivity,
                modifier = Modifier.weight(0.9f)
            )
        }

        // --- COLUMNA CENTRAL ---
        Column(modifier = Modifier.weight(0.4f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MapSecurityCard(currentLocation = uiState.location, modifier = Modifier.weight(1f))
            Box(modifier = Modifier.height(50.dp)) { EmergencyButton() }
        }

        // --- COLUMNA DERECHA ---
        Column(modifier = Modifier.weight(0.3f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            GForceCard(accelX = uiState.accelX, accelY = uiState.accelY, modifier = Modifier.weight(1.2f))
            AudioAuraCard(
                uiState.amplitude,
                Modifier.weight(0.8f)
            ) // Asegúrate de quitar la altura fija en AudioAuraCard
        }
    }
}