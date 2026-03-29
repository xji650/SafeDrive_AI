package com.example.safedriveai.sensors

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.safedriveai.ui.edr.BlackBoxManager

class IncidentDetector(
    private val context: Context,
    private val blackBox: BlackBoxManager
) {
    private var lastCrashTime = 0L

    private val CRITICAL_G_THRESHOLD = 1.8f
    private val COOLDOWN_MS = 10000L

    @RequiresApi(Build.VERSION_CODES.O)
    fun processTelemetry(g: Float, speed: Float, amplitude: Float) {
        val currentTime = System.currentTimeMillis()

        // 1. Alimentar la Caja Negra siempre
        blackBox.addPoint(g, speed, amplitude)

        // 2. Evaluar Trigger de Emergencia (CU-03)
        if (shouldTriggerEvent(g, currentTime)) {
            executeEmergencyProtocol()
            lastCrashTime = currentTime
        }
    }

    private fun shouldTriggerEvent(g: Float, currentTime: Long): Boolean {
        return g > CRITICAL_G_THRESHOLD && (currentTime - lastCrashTime > COOLDOWN_MS)
    }

    private fun executeEmergencyProtocol() {
        Log.e("SafeDriveAI", "¡Impacto Detectado por IncidentDetector!")
        blackBox.saveEventToDisk()
        // Aquí podría disparar un Callback hacia el ViewModel para mostrar la alerta SOS
    }
}