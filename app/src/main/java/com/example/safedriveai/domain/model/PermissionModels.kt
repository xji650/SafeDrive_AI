package com.example.safedriveai.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

data class PermissionItemData(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val permissionsToRequest: List<String>
)

data class PermissionState(
    val missingHardware: List<String> = emptyList(),
    val isNetworkAvailable: Boolean = true,
    val grantedPermissions: Set<String> = emptySet(),
    val allPermissionsGranted: Boolean = false
)