package com.example.safedriveai.ui.dashboard

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

val DarkBackground = Color(0xFF0F172A)
val CardBackground = Color(0xFF1E293B)
val NeonGreen = Color(0xFF10B981)
val EmergencyRed = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel, isLandscape: Boolean) {

    val uiState by viewModel.uiState.collectAsState()

    DisposableEffect(Unit) {
        viewModel.startSensors()
        onDispose { viewModel.stopSensors() }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground) {
        if (isLandscape) {
            DashboardLandscapeLayout(uiState)
        } else {
            DashboardPortraitLayout(uiState)
        }
    }
}