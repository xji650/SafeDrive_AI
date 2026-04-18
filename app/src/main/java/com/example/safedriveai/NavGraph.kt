package com.example.safedriveai

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.safedriveai.ui.SafeDriveAIApp
import com.example.safedriveai.ui.permission.PermissionScreen
import com.example.safedriveai.ui.permission.PermissionViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current

    // Creamos el ViewModel con su fábrica para pasarle el Contexto
    val permissionViewModel: PermissionViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PermissionViewModel(context.applicationContext) as T
            }
        }
    )

    NavHost(navController = navController, startDestination = "permission_check") {
        composable("permission_check") {
            PermissionScreen(
                viewModel = permissionViewModel, // Le pasamos el ViewModel profesional
                onAllPermissionsGranted = {
                    // Una vez concedidos, cargamos la estructura principal de la app
                    SafeDriveAIApp(navController)
                }
            )
        }
    }
}