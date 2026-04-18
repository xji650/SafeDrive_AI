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
import com.example.safedriveai.data.model.DashboardModel
import com.example.safedriveai.ui.dashboard.components.ActivityMonitorCard
import com.example.safedriveai.ui.dashboard.components.AudioAuraCard
import com.example.safedriveai.ui.dashboard.components.EmergencyButton
import com.example.safedriveai.ui.dashboard.components.GForceCard
import com.example.safedriveai.ui.dashboard.components.MapSecurityCard
import com.example.safedriveai.ui.dashboard.components.SpeedometerCard
import com.example.safedriveai.ui.dashboard.components.TopStatusBar

@Composable
fun DashboardPortraitLayout(
    uiState: DashboardModel
) {
    // SIN SCROLL. Usamos weights para empujar todo a su sitio.
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TopStatusBar()

        // El mapa actúa como un resorte: ocupa todo el espacio sobrante
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            MapSecurityCard(uiState.location, modifier = Modifier.fillMaxSize())
        }

        // Bloque inferior estático
        Row(modifier = Modifier.height(150.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f)) { SpeedometerCard(speed = uiState.speed) }
            Box(modifier = Modifier.weight(1f)) {
                ActivityMonitorCard(
                    location = uiState.location,
                    activityStatus = uiState.currentActivity
                )
            }
        }

        Row(modifier = Modifier.height(200.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f)) { GForceCard(uiState.accelX, uiState.accelY) }
            Box(modifier = Modifier.weight(1f)) { AudioAuraCard(uiState.amplitude) }
        }

        Box(modifier = Modifier.height(50.dp)) { EmergencyButton() }
    }
}