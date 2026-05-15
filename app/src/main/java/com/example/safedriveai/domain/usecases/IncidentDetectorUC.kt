package com.example.safedriveai.domain.usecases

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.safedriveai.data.local.BlackBoxManager
import com.example.safedriveai.data.local.entity.IncidentEntity
import com.example.safedriveai.domain.model.EdrModel
import com.example.safedriveai.domain.repository.IncidentRepository
import com.example.safedriveai.ml.IncidentClassifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

class IncidentDetectorUC @Inject constructor (
    private val repository: IncidentRepository,
    private val blackBox: BlackBoxManager,
    private val classifier: IncidentClassifier
) {
    private var lastCrashTime = 0L
    private val COOLDOWN_MS = 10000L
    private val scope = CoroutineScope(Dispatchers.IO)

    @RequiresApi(Build.VERSION_CODES.O)
    fun processTelemetry(g: Float, speed: Float, amplitude: Float, j: Float, a: Float, lat: Double = 0.0, lon: Double = 0.0) {
        val currentTime = System.currentTimeMillis()
        
        // 1. Siempre guardar en el búfer circular de la caja negra
        blackBox.addPoint(g, speed, amplitude, j, a, lat, lon)

        // 2. Clasificación mediante IA (Fusión Sensorial)
        // El modelo devuelve: 0=Normal, 1=Agresivo, 2=Accidente
        val classification = classifier.classify(g, j, amplitude, a, speed)

        // 3. Decisión de disparo: SOLO ACCIDENTES (Clase 2)
        if (classification == 2 && (currentTime - lastCrashTime > COOLDOWN_MS)) {
            executeEmergencyProtocol(g, amplitude, speed, j, a, lat, lon, currentTime)
            lastCrashTime = currentTime
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun executeEmergencyProtocol(g: Float, amplitude: Float, speed: Float, j: Float, a: Float, lat: Double, lon: Double, time: Long) {
        Log.e("SafeDriveAI", "¡Impacto Detectado! Guardando datos...")

        // 1. Guardar en la Caja Negra (JSON) - Solo para accidentes reales
        blackBox.saveEventToDisk(time)

        // 2. Guardar en el Repositorio usando EdrModel
        scope.launch {
            val incident = EdrModel(
                id = java.util.UUID.randomUUID().toString(),
                time = LocalDateTime.now().toString(),
                rawTimestamp = time,
                gForce = g,
                speed = speed,
                audioAmplitude = amplitude,
                jerk = j,
                angle = a,
                latitude = lat,
                longitude = lon,
                isSynced = false,
                type = 2 // 2: Accidente
            )
            repository.saveIncident(incident)
        }
    }
}
