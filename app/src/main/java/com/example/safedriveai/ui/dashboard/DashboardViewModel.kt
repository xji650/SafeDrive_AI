package com.example.safedriveai.ui.dashboard

import android.location.Location
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safedriveai.domain.model.DashboardModel
import com.example.safedriveai.data.repository.SensorRepository
import com.example.safedriveai.sensors.ActivityState
import kotlinx.coroutines.flow.*

class DashboardViewModel(private val sensorRepository: SensorRepository) : ViewModel() {

    val uiState: StateFlow<DashboardModel> = combine(
        sensorRepository.accelX,        // Índice 0
        sensorRepository.accelY,        // Índice 1
        sensorRepository.speed,         // Índice 2
        sensorRepository.amplitude,     // Índice 3
        ActivityState.currentActivity,  // Índice 4
        sensorRepository.currentLocation // Índice 5
    ) { array ->
        // 'array' contiene los valores en el mismo orden de arriba
        DashboardModel(
            accelX = array[0] as Float,
            accelY = array[1] as Float,
            speed = array[2] as Float,
            amplitude = array[3] as Float,
            currentActivity = array[4] as String,
            location = array[5] as Location?
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardModel()
    )

    @RequiresApi(Build.VERSION_CODES.O)
    fun startSensors() = sensorRepository.startListening()

    fun stopSensors() = sensorRepository.stopListening()
}