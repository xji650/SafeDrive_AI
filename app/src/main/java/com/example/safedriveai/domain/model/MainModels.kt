package com.example.safedriveai.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestinations {
    DASHBOARD, DIAGNOSTIC, CHAT, EDR, USER_PREFERENCE, TRASH
}

data class NavigationItem(
    val destination: AppDestinations,
    val icon: ImageVector,
    val label: String
)