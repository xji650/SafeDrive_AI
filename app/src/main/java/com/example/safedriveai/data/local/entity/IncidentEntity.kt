package com.example.safedriveai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "incidents_table")
data class IncidentEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val timestamp: Long = 0L,
    val amplitudeMicrophone: Float = 0f,
    val maxGForce: Float = 0f,
    val speedAtImpact: Float = 0f,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isSynced: Boolean = false
)