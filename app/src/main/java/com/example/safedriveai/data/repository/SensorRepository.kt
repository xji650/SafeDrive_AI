package com.example.safedriveai.data.repository

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.safedriveai.domain.usecases.IncidentDetectorUC
import com.example.safedriveai.sensors.AccelerometerProvider
import com.example.safedriveai.sensors.AudioProvider
import com.example.safedriveai.sensors.GyroscopeProvider
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
    private val detector: IncidentDetectorUC
) {

    private val accelProvider = AccelerometerProvider(context)
    private val locationProvider = LocationProvider(context)
    private val audioProvider = AudioProvider()
    private val gyroProvider = GyroscopeProvider(context)

    val accelX = accelProvider.accelX
    val accelY = accelProvider.accelY
    val totalG = accelProvider.totalG
    val jerk = accelProvider.jerk
    val angularVelocity = gyroProvider.angularVelocity
    val speed = locationProvider.speed
    val amplitude = audioProvider.amplitude
    val currentLocation = locationProvider.currentLocation

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    @RequiresApi(Build.VERSION_CODES.O)
    fun startListening() {
        accelProvider.start()
        locationProvider.start()
        audioProvider.start()
        gyroProvider.start()

        scope.launch {
            totalG.collect { g ->
                val location = currentLocation.value
                detector.processTelemetry(
                    g = g,
                    speed = speed.value,
                    amplitude = amplitude.value,
                    j = jerk.value,
                    a = angularVelocity.value,
                    lat = location?.latitude ?: 0.0,
                    lon = location?.longitude ?: 0.0
                )
            }
        }
    }

    fun stopListening() {
        accelProvider.stop()
        locationProvider.stop()
        audioProvider.stop()
        gyroProvider.stop()
        scope.coroutineContext.cancelChildren()
    }
}