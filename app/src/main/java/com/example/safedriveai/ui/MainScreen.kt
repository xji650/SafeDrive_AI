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
import androidx.compose.material.icons.filled.Place
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.safedriveai.ui.dashboard.DashboardApp
import com.example.safedriveai.ui.diagnostic.DiagnosticApp
import com.example.safedriveai.utils.RotationAwareContent
import com.example.safedriveai.utils.rememberDeviceRotation

enum class AppDestinations {
    DASHBOARD, DIAGNOSTIC, MAPS, USER_PREFERENCE
}

data class NavigationItem(
    val destination: AppDestinations,
    val icon: ImageVector,
    val label: String
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeDriveAIApp(navController: NavController) {
    var selectedScreen by remember { mutableStateOf(AppDestinations.DASHBOARD) }
    val currentRotation by rememberDeviceRotation()
    val navItems = listOf(
        NavigationItem(AppDestinations.DASHBOARD, Icons.Default.Home, "Dashboard"),
        NavigationItem(AppDestinations.DIAGNOSTIC, Icons.Default.Build, "Diagnostic"),
        NavigationItem(AppDestinations.MAPS, Icons.Default.Place, "Maps"),
        NavigationItem(AppDestinations.USER_PREFERENCE, Icons.Default.Person, "Prefs")
    )

    Scaffold(
        bottomBar = {
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
    ) { innerPadding ->
        RotationAwareContent(
            rotation = currentRotation,
            modifier = Modifier.padding(innerPadding)
        ) {
            when (selectedScreen) {
                AppDestinations.DASHBOARD -> DashboardApp()
                AppDestinations.DIAGNOSTIC -> DiagnosticApp()
                AppDestinations.MAPS -> MapsScreen()
                AppDestinations.USER_PREFERENCE -> UserPreferenceScreen()
            }
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DashboardScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = "Dashboard", style = MaterialTheme.typography.titleLarge)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DiagnosticScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = "Diagnostic", style = MaterialTheme.typography.titleLarge)
        DiagnosticApp()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MapsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = "Maps", style = MaterialTheme.typography.titleLarge)
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