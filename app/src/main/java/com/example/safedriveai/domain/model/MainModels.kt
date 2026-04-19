package com.example.safedriveai.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestinations {
    DASHBOARD, DIAGNOSTIC, EDR, USER_PREFERENCE
}

data class NavigationItem(
    val destination: AppDestinations,
    val icon: ImageVector,
    val label: String
)