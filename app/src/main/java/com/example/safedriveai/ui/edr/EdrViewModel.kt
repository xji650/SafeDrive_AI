package com.example.safedriveai.ui.edr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safedriveai.data.local.BlackBoxManager
import com.example.safedriveai.data.local.entity.IncidentEntity
import com.example.safedriveai.domain.model.EdrModel
import com.example.safedriveai.domain.repository.IncidentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject // <-- CORREGIDO: javax en lugar de jakarta
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

@HiltViewModel
class EdrViewModel @Inject constructor(
    private val repository: IncidentRepository, // <-- NUEVO: Inyectamos Room
    private val blackBoxManager: BlackBoxManager
) : ViewModel() {

    // Cada vez que se guarde un choque, esta lista se actualizará SOLA.
    val incidentsHistory: StateFlow<List<IncidentEntity>> = repository.getAllIncidents()
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
    fun openDetailsFromEntity(incident: IncidentEntity) {
        val files = blackBoxManager.getSavedEvents()
        // Buscamos un archivo que contenga el timestamp en su nombre
        val targetFile = files.find { it.name.contains(incident.timestamp.toString()) }
            ?: files.firstOrNull() // Si no lo encuentra, cogemos el primero por seguridad

        targetFile?.let { file ->
            _selectedFile.value = file
            viewModelScope.launch {
                _selectedEventData.value = blackBoxManager.loadEventFromDisk(file)
            }
        }
    }

    fun closeDetails() {
        _selectedFile.value = null
        _selectedEventData.value = null
    }
}