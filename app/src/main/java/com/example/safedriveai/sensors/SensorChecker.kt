package com.example.safedriveai.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log

object SensorChecker {
    fun hasRequiredSensors(context: Context): Boolean {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        val hasSensors = accelerometer != null && gyroscope != null

        if (!hasSensors) {
            Log.e("SafeDriveAI", "Falta hardware crítico: Acelerómetro o Giroscopio no detectados.")
        }
        return hasSensors
    }
}