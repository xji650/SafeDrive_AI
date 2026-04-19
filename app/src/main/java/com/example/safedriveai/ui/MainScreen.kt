package com.example.safedriveai.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.safedriveai.ui.dashboard.DashboardScreen
import com.example.safedriveai.utils.RotationAwareContent
import com.example.safedriveai.utils.rememberDeviceRotation
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.*
import androidx.compose.ui.input.pointer.pointerInput
import android.app.Activity
import androidx.compose.material.icons.filled.Report
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.safedriveai.data.local.BlackBoxManager
import com.example.safedriveai.ui.edr.EdrScreen
import com.example.safedriveai.ui.edr.EdrViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.safedriveai.domain.model.AppDestinations
import com.example.safedriveai.domain.model.NavigationItem
import com.example.safedriveai.ui.dashboard.DashboardViewModel
import com.example.safedriveai.data.repository.SensorRepository
import com.example.safedriveai.ui.diagnostic.DiagnosticScreen
import com.example.safedriveai.ui.diagnostic.DiagnosticViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeDriveAIApp(navController: NavController) {
    var selectedScreen by remember { mutableStateOf(AppDestinations.DASHBOARD) }
    val currentRotation by rememberDeviceRotation()

    // --- 1. ESTADO DE PANTALLA COMPLETA ---
    var isFullScreen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? Activity

    val edrViewModel: EdrViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                // Le pasamos el applicationContext para evitar fugas de memoria
                val blackBoxManager = BlackBoxManager(context.applicationContext)
                return EdrViewModel(blackBoxManager) as T
            }
        }
    )

    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val sensorRepository = SensorRepository.getInstance(context)
                return DashboardViewModel(sensorRepository) as T
            }
        }
    )

    val diagnosticViewModel: DiagnosticViewModel = viewModel()

    // --- 2. AUTOMATIZACIÓN INTELIGENTE ---
    LaunchedEffect(isFullScreen) {
        activity?.window?.let { window ->
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            if (isFullScreen) {
                // Oculta la barra de arriba y la de navegación de abajo de Android
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                // Las vuelve a mostrar si sales de pantalla completa
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    LaunchedEffect(currentRotation.isLandscape) {
        isFullScreen = currentRotation.isLandscape
    }

    val navItems = listOf(
        NavigationItem(AppDestinations.DASHBOARD, Icons.Default.Home, "Dashboard"),
        NavigationItem(AppDestinations.DIAGNOSTIC, Icons.Default.Build, "Diagnostic"),
        NavigationItem(AppDestinations.EDR, Icons.Default.Report, "EDR"),
        NavigationItem(AppDestinations.USER_PREFERENCE, Icons.Default.Person, "Prefs")
    )

    Scaffold(
        bottomBar = {
            // --- 3. ANIMACIÓN DE OCULTACIÓN ---
            AnimatedVisibility(
                visible = !isFullScreen,
                enter = slideInVertically(initialOffsetY = { it }), // Desliza hacia arriba
                exit = slideOutVertically(targetOffsetY = { it })   // Desliza hacia abajo
            ) {
                NavigationBar {
                    navItems.forEach { item ->
                        val isSelected = selectedScreen == item.destination

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedScreen = item.destination },
                            icon = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.graphicsLayer {
                                        rotationZ = currentRotation.angle
                                    }
                                ) {
                                    Icon(
                                        item.icon,
                                        contentDescription = item.label,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            label = null
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        RotationAwareContent(
            rotation = currentRotation,
            modifier = Modifier
                .padding(innerPadding)
                // --- 4. GESTO DE DOBLE TOQUE MANUAL ---
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            isFullScreen = !isFullScreen
                        }
                    )
                }
        ) {
            when (selectedScreen) {
                AppDestinations.DASHBOARD -> DashboardScreen(
                    viewModel = dashboardViewModel,
                    isLandscape = currentRotation.isLandscape
                )
                AppDestinations.DIAGNOSTIC -> DiagnosticScreen(viewModel = diagnosticViewModel)
                AppDestinations.EDR -> EdrScreen(viewModel = edrViewModel)
                AppDestinations.USER_PREFERENCE -> UserPreferenceScreen()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UserPreferenceScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = "User Preferences", style = MaterialTheme.typography.titleLarge)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun MainAppPreview() {
    SafeDriveAIApp(
        navController = rememberNavController(),
    )
}