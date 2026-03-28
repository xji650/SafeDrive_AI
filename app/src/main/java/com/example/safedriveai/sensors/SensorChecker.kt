package com.example.safedriveai.sensors

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log

object SensorChecker {

    fun getMissingHardware(context: Context): List<String> {
        val missingHardware = mutableListOf<String>()
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) {
            missingHardware.add("Acelerómetro")
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) == null) {
            missingHardware.add("Giroscopio")
        }

        val packageManager = context.packageManager
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
            missingHardware.add("Micrófono")
        }
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {
            missingHardware.add("Antena GPS")
        }

        if (missingHardware.isNotEmpty()) {
            Log.e("SafeDriveAI", "Falta hardware de fábrica: $missingHardware")
        }

        return missingHardware
    }
}