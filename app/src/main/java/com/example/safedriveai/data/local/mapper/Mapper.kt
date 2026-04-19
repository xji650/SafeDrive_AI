package com.example.safedriveai.data.local.mapper

import com.example.safedriveai.data.local.entity.IncidentEntity
import com.example.safedriveai.domain.model.EdrModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun IncidentEntity.toDomainModel(): EdrModel {

    // 1. Traducimos el tiempo: De milisegundos (Long) a texto legible (String)
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    val formattedTime = sdf.format(Date(this.timestamp))

    // 2. Construimos el modelo final conectando los cables
    // 2. Construimos el modelo final conectando los cables
    return EdrModel(
        time = formattedTime,
        rawTimestamp = this.timestamp, // <--- ¡AÑADE ESTA LÍNEA AQUÍ!
        gForce = this.maxGForce,
        speed = this.speedAtImpact,
        audioAmplitude = this.amplitudeMicrophone,
        latitude = this.latitude,
        longitude = this.longitude,
        isSynced = this.isSynced
    )
}