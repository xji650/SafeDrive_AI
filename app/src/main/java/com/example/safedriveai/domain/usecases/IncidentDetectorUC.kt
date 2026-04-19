package com.example.safedriveai.domain.usecases

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.safedriveai.data.local.BlackBoxManager
import com.example.safedriveai.data.local.entity.IncidentEntity
import com.example.safedriveai.domain.model.EdrModel
import com.example.safedriveai.domain.repository.IncidentRepository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

class IncidentDetectorUC @Inject constructor (
    private val repository: IncidentRepository,
    private val blackBox: BlackBoxManager
) {
    private var lastCrashTime = 0L
    private val CRITICAL_G_THRESHOLD = 4.0f
    private val COOLDOWN_MS = 10000L
    private val scope = CoroutineScope(Dispatchers.IO)

    @RequiresApi(Build.VERSION_CODES.O)
    fun processTelemetry(g: Float, speed: Float, amplitude: Float, lat: Double = 0.0, lon: Double = 0.0) {
        val currentTime = System.currentTimeMillis()
        blackBox.addPoint(g, speed, amplitude, lat, lon)

        if (shouldTriggerEvent(g, currentTime)) {
            executeEmergencyProtocol(g, amplitude, speed, lat, lon, currentTime)
            lastCrashTime = currentTime
        }
    }

    private fun shouldTriggerEvent(g: Float, currentTime: Long): Boolean {
        return g > CRITICAL_G_THRESHOLD && (currentTime - lastCrashTime > COOLDOWN_MS)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun executeEmergencyProtocol(g: Float, amplitude: Float, speed: Float, lat: Double, lon: Double, time: Long) {
        Log.e("SafeDriveAI", "¡Impacto Detectado! Guardando datos...")

        // 1. Guardar en la Caja Negra (JSON)
        blackBox.saveEventToDisk(time)

        // 2. Guardar en el Repositorio usando EdrModel
        scope.launch {

            val incident = EdrModel(
                time = LocalDateTime.now().toString(), // Fecha para humanos
                rawTimestamp = time,        // La llave maestra para el JSON
                gForce = g,
                speed = speed,
                audioAmplitude = amplitude,
                latitude = lat,
                longitude = lon,
                isSynced = false
            )
            repository.saveIncident(incident)
        }
    }
}