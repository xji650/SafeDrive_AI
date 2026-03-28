package com.example.safedriveai.ui.diagnostic

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.safedriveai.sensors.SensorChecker


@Composable
fun GatekeeperScreen(onAllPermissionsGranted: @Composable () -> Unit) {
    val context = LocalContext.current

    // 1. Lista de permisos que necesitamos pedir
    val requiredPermissions = mutableListOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(Manifest.permission.ACTIVITY_RECOGNITION)
        }
    }.toTypedArray()

    // 2. Estados para saber si ya tenemos todos
    var hasSensors by remember { mutableStateOf(SensorChecker.hasRequiredSensors(context)) }
    var hasPermissions by remember {
        mutableStateOf(requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        })
    }

    // 3. El lanzador de la ventana de permisos de Android
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        // Verificamos si el usuario aceptó TODOS los permisos
        hasPermissions = permissionsMap.values.all { it == true }
    }

    // 4. Lógica de renderizado
    if (!hasSensors) {
        // PANTALLA DE ERROR: No hay hardware
        ErrorScreen(
            title = "Hardware no compatible",
            message = "SafeDriveAI requiere un Acelerómetro y Giroscopio para funcionar correctamente."
        )
    } else if (!hasPermissions) {
        // PANTALLA DE PERMISOS: Faltan permisos, obligamos a darlos
        ErrorScreen(
            title = "Permisos Requeridos",
            message = "Para protegerte en el camino, necesitamos acceso al GPS, Micrófono y Actividad Física.",
            buttonText = "Conceder Permisos",
            onClick = { permissionLauncher.launch(requiredPermissions) }
        )
    } else {
        // ¡TODO CORRECTO! Mostramos la aplicación real
        onAllPermissionsGranted()
    }
}

@Composable
fun ErrorScreen(title: String, message: String, buttonText: String? = null, onClick: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Warning, contentDescription = "Advertencia", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = message, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)

        if (buttonText != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onClick) {
                Text(buttonText)
            }
        }
    }
}