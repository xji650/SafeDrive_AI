package com.example.safedriveai.ui.dashboard

import android.location.Location
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safedriveai.domain.model.DashboardModel
import com.example.safedriveai.data.repository.SensorRepository
import com.example.safedriveai.sensors.ActivityState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val sensorRepository: SensorRepository
) : ViewModel() {

    @RequiresApi(Build.VERSION_CODES.O)
    fun startDemoSimulation(context: android.content.Context) {
        viewModelScope.launch {
            try {
                val csvContent = context.assets.open("choque_simulado.csv")
                    .bufferedReader()
                    .readLines()
                sensorRepository.triggerDemoSimulation(csvContent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val uiState: StateFlow<DashboardModel> = combine(
        sensorRepository.accelX,        // Índice 0
        sensorRepository.accelY,        // Índice 1
        sensorRepository.speed,         // Índice 2
        sensorRepository.amplitude,     // Índice 3
        sensorRepository.jerk,          // Índice 4
        sensorRepository.angularVelocity, // Índice 5
        ActivityState.currentActivity,  // Índice 6
        sensorRepository.currentLocation // Índice 7
    ) { array ->
        // 'array' contiene los valores en el mismo orden de arriba
        DashboardModel(
            accelX = array[0] as Float,
            accelY = array[1] as Float,
            speed = array[2] as Float,
            amplitude = array[3] as Float,
            jerk = array[4] as Float,
            angularVelocity = array[5] as Float,
            currentActivity = array[6] as String,
            location = array[7] as Location?
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardModel()
    )
}