package com.example.safedriveai.ui.diagnostic

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
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
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.safedriveai.sensors.SensorChecker

// 1. MODELO DE DATOS
data class PermissionItemData(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val permissionsToRequest: List<String>
)

@Composable
fun GatekeeperScreen(onAllPermissionsGranted: @Composable () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // ESTADOS DE VALIDACIÓN
    var missingHardwareList by remember { mutableStateOf(SensorChecker.getMissingHardware(context)) }
    var isNetworkAvailable by remember { mutableStateOf(checkNetwork(context)) }
    var grantedPermissions by remember { mutableStateOf(setOf<String>()) }

    // 2. LISTA DINÁMICA DE PERMISOS (SEGÚN VERSIÓN DE ANDROID)
    val permissionItems = remember {
        val list = mutableListOf(
            PermissionItemData(
                title = "Ubicación",
                description = "Para el mapa y medir la velocidad de conducción.",
                icon = Icons.Default.LocationOn,
                permissionsToRequest = listOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            ),
            PermissionItemData(
                title = "Protocolo SOS",
                description = "Permite llamar al 112 y enviar SMS en caso de impacto.",
                icon = Icons.Default.Phone,
                permissionsToRequest = listOf(
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.SEND_SMS
                )
            ),
            PermissionItemData(
                title = "Micrófono",
                description = "Para detectar el sonido del impacto (Firma Acústica).",
                icon = Icons.Default.Mic,
                permissionsToRequest = listOf(Manifest.permission.RECORD_AUDIO)
            )
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            list.add(PermissionItemData(
                title = "Actividad Física",
                description = "Para saber cuándo entras y sales del vehículo.",
                icon = Icons.AutoMirrored.Filled.DirectionsRun,
                permissionsToRequest = listOf(Manifest.permission.ACTIVITY_RECOGNITION)
            ))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(PermissionItemData(
                title = "Notificaciones",
                description = "Para alertas críticas de seguridad y estado del sistema.",
                icon = Icons.Default.Notifications,
                permissionsToRequest = listOf(Manifest.permission.POST_NOTIFICATIONS)
            ))
        }
        list
    }

    // 3. LÓGICA DE ACTUALIZACIÓN
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

    // Sincronizar con el ciclo de vida (vuelve a chequear al regresar a la app)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                updatePermissionsState()
                isNetworkAvailable = checkNetwork(context)
                missingHardwareList = SensorChecker.getMissingHardware(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> updatePermissionsState() }

    val allPermissionsGranted = permissionItems.all { item ->
        item.permissionsToRequest.all { it in grantedPermissions }
    }

    // 4. UI DE DECISIÓN (ORDEN DE IMPORTANCIA)
    when {
        // A. ¿Falta Hardware? (Crítico)
        missingHardwareList.isNotEmpty() -> {
            ErrorScreen(
                title = "Hardware no compatible",
                message = "Tu dispositivo no cuenta con los sensores necesarios:",
                components = missingHardwareList.joinToString("\n ")
            )
        }
        // B. ¿No hay Internet? (Necesario para configurar)
        !isNetworkAvailable -> {
            ErrorScreen(
                title = "Sin Conexión",
                message = "SafeDriveAI necesita Internet para la configuración inicial y el mapa.",
                components = "Por favor, activa el Wi-Fi o los Datos Móviles."
            )
        }
        // C. ¿Todo OK?
        allPermissionsGranted -> {
            onAllPermissionsGranted()
        }
        // D. Faltan permisos (Mostrar lista)
        else -> {
            PermissionListUI(
                permissionItems = permissionItems,
                grantedPermissions = grantedPermissions,
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

// FUNCIONES AUXILIARES
fun checkNetwork(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val nw = cm.activeNetwork ?: return false
    val actNw = cm.getNetworkCapabilities(nw) ?: return false
    return actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}