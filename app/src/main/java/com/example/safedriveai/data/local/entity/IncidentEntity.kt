package com.example.safedriveai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName
import java.util.UUID

@Entity(tableName = "incidents_table")
data class IncidentEntity(
    @PrimaryKey
    var id: String = UUID.randomUUID().toString(),

    var timestamp: Long = 0L,
    var amplitudeMicrophone: Float = 0f,
    var maxGForce: Float = 0f,
    var speedAtImpact: Float = 0f,
    var angleAtImpact: Float = 0f,
    var jerkAtImpact: Float = 0f,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,

    @get:PropertyName("isSynced")
    @set:PropertyName("isSynced")
    var isSynced: Boolean = false,

    var type: Int? = null,

    @get:PropertyName("isDeleted")
    @set:PropertyName("isDeleted")
    var isDeleted: Boolean = false,

    @get:PropertyName("deletedAt")
    @set:PropertyName("deletedAt")
    var deletedAt: Long? = null
)