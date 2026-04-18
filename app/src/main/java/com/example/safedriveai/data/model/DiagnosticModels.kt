package com.example.safedriveai.data.model

import androidx.compose.ui.graphics.vector.ImageVector

enum class DiagnosticStatus {
    PENDING, TESTING, OK, WARNING, ERROR
}
data class DiagnosticItem(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val description: String,
    val status: DiagnosticStatus = DiagnosticStatus.PENDING,
    val message: String = "Esperando..."
)
