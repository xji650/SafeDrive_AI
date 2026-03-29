package com.example.safedriveai.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

class AccelerometerProvider(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val _accelX = MutableStateFlow(0f)
    val accelX = _accelX.asStateFlow()

    private val _accelY = MutableStateFlow(0f)
    val accelY = _accelY.asStateFlow()

    private val _totalG = MutableStateFlow(1f)
    val totalG = _totalG.asStateFlow()

    fun start() {
        val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_UI)
    }

    fun stop() = sensorManager.unregisterListener(this)

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            _accelX.value = x / 9.81f
            _accelY.value = y / 9.81f
            _totalG.value = sqrt(x*x + y*y + z*z) / 9.81f
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}