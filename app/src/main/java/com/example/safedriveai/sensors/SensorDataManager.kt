package com.example.safedriveai.sensors

import android.content.Context
import com.example.safedriveai.ui.edr.BlackBoxManager
import kotlinx.coroutines.*

class SensorDataManager(private val context: Context) {

    private val accelProvider = AccelerometerProvider(context)
    private val locationProvider = LocationProvider(context)
    private val audioProvider = AudioProvider()
    private val blackBox = BlackBoxManager(context)
    private val detector = IncidentDetector(context, blackBox)

    val accelX = accelProvider.accelX
    val accelY = accelProvider.accelY
    val totalG = accelProvider.totalG
    val speed = locationProvider.speed
    val amplitude = audioProvider.amplitude
    val currentLocation = locationProvider.currentLocation

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun startListening() {
        accelProvider.start()
        locationProvider.start()
        audioProvider.start()

        scope.launch {
            accelProvider.totalG.collect { g ->
                detector.processTelemetry(g, speed.value, amplitude.value)
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