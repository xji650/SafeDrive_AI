package com.example.safedriveai.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log

object SensorChecker {

    /**
     * Verifica si el acelerómetro está disponible y funcional.
     */
    fun isAccelerometerAvailable(context: Context): Boolean {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        return if (accelerometer != null) {
            true
        } else {
            Log.e("SafeDriveAI", "Dispositivo sin Acelerómetro detectado.")
            false
        }
    }
}