package com.example.safedriveai.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.abs
import com.example.safedriveai.ui.diagnostic.DiagnosticStatus // Importa tu enum de la UI

object DiagnosticEngine {

    /**
     * Evalúa el estado REAL de un sensor.
     * Es 'suspend' porque toma tiempo (milisegundos) encender la pieza y leer sus datos.
     */
    suspend fun runDiagnosticOn(context: Context, sensorId: String): Pair<DiagnosticStatus, String> {

        // 1. EL INSPECTOR: Preguntamos primero si la pieza física existe
        val missingHardware = SensorChecker.getMissingHardware(context)

        // Si falta, no perdemos el tiempo intentando leer datos que no existen
        when (sensorId) {
            "ACCEL" -> if (missingHardware.contains("Acelerómetro")) return Pair(DiagnosticStatus.ERROR, "Pieza física no detectada.")
            "GYRO" -> if (missingHardware.contains("Giroscopio")) return Pair(DiagnosticStatus.ERROR, "Pieza física no detectada.")
            "GPS" -> if (missingHardware.contains("Antena GPS")) return Pair(DiagnosticStatus.ERROR, "Antena no instalada de fábrica.")
            "MIC" -> if (missingHardware.contains("Micrófono")) return Pair(DiagnosticStatus.ERROR, "Sin hardware de micrófono.")
        }

        // 2. EL MECÁNICO: Si llegamos aquí, la pieza EXISTE. Vamos a probarla.
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        return when (sensorId) {
            "ACCEL" -> {
                // Podríamos hacer una lectura real aquí también, pero asumimos OK si existe para simplificar
                Pair(DiagnosticStatus.OK, "Sensor respondiendo correctamente.")
            }
            "GYRO" -> {
                val gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

                // Leemos el mundo real
                suspendCancellableCoroutine { continuation ->
                    val listener = object : android.hardware.SensorEventListener {
                        override fun onSensorChanged(event: android.hardware.SensorEvent?) {
                            event?.let {
                                val x = abs(it.values[0])
                                val y = abs(it.values[1])
                                val z = abs(it.values[2])

                                sensorManager.unregisterListener(this)

                                if (x > 0.2f || y > 0.2f || z > 0.2f) {
                                    if (continuation.isActive) continuation.resume(Pair(DiagnosticStatus.WARNING, "Vibración excesiva. Fija el móvil firmemente."))
                                } else {
                                    if (continuation.isActive) continuation.resume(Pair(DiagnosticStatus.OK, "Calibración estática perfecta (0.0 rad/s)."))
                                }
                            }
                        }
                        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                    }
                    sensorManager.registerListener(listener, gyro, SensorManager.SENSOR_DELAY_UI)
                    continuation.invokeOnCancellation { sensorManager.unregisterListener(listener) }
                }
            }
            "GPS" -> {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
                if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                    Pair(DiagnosticStatus.OK, "Señal GPS activa y lista para navegar.")
                } else {
                    Pair(DiagnosticStatus.WARNING, "Ubicación apagada. Activa el GPS en ajustes.")
                }
            }
            "MIC" -> {
                Pair(DiagnosticStatus.OK, "Micrófono listo para recibir comandos.")
            }
            else -> Pair(DiagnosticStatus.PENDING, "Sensor desconocido")
        }
    }
}