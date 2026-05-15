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

    private val _jerk = MutableStateFlow(0f)
    val jerk = _jerk.asStateFlow()

    private var lastG = 1f
    private var lastTimestamp = 0L

    fun start() {
        val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        // SENSOR_DELAY_UI (~16-20Hz) es suficiente para telemetría y ahorra mucha CPU
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_UI)
    }

    fun stop() = sensorManager.unregisterListener(this)

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val currentG = sqrt(x*x + y*y + z*z) / 9.81f
            val coercedG = currentG.coerceIn(0f, 15f)
            _totalG.value = coercedG

            val currentTime = System.currentTimeMillis()
            if (lastTimestamp > 0) {
                val dt = (currentTime - lastTimestamp) / 1000f
                if (dt > 0) {
                    val jerkVal = Math.abs(currentG - lastG) / dt
                    _jerk.value = jerkVal.coerceIn(0f, 20f)
                }
            }
            lastG = currentG
            lastTimestamp = currentTime

            _accelX.value = x / 9.81f
            _accelY.value = y / 9.81f
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}