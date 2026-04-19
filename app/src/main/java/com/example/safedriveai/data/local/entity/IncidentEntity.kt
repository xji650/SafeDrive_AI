package com.example.safedriveai.data.local.entity

import android.media.MicrophoneInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incidents_table")
data class IncidentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long, // Cuándo ocurrió el accidente
    val amplitudeMicrophone: Float,
    val maxGForce: Float, // Fuerza del impacto
    val speedAtImpact: Float, // A qué velocidad iba
    val latitude: Double, // Dónde ocurrió
    val longitude: Double,
    val isSynced: Boolean = false // Nos dirá si ya lo subimos a Firebase o no
)