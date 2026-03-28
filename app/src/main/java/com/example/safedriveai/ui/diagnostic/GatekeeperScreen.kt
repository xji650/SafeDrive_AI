package com.example.safedriveai.ui.diagnostic

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.safedriveai.sensors.SensorChecker

data class PermissionItemData(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val permissionsToRequest: List<String>
)

@Composable
fun GatekeeperScreen(onAllPermissionsGranted: @Composable () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var missingHardwareList by remember { mutableStateOf(SensorChecker.getMissingHardware(context)) }

    val permissionItems = remember {
        val list = mutableListOf(
            PermissionItemData(
                title = "Ubicación",
                description = "Necesario para el mapa y medir la velocidad de conducción.",
                icon = Icons.Default.LocationOn,
                permissionsToRequest = listOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            ),
            PermissionItemData(
                title = "Micrófono",
                description = "Para interactuar con la IA usando comandos de voz.",
                icon = Icons.Default.Mic,
                permissionsToRequest = listOf(Manifest.permission.RECORD_AUDIO)
            )
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            list.add(
                PermissionItemData(
                    title = "Actividad Física",
                    description = "Para detectar automáticamente cuándo estás conduciendo.",
                    icon = Icons.AutoMirrored.Filled.DirectionsRun,
                    permissionsToRequest = listOf(Manifest.permission.ACTIVITY_RECOGNITION)
                )
            )
        }
        list
    }

    var grantedPermissions by remember { mutableStateOf(setOf<String>()) }

    fun updatePermissionsState() {
        val currentGranted = mutableSetOf<String>()
        permissionItems.forEach { item ->
            val isGranted = item.permissionsToRequest.all { perm ->
                ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
            }
            if (isGranted) currentGranted.addAll(item.permissionsToRequest)
        }
        grantedPermissions = currentGranted
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                updatePermissionsState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        updatePermissionsState()
    }

    val allGranted = permissionItems.all { item ->
        item.permissionsToRequest.all { it in grantedPermissions }
    }

    if (missingHardwareList.isNotEmpty()) {
        val missingText = missingHardwareList.joinToString(separator = "\n ")

        ErrorScreen(
            title = "Hardware no compatible",
            message = "Lamentablemente, esta app no funcionará en este dispositivo.\n\n" +
                    "Tu dispositivo no cuenta con los componentes físicos de fábrica necesarios para ejecutar SafeDriveAI:",
            components = "\n$missingText"
        )
    } else if (allGranted) {
        onAllPermissionsGranted()
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Configuración Inicial",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "SafeDriveAI necesita estos accesos para protegerte en tu viaje. Por favor, concédelos uno por uno.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            permissionItems.forEach { item ->
                val isItemGranted = item.permissionsToRequest.all { it in grantedPermissions }

                PermissionRow(
                    item = item,
                    isGranted = isItemGranted,
                    onRequestClick = {
                        permissionLauncher.launch(item.permissionsToRequest.toTypedArray())
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            ) {
                Text("¿Un botón no funciona? Abrir Ajustes de la App")
            }
        }
    }
}

@Composable
fun PermissionRow(
    item: PermissionItemData,
    isGranted: Boolean,
    onRequestClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isGranted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (isGranted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Concedido",
                tint = Color(0xFF4CAF50), // Verde
                modifier = Modifier.size(28.dp)
            )
        } else {
            Button(
                onClick = onRequestClick,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("Conceder")
            }
        }
    }
}

@Composable
fun ErrorScreen(title: String, message: String, components: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Warning, contentDescription = "Advertencia", modifier =
            Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = message, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Left)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = components, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)

    }
}