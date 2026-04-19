package com.example.safedriveai.ui.permission

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.safedriveai.domain.model.PermissionItemData


@Composable
fun PermissionScreen(viewModel: PermissionViewModel, onAllPermissionsGranted: @Composable () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    // 1. Sincronización con el ciclo de vida
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkSystemStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 2. Lanzador de permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { viewModel.checkSystemStatus() }

    // 3. UI de Decisión basada en el Estado del ViewModel
    when {
        uiState.missingHardware.isNotEmpty() -> {
            ErrorScreen(
                title = "Hardware no compatible",
                message = "Tu dispositivo no cuenta con:",
                components = uiState.missingHardware.joinToString("\n ")
            )
        }
        !uiState.isNetworkAvailable -> {
            ErrorScreen(
                title = "Sin Conexión",
                message = "SafeDriveAI necesita Internet para el mapa.",
                components = "Activa Wi-Fi o Datos Móviles."
            )
        }
        uiState.allPermissionsGranted -> {
            onAllPermissionsGranted()
        }
        else -> {
            PermissionListUI(
                permissionItems = viewModel.permissionItems,
                grantedPermissions = uiState.grantedPermissions,
                onRequestClick = { perms -> permissionLauncher.launch(perms.toTypedArray()) },
                onOpenSettings = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            )
        }
    }
}

// --- SUB-COMPONENTES DE UI ---

@Composable
fun PermissionListUI(
    permissionItems: List<PermissionItemData>,
    grantedPermissions: Set<String>,
    onRequestClick: (List<String>) -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text("Configuración Inicial", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "SafeDriveAI necesita estos accesos para protegerte. Por favor, concédelos uno por uno.",
            style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        permissionItems.forEach { item ->
            val isGranted = item.permissionsToRequest.all { it in grantedPermissions }
            PermissionRow(item, isGranted) { onRequestClick(item.permissionsToRequest) }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.weight(1f))
        TextButton(onClick = onOpenSettings) {
            Text("¿Un botón no funciona? Abrir Ajustes")
        }
    }
}

@Composable
fun PermissionRow(item: PermissionItemData, isGranted: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isGranted) Color(0xFF10B981).copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            item.icon, contentDescription = null,
            tint = if (isGranted) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(item.description, style = MaterialTheme.typography.bodySmall)
        }
        if (isGranted) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
        } else {
            Button(onClick = onClick) { Text("Permitir") }
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
        Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(8.dp))
        Text(message, textAlign = TextAlign.Center)
        Text(components, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}