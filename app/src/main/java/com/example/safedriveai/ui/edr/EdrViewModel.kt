package com.example.safedriveai.ui.edr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safedriveai.data.local.BlackBoxManager
import com.example.safedriveai.domain.model.EdrModel
import com.example.safedriveai.domain.repository.IncidentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

@HiltViewModel
class EdrViewModel @Inject constructor(
    private val repository: IncidentRepository,
    private val blackBoxManager: BlackBoxManager
) : ViewModel() {

    init {
        // Mantenimiento RGPD: Limpiar registros de >30 días al abrir la sección
        viewModelScope.launch {
            repository.purgeDeletedData()
        }
    }

    // Cada vez que se guarde un choque, esta lista se actualizará SOLA.
    // Así quedaría tu ViewModel ahora:
    val incidentsHistory: StateFlow<List<EdrModel>> = repository.getAllIncidents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val deletedIncidents: StateFlow<List<EdrModel>> = repository.getDeletedIncidents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedFile = MutableStateFlow<File?>(null)
    val selectedFile: StateFlow<File?> = _selectedFile.asStateFlow()

    private val _selectedEventData = MutableStateFlow<List<EdrModel>?>(null)
    val selectedEventData: StateFlow<List<EdrModel>?> = _selectedEventData.asStateFlow()

    // Ahora para abrir el detalle, buscamos el archivo JSON que coincida con el timestamp
    fun openDetailsFromEntity(incident: EdrModel) {
        viewModelScope.launch {
            // 1. Buscamos el archivo en el móvil
            val files = blackBoxManager.getSavedEvents()
            var targetFile = files.find { it.name.contains(incident.rawTimestamp.toString()) }

            // 2. Si NO ESTÁ en el móvil, le pedimos al Repositorio que lo baje de la nube
            if (targetFile == null) {
                targetFile = repository.getTelemetryFile(incident.rawTimestamp)
            }

            // 3. Si por fin lo tenemos (de local o de nube), lo abrimos
            if (targetFile != null && targetFile.exists()) {
                _selectedFile.value = targetFile
                _selectedEventData.value = blackBoxManager.loadEventFromDisk(targetFile)
            } else {
                // Aquí podrías poner un aviso si el JSON se borró de Firebase
                println("Error: El archivo no existe ni en local ni en la nube.")
            }
        }
    }

    fun closeDetails() {
        _selectedFile.value = null
        _selectedEventData.value = null
    }

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // Esta es la función que llamará el botón de la pantalla
    fun syncHistoryWithCloud() {
        viewModelScope.launch {
            _isSyncing.value = true // Encendemos la ruedita de carga

            // 1. Primero subimos lo local (incluyendo marcas de borrado suave)
            repository.syncWithCloud()

            // 2. Luego descargamos lo nuevo de Firebase
            repository.fetchHistoryFromCloud()

            _isSyncing.value = false // Apagamos la ruedita de carga
        }
    }

    fun deleteIncident(incidentId: String) {
        viewModelScope.launch {
            repository.deleteIncident(incidentId)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.deleteAllIncidents()
        }
    }

    fun restoreIncident(incidentId: String) {
        viewModelScope.launch {
            repository.restoreIncident(incidentId)
        }
    }

    fun restoreAllIncidents() {
        viewModelScope.launch {
            repository.restoreAllIncidents()
        }
    }
}