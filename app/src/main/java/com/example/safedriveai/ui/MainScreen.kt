package com.example.safedriveai.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.safedriveai.ui.dashboard.DashboardApp

enum class AppDestinations{
    DASHBOARD,
    DIAGNOSTIC,
    MAPS,
    USER_PREFERENCE,
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeDriveAIApp(navController: NavController) {
    var selectedScreen by remember { mutableStateOf(AppDestinations.DASHBOARD) }

    Scaffold(

        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    selected = selectedScreen == AppDestinations.DASHBOARD,
                    onClick = { selectedScreen = AppDestinations.DASHBOARD },
                    label = { Text(text = "Dashboard") }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Build, contentDescription = "Diagnostic") },
                    selected = selectedScreen == AppDestinations.DIAGNOSTIC,
                    onClick = { selectedScreen = AppDestinations.DIAGNOSTIC },
                    label = { Text(text = "Diagnostic") }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Place, contentDescription = "Maps") },
                    selected = selectedScreen == AppDestinations.MAPS,
                    onClick = { selectedScreen = AppDestinations.MAPS },
                    label = { Text(text = "Maps") }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "User Preference") },
                    selected = selectedScreen == AppDestinations.USER_PREFERENCE,
                    onClick = { selectedScreen = AppDestinations.USER_PREFERENCE },
                    label = { Text(text = "Preferences") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedScreen) {
                AppDestinations.DASHBOARD -> DashboardScreen(navController)
                AppDestinations.DIAGNOSTIC -> DiagnosticScreen()
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
        DashboardApp(navController = navController)
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