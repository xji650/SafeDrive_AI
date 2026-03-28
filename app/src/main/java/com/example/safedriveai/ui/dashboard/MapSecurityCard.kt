package com.example.safedriveai.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MapSecurityCard(height: Dp = Dp.Unspecified) {
    Card(
        modifier = Modifier.fillMaxWidth().then(if (height != Dp.Unspecified) Modifier.height(height) else Modifier.fillMaxHeight()),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Aquí iría el MapView de Google o OSM
            Text("MAPA DGT 3.0", Modifier.align(Alignment.Center), color = Color.Gray)

            // OVERLAY: Estado de la Caja Negra (EDR)
            Row(
                modifier = Modifier.align(Alignment.TopStart).padding(12.dp).background(Color.Black.copy(0.6f), RoundedCornerShape(8.dp)).padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).background(Color.Red, CircleShape))
                Spacer(Modifier.width(8.dp))
                Text("EDR ACTIVE: 30s BUFFER", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}