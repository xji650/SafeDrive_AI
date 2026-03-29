package com.example.safedriveai.sensors

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

class SensorDataManager(context: Context) : SensorEventListener, LocationListener {

    // 1. MOTORES DE HARDWARE
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // 2. FLUJOS DE DATOS (ACELERÓMETRO)
    private val _accelX = MutableStateFlow(0f)
    val accelX = _accelX.asStateFlow()

    private val _accelY = MutableStateFlow(0f)
    val accelY = _accelY.asStateFlow()

    private val _totalG = MutableStateFlow(1f)
    val totalG = _totalG.asStateFlow()

    // 3. FLUJOS DE DATOS (GPS)
    private val _speed = MutableStateFlow(0f) // Velocidad en km/h
    val speed = _speed.asStateFlow()

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation = _currentLocation.asStateFlow()

    // --- MÉTODOS DE CONTROL ---

    @SuppressLint("MissingPermission") // Seguro gracias a tu GatekeeperScreen
    fun startListening() {
        // Encendemos Acelerómetro
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        // Encendemos GPS (Actualiza cada 1 seg o cada 1 metro)
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L,
                    1f,
                    this
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
        locationManager.removeUpdates(this)
    }

    // --- CALLBACKS ACELERÓMETRO (Inercia) ---

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Gravedad estándar ~ 9.81 m/s²
            _accelX.value = x / 9.81f
            _accelY.value = y / 9.81f
            _totalG.value = sqrt(x*x + y*y + z*z) / 9.81f
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // --- CALLBACKS GPS (Velocidad y Posición) ---

    override fun onLocationChanged(location: Location) {
        _currentLocation.value = location

        // El GPS devuelve m/s. Convertimos a km/h para el Dashboard
        // Fórmula: m/s * 3.6
        val speedInKmH = location.speed * 3.6f
        _speed.value = if (speedInKmH < 1f) 0f else speedInKmH // Filtro de ruido para coche parado
    }

    // Métodos requeridos por la interfaz (pueden quedar vacíos)
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}