package com.example.safedriveai.data.local.mapper

import com.example.safedriveai.data.local.entity.IncidentEntity
import com.example.safedriveai.domain.model.EdrModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun IncidentEntity.toDomainModel(): EdrModel {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    return EdrModel(
        time = sdf.format(Date(this.timestamp)),
        rawTimestamp = this.timestamp,
        gForce = this.maxGForce,
        speed = this.speedAtImpact,
        audioAmplitude = this.amplitudeMicrophone,
        latitude = this.latitude,
        longitude = this.longitude,
        isSynced = this.isSynced
    )
}