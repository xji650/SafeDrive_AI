package com.example.safedriveai.domain.usecases

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.safedriveai.data.local.BlackBoxManager
import com.example.safedriveai.data.local.entity.IncidentEntity
import com.example.safedriveai.domain.repository.IncidentRepository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

        // 1. Alimentar la Caja Negra siempre
        // CÁMBIALO POR ESTO:
        blackBox.addPoint(g, speed, amplitude, lat, lon)

        // 2. Evaluar Trigger de Emergencia (CU-03)
        if (shouldTriggerEvent(g, currentTime)) {
            // Pasamos los datos al protocolo para que pueda guardarlos
            executeEmergencyProtocol(g, amplitude, speed, lat, lon, currentTime)
            lastCrashTime = currentTime
        }
    }

    private fun shouldTriggerEvent(g: Float, currentTime: Long): Boolean {
        return g > CRITICAL_G_THRESHOLD && (currentTime - lastCrashTime > COOLDOWN_MS)
    }

    private fun executeEmergencyProtocol(g: Float, amplitude: Float, speed: Float, lat: Double, lon: Double, time: Long) {
        Log.e("SafeDriveAI", "¡Impacto Detectado! Guardando en JSON")

        // 1. Guardar JSON (¡AHORA LE PASAMOS EL MISMO 'time' EXACTO!)
        blackBox.saveEventToDisk(time)

        // 2. Guardar en ROOM (Usa el mismo 'time')
        scope.launch {
            val incident = IncidentEntity(
                timestamp = time,
                amplitudeMicrophone = amplitude,
                maxGForce = g,
                speedAtImpact = speed,
                latitude = lat,
                longitude = lon,
                isSynced = false
            )
            repository.saveIncident(incident)
        }
    }
}