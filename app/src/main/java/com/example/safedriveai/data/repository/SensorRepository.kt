package com.example.safedriveai.data.repository

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.safedriveai.data.local.BlackBoxManager
import com.example.safedriveai.domain.usecases.IncidentDetectorUC
import com.example.safedriveai.sensors.AccelerometerProvider
import com.example.safedriveai.sensors.AudioProvider
import com.example.safedriveai.sensors.LocationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val blackBox: BlackBoxManager,
    private val detector: IncidentDetectorUC
) {

    private val accelProvider = AccelerometerProvider(context)
    private val locationProvider = LocationProvider(context)
    private val audioProvider = AudioProvider()

    val accelX = accelProvider.accelX
    val accelY = accelProvider.accelY
    val totalG = accelProvider.totalG
    val speed = locationProvider.speed
    val amplitude = audioProvider.amplitude
    val currentLocation = locationProvider.currentLocation

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    @RequiresApi(Build.VERSION_CODES.O)
    fun startListening() {
        accelProvider.start()
        locationProvider.start()
        audioProvider.start()

        scope.launch {
            accelProvider.totalG.collect { g ->
                val location = currentLocation.value // Obtenemos la última posición conocida
                detector.processTelemetry(
                    g,
                    speed.value,
                    amplitude.value,
                    location?.latitude ?: 0.0,
                    location?.longitude ?: 0.0
                )
            }
        }
    }

    fun stopListening() {
        accelProvider.stop()
        locationProvider.stop()
        audioProvider.stop()
        scope.coroutineContext.cancelChildren()
    }
}