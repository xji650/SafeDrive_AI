package com.example.safedriveai.ui.diagnostic

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safedriveai.domain.model.DiagnosticItem
import com.example.safedriveai.domain.model.DiagnosticStatus
import com.example.safedriveai.domain.usecases.DiagnosticExecutorUC
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DiagnosticViewModel : ViewModel() {

    // 1. Mantenemos el estado de la lista
    private val _diagnosticItems = MutableStateFlow(
        listOf(
            DiagnosticItem("ACCEL", "Acelerómetro", Icons.Default.Speed, "Mide la aceleración y frenado."),
            DiagnosticItem("GYRO", "Giroscopio", Icons.Default.ScreenRotation, "Detecta las curvas y giros del coche."),
            DiagnosticItem("GPS", "Antena GPS", Icons.Default.LocationOn, "Precisión de la ubicación en tiempo real."),
            DiagnosticItem("MIC", "Micrófono", Icons.Default.Mic, "Firma acústica de colisión."),
            DiagnosticItem("SOS", "Protocolo SOS", Icons.Default.Phone, "Permisos para llamadas y SMS."),
            DiagnosticItem("NET", "Conectividad", Icons.Default.Wifi, "Conexión para alertas DGT 3.0.")
        )
    )
    val diagnosticItems: StateFlow<List<DiagnosticItem>> = _diagnosticItems.asStateFlow()

    // 2. Mantenemos el estado de carga
    private val _isRunningTests = MutableStateFlow(false)
    val isRunningTests: StateFlow<Boolean> = _isRunningTests.asStateFlow()

    // 3. Toda la lógica del bucle se hace en el ViewModel
    fun runDiagnostics(context: Context) {
        if (_isRunningTests.value) return

        viewModelScope.launch {
            _isRunningTests.value = true

            // Reiniciamos todos a "Pending"
            val initialList = _diagnosticItems.value.map {
                it.copy(status = DiagnosticStatus.PENDING, message = "Esperando...")
            }
            _diagnosticItems.value = initialList

            val currentList = initialList.toMutableList()

            for (i in currentList.indices) {
                // Marcamos como "Testing"
                currentList[i] = currentList[i].copy(status = DiagnosticStatus.TESTING, message = "Analizando...")
                _diagnosticItems.value = currentList.toList()

                // Delay UX
                delay(600)

                // Llamada al Caso de Uso (Domain Layer)
                val (resultStatus, resultMessage) = DiagnosticExecutorUC.runDiagnosticOn(context, currentList[i].id)

                // Actualizamos con el resultado
                currentList[i] = currentList[i].copy(status = resultStatus, message = resultMessage)
                _diagnosticItems.value = currentList.toList()
            }
            _isRunningTests.value = false
        }
    }
}