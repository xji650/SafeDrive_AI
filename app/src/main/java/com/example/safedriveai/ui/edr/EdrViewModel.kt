package com.example.safedriveai.ui.edr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safedriveai.data.local.BlackBoxManager
import com.example.safedriveai.domain.model.EdrModel // Asegúrate de tener este import
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

@HiltViewModel
class EdrViewModel @Inject constructor(
    private val blackBoxManager: BlackBoxManager
) : ViewModel() {

    // Estado 1: La lista de archivos encontrados
    private val _incidentFiles = MutableStateFlow<List<File>>(emptyList())
    val incidentFiles: StateFlow<List<File>> = _incidentFiles.asStateFlow()

    // Estado 2: El archivo que el usuario ha tocado
    private val _selectedFile = MutableStateFlow<File?>(null)
    val selectedFile: StateFlow<File?> = _selectedFile.asStateFlow()

    // Estado 3: Los datos (JSON) ya procesados del archivo tocado
    private val _selectedEventData = MutableStateFlow<List<EdrModel>?>(null)
    val selectedEventData: StateFlow<List<EdrModel>?> = _selectedEventData.asStateFlow()



    fun loadFile() {
        viewModelScope.launch {
            // Le pedimos al manager que busque los archivos
            _incidentFiles.value = blackBoxManager.getSavedEvents()
        }
    }

    fun openDetails(file: File) {
        _selectedFile.value = file
        viewModelScope.launch {
            // Le pedimos al manager que lea y parsee el JSON
            _selectedEventData.value = blackBoxManager.loadEventFromDisk(file)
        }
    }

    fun closeDetails() {
        _selectedFile.value = null
        _selectedEventData.value = null
    }
}