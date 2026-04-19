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
        blackBox.addPoint(g, speed, amplitude)

        // 2. Evaluar Trigger de Emergencia (CU-03)
        if (shouldTriggerEvent(g, currentTime)) {
            // Pasamos los datos al protocolo para que pueda guardarlos
            executeEmergencyProtocol(g, speed, lat, lon, currentTime)
            lastCrashTime = currentTime
        }
    }

    private fun shouldTriggerEvent(g: Float, currentTime: Long): Boolean {
        return g > CRITICAL_G_THRESHOLD && (currentTime - lastCrashTime > COOLDOWN_MS)
    }

    private fun executeEmergencyProtocol(g: Float, speed: Float, lat: Double, lon: Double, time: Long) {
        Log.e("SafeDriveAI", "¡Impacto Detectado! Guardando en JSON y Room...")

        // 1. Guardar JSON (Caja Negra técnica)
        blackBox.saveEventToDisk()

        // 2. Guardar en ROOM (Para que aparezca en tu lista de EDR)
        scope.launch {
            val incident = IncidentEntity(
                timestamp = time,
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