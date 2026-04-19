package com.example.safedriveai.ui.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.example.safedriveai.domain.model.PermissionItemData
import com.example.safedriveai.domain.model.PermissionState
import com.example.safedriveai.sensors.SensorChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PermissionViewModel(private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionState())
    val uiState = _uiState.asStateFlow()

    // 1. Definición centralizada de los requisitos
    val permissionItems: List<PermissionItemData> = run {
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
                description = "Para detectar el sonido del impacto.",
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
                description = "Para alertas críticas de seguridad.",
                icon = Icons.Default.Notifications,
                permissionsToRequest = listOf(Manifest.permission.POST_NOTIFICATIONS)
            ))
        }
        list
    }

    // 2. Lógica de chequeo de sistema
    fun checkSystemStatus() {
        val missingHardware = SensorChecker.getMissingHardware(context)
        val networkOk = checkNetwork(context)

        val granted = mutableSetOf<String>()
        permissionItems.forEach { item ->
            val isGranted = item.permissionsToRequest.all { perm ->
                ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
            }
            if (isGranted) granted.addAll(item.permissionsToRequest)
        }

        val allGranted = permissionItems.all { item ->
            item.permissionsToRequest.all { it in granted }
        }

        _uiState.update {
            it.copy(
                missingHardware = missingHardware,
                isNetworkAvailable = networkOk,
                grantedPermissions = granted,
                allPermissionsGranted = allGranted
            )
        }
    }

    private fun checkNetwork(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw = cm.activeNetwork ?: return false
        val actNw = cm.getNetworkCapabilities(nw) ?: return false
        return actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}