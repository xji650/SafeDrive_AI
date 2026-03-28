package com.example.safedriveai.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

class SensorDataManager(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // Flujos de datos para la UI
    private val _accelX = MutableStateFlow(0f)
    val accelX = _accelX.asStateFlow()

    private val _accelY = MutableStateFlow(0f)
    val accelY = _accelY.asStateFlow()

    private val _totalG = MutableStateFlow(1f)
    val totalG = _totalG.asStateFlow()

    fun startListening() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Convertimos m/s² a G (1G ≈ 9.81 m/s²)
            _accelX.value = x / 9.81f
            _accelY.value = y / 9.81f

            // Fuerza G total resultante (Hipotenusa 3D)
            _totalG.value = sqrt(x*x + y*y + z*z) / 9.81f
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}