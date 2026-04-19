package com.example.safedriveai.sensors

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.util.Log

class LocationProvider(private val context: Context) {

    // 1. Usamos la API de Google Play
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private val _speed = MutableStateFlow(0f)
    val speed = _speed.asStateFlow()

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation = _currentLocation.asStateFlow()

    // 2. Definimos la configuración del Request
    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, // Máxima precisión para seguridad vial
        1000L // Intervalo de 1 segundo
    ).apply {
        setMinUpdateIntervalMillis(500L) // No más rápido de medio segundo para ahorrar batería
        setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
    }.build()

    // Callback que recibe los datos
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                updateLocationData(location)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun start() {
        try {
            // Solicitamos actualizaciones de forma optimizada
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: Exception) {
            Log.e("LocationProvider", "Error al iniciar FusedLocation: ${e.message}")
        }
    }

    fun stop() {
        // Importante: detener para ahorrar batería
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun updateLocationData(location: Location) {
        _currentLocation.value = location
        val speedInKmH = location.speed * 3.6f
        _speed.value = if (speedInKmH < 1.5f) 0f else speedInKmH
    }
}