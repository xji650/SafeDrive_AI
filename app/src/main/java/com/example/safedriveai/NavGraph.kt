package com.example.safedriveai

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.safedriveai.ui.SafeDriveAIApp
import androidx.navigation.compose.NavHost
import com.example.safedriveai.ui.diagnostic.GatekeeperScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph (navController: NavHostController) {
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            GatekeeperScreen(
                onAllPermissionsGranted = {
                    SafeDriveAIApp(navController)
                }
            )
        }
    }
}