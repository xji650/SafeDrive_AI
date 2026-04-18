package com.example.safedriveai.data.model

data class EdrModel(
    val time: String,
    val gForce: Float,
    val speed: Float,
    val audioAmplitude: Float
)