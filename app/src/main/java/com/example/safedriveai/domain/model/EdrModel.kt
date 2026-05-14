package com.example.safedriveai.domain.model

data class EdrModel(
    val id: String, // <-- Añadimos el ID para poder borrar
    val time: String,
    val rawTimestamp: Long,
    val gForce: Float,
    val speed: Float,
    val audioAmplitude: Float,
    val latitude: Double,
    val longitude: Double,
    val isSynced: Boolean
)