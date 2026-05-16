package com.example.safedriveai.data.repository

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.safedriveai.domain.usecases.IncidentDetectorUC
import com.example.safedriveai.sensors.AccelerometerProvider
import com.example.safedriveai.sensors.AudioProvider
import com.example.safedriveai.sensors.GyroscopeProvider
import com.example.safedriveai.sensors.LocationProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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

    // Flows internos para telemetría
    private val _accelX = MutableStateFlow(0f)
    private val _accelY = MutableStateFlow(0f)
    private val _totalG = MutableStateFlow(1f)
    private val _jerk = MutableStateFlow(0f)
    private val _speed = MutableStateFlow(0f)
    private val _amplitude = MutableStateFlow(0f)
    private val _angularVelocity = MutableStateFlow(0f)
    
    // Estado para que la UI sepa que estamos en demo
    private val _isSimulating = MutableStateFlow(false)
    val isSimulating = _isSimulating.asStateFlow()

    // Exposición de flows
    val accelX = _accelX.asStateFlow()
    val accelY = _accelY.asStateFlow()
    val totalG = _totalG.asStateFlow()
    val jerk = _jerk.asStateFlow()
    val speed = _speed.asStateFlow()
    val amplitude = _amplitude.asStateFlow()
    val angularVelocity = _angularVelocity.asStateFlow()
    val currentLocation = locationProvider.currentLocation

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var simulationJob: Job? = null

    @RequiresApi(Build.VERSION_CODES.O)
    fun startListening() {
        if (_isSimulating.value) return
        
        accelProvider.start()
        locationProvider.start()
        audioProvider.start()
        gyroProvider.start()

        scope.launch {
            // Unimos todos los proveedores reales a nuestros flows internos
            launch { accelProvider.accelX.collect { if (!_isSimulating.value) _accelX.value = it } }
            launch { accelProvider.accelY.collect { if (!_isSimulating.value) _accelY.value = it } }
            launch { accelProvider.totalG.collect { if (!_isSimulating.value) _totalG.value = it } }
            launch { accelProvider.jerk.collect { if (!_isSimulating.value) _jerk.value = it } }
            launch { locationProvider.speed.collect { if (!_isSimulating.value) _speed.value = it } }
            launch { audioProvider.amplitude.collect { if (!_isSimulating.value) _amplitude.value = it } }
            launch { gyroProvider.angularVelocity.collect { if (!_isSimulating.value) _angularVelocity.value = it } }

            _totalG.collect { g ->
                detector.processTelemetry(
                    g = g, speed = _speed.value, amplitude = _amplitude.value,
                    j = _jerk.value, a = _angularVelocity.value,
                    lat = currentLocation.value?.latitude ?: 0.0,
                    lon = currentLocation.value?.longitude ?: 0.0
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun triggerDemoSimulation(csvData: List<String>) {
        if (_isSimulating.value) {
            stopDemoSimulation() 
            return
        }

        _isSimulating.value = true
        accelProvider.stop() 
        
        simulationJob = scope.launch(Dispatchers.IO) {
            try {
                csvData.drop(1).forEach { linea ->
                    val parts = linea.split(",")
                    if (parts.size >= 5) {
                        _totalG.value = parts[0].toFloat()
                        _jerk.value = parts[1].toFloat()
                        _amplitude.value = parts[2].toFloat()
                        _angularVelocity.value = parts[3].toFloat()
                        _speed.value = parts[4].toFloat()
                        // En demo ponemos los ejes a 0 o valores pequeños
                        _accelX.value = 0.1f
                        _accelY.value = 0.1f
                        delay(200) 
                    }
                }
            } finally {
                stopDemoSimulation()
            }
        }
    }

    fun stopDemoSimulation() {
        simulationJob?.cancel()
        _isSimulating.value = false
        accelProvider.start()
        locationProvider.start()
        audioProvider.start()
        gyroProvider.start()
    }

    fun stopListening() {
        stopDemoSimulation()
        accelProvider.stop()
        locationProvider.stop()
        audioProvider.stop()
        gyroProvider.stop()
        scope.coroutineContext.cancelChildren()
    }
}
