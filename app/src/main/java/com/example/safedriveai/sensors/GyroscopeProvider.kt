package com.example.safedriveai.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

class GyroscopeProvider(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val _angularVelocity = MutableStateFlow(0f)
    val angularVelocity = _angularVelocity.asStateFlow()

    fun start() {
        val gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        // Reducimos a SENSOR_DELAY_UI para evitar sobrecarga del procesador
        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_UI)
    }

    fun stop() = sensorManager.unregisterListener(this)

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_GYROSCOPE) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Convertimos de rad/s a deg/s (57.2958)
            val magnitudeRad = sqrt(x*x + y*y + z*z)
            val magnitudeDeg = magnitudeRad * 57.2958f

            _angularVelocity.value = magnitudeDeg.coerceIn(0f, 500f)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}