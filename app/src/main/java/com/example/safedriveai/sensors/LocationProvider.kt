package com.example.safedriveai.sensors

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.util.Log

class LocationProvider(private val context: Context) : LocationListener {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val _speed = MutableStateFlow(0f)
    val speed = _speed.asStateFlow()

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation = _currentLocation.asStateFlow()

    @SuppressLint("MissingPermission")
    fun start() {
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L, // 1 segundo
                    1f,    // 1 metro
                    this
                )
            }
        } catch (e: Exception) {
            Log.e("LocationProvider", "Error al iniciar GPS: ${e.message}")
        }
    }

    fun stop() {
        locationManager.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location) {
        _currentLocation.value = location

        // Conversión: m/s a km/h (m/s * 3.6)
        val speedInKmH = location.speed * 3.6f

        // Filtro de ruido: si la velocidad es despreciable, marcamos 0
        _speed.value = if (speedInKmH < 1.5f) 0f else speedInKmH
    }

    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}