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
fun DashboardPortraitLayout(
    x: Float,
    y: Float,
    speed: Float,
    location: android.location.Location?
) {
    // SIN SCROLL. Usamos weights para empujar todo a su sitio.
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TopStatusBar()

        // El mapa actúa como un resorte: ocupa todo el espacio sobrante
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            MapSecurityCard(location, modifier = Modifier.fillMaxSize())
        }

        // Bloque inferior estático
        Box(modifier = Modifier.height(110.dp)) { SpeedometerCard() }

        Row(modifier = Modifier.height(200.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f)) { GForceCard(x,y) }
            Box(modifier = Modifier.weight(1f)) { AudioAuraCard() }
        }

        Box(modifier = Modifier.height(70.dp)) { EmergencyButton() }
    }
}