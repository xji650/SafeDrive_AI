package com.example.safedriveai.domain.usecases

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.content.ContextCompat
import com.example.safedriveai.domain.model.DiagnosticStatus
import com.example.safedriveai.sensors.SensorChecker
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.abs

object DiagnosticExecutorUC {

    suspend fun runDiagnosticOn(context: Context, sensorId: String): Pair<DiagnosticStatus, String> {
        // 1. Verificación de Hardware (Físico)
        val missingHardware = SensorChecker.getMissingHardware(context)
        when (sensorId) {
            "ACCEL" -> if (missingHardware.contains("Acelerómetro")) return Pair(DiagnosticStatus.ERROR, "Pieza física no detectada.")
            "GYRO" -> if (missingHardware.contains("Giroscopio")) return Pair(DiagnosticStatus.ERROR, "Pieza física no detectada.")
            "GPS" -> if (missingHardware.contains("Antena GPS")) return Pair(DiagnosticStatus.ERROR, "Antena no instalada de fábrica.")
            "MIC" -> if (missingHardware.contains("Micrófono")) return Pair(DiagnosticStatus.ERROR, "Sin hardware de micrófono.")
        }

        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        return when (sensorId) {
            "ACCEL" -> {
                // Chequeo rápido de existencia
                Pair(DiagnosticStatus.OK, "Acelerómetro respondiendo.")
            }

            "GYRO" -> {
                // Chequeo ACTIVO: Mira si los valores se vuelven locos
                val gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
                suspendCancellableCoroutine { continuation ->
                    val listener = object : SensorEventListener {
                        override fun onSensorChanged(event: SensorEvent?) {
                            event?.let {
                                val x = abs(it.values[0]);
                                val y = abs(it.values[1]);
                                val z = abs(it.values[2])
                                sensorManager.unregisterListener(this)
                                if (x > 0.2f || y > 0.2f || z > 0.2f) {
                                    if (continuation.isActive) continuation.resume(
                                        Pair(
                                            DiagnosticStatus.WARNING,
                                            "Vibración detectada. Fija el móvil."
                                        )
                                    )
                                } else {
                                    if (continuation.isActive) continuation.resume(
                                        Pair(
                                            DiagnosticStatus.OK,
                                            "Calibración estática perfecta."
                                        )
                                    )
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
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

                if (!hasPermission) Pair(DiagnosticStatus.ERROR, "Falta permiso de ubicación.")
                else if (!isEnabled) Pair(DiagnosticStatus.WARNING, "GPS apagado en el sistema.")
                else Pair(DiagnosticStatus.OK, "Señal GPS activa.")
            }

            "MIC" -> {
                val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                if (hasPerm) Pair(DiagnosticStatus.OK, "Micrófono listo.")
                else Pair(DiagnosticStatus.ERROR, "Permiso de audio denegado.")
            }

            "SOS" -> {
                val hasCall = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
                val hasSms = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED

                if (hasCall && hasSms) Pair(DiagnosticStatus.OK, "Protocolo SOS verificado.")
                else Pair(DiagnosticStatus.ERROR, "Faltan permisos de emergencia.")
            }

            "NET" -> {
                if (isInternetAvailable(context)) {
                    Pair(DiagnosticStatus.OK, "Conexión a internet estable.")
                } else {
                    Pair(DiagnosticStatus.WARNING, "Modo offline (Sin conexión).")
                }
            }

            else -> Pair(DiagnosticStatus.PENDING, "Sensor desconocido")
        }
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw = cm.activeNetwork ?: return false
        val actNw = cm.getNetworkCapabilities(nw) ?: return false
        return actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}