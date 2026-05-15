package com.example.safedriveai.domain.model

data class EdrModel(
    val id: String,
    val time: String,
    val rawTimestamp: Long,
    val gForce: Float,
    val speed: Float,
    val audioAmplitude: Float,
    val jerk: Float,
    val angle: Float,
    val latitude: Double,
    val longitude: Double,
    val isSynced: Boolean,
    val type: Int? = null, // null: Sin validar, 0: Falso Positivo, 1: Susto, 2: Accidente
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)