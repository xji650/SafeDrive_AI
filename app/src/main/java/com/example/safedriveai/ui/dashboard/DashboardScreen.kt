package com.example.safedriveai.ui.dashboard

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.example.safedriveai.ui.service.LocationService

val DarkBackground = Color(0xFF0F172A)
val CardBackground = Color(0xFF1E293B)
val NeonGreen = Color(0xFF10B981)
val EmergencyRed = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel, isLandscape: Boolean) {

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    DisposableEffect(Unit) {
        // Ahora "context" ya existe y el compilador sabe cómo llamar a startService
        val serviceIntent = Intent(context, LocationService::class.java)

        context.startForegroundService(serviceIntent)

        onDispose {
            // context.stopService(serviceIntent)
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground) {
        if (isLandscape) {
            DashboardLandscapeLayout(uiState)
        } else {
            DashboardPortraitLayout(uiState)
        }
    }
}