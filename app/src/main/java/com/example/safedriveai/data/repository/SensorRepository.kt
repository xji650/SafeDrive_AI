package com.example.safedriveai.data.repository

import android.annotation.SuppressLint
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

class SensorRepository private constructor(private val context: Context) {

    private val accelProvider = AccelerometerProvider(context)
    private val locationProvider = LocationProvider(context)
    private val audioProvider = AudioProvider()
    private val blackBox = BlackBoxManager(context)
    private val detector = IncidentDetectorUC(context, blackBox)

    val accelX = accelProvider.accelX
    val accelY = accelProvider.accelY
    val totalG = accelProvider.totalG
    val speed = locationProvider.speed
    val amplitude = audioProvider.amplitude
    val currentLocation = locationProvider.currentLocation

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: SensorRepository? = null

        fun getInstance(context: Context): SensorRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SensorRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
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