package com.example.safedriveai.domain.model

import android.location.Location

data class DashboardModel(
    val accelX: Float = 0f,
    val accelY: Float = 0f,
    val speed: Float = 0f,
    val amplitude: Float = 0f,
    val currentActivity: String = "UNKNOWN",
    val location: Location? = null,
    val isEmergencyActive: Boolean = false
)