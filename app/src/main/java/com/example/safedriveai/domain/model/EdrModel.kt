package com.example.safedriveai.domain.model

data class EdrModel(
    val time: String,
    val gForce: Float,
    val speed: Float,
    val audioAmplitude: Float
)