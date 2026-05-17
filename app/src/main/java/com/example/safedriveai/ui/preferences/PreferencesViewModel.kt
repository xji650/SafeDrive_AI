package com.example.safedriveai.ui.preferences

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safedriveai.domain.repository.AiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.content.edit

data class UserPreferencesState(
    val useMetricSystem: Boolean = true,
    val enableNotifications: Boolean = true,
    val darkMode: DarkModeConfig = DarkModeConfig.FOLLOW_SYSTEM,
    val syncOnlyWifi: Boolean = true,
    val voiceAlerts: Boolean = true,
    val ollamaHost: String = "192.168.1.130",
    val ragHost: String = "192.168.1.130",
    val ollamaStatus: ConnectionStatus = ConnectionStatus.UNKNOWN,
    val ragStatus: ConnectionStatus = ConnectionStatus.UNKNOWN
)

enum class ConnectionStatus {
    UNKNOWN, CHECKING, ONLINE, OFFLINE
}

enum class DarkModeConfig {
    FOLLOW_SYSTEM, LIGHT, DARK
}

@HiltViewModel
class PreferencesViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val aiRepository: AiRepository
) : ViewModel() {

    private val prefs = context.getSharedPreferences("safedrive_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(UserPreferencesState(
        ollamaHost = prefs.getString("ollama_host", "192.168.1.130") ?: "192.168.1.130",
        ragHost = prefs.getString("rag_host", "192.168.1.130") ?: "192.168.1.130",
        useMetricSystem = prefs.getBoolean("metric_system", true),
        enableNotifications = prefs.getBoolean("notifications", true),
        syncOnlyWifi = prefs.getBoolean("sync_wifi", true),
        voiceAlerts = prefs.getBoolean("voice_alerts", true)
    ))
    val uiState: StateFlow<UserPreferencesState> = _uiState.asStateFlow()

    fun toggleMetricSystem(enabled: Boolean) {
        _uiState.update { it.copy(useMetricSystem = enabled) }
        prefs.edit { putBoolean("metric_system", enabled) }
    }

    fun toggleNotifications(enabled: Boolean) {
        _uiState.update { it.copy(enableNotifications = enabled) }
        prefs.edit { putBoolean("notifications", enabled) }
    }

    fun setDarkMode(mode: DarkModeConfig) {
        _uiState.update { it.copy(darkMode = mode) }
    }

    fun toggleSyncOnlyWifi(enabled: Boolean) {
        _uiState.update { it.copy(syncOnlyWifi = enabled) }
        prefs.edit { putBoolean("sync_wifi", enabled) }
    }

    fun toggleVoiceAlerts(enabled: Boolean) {
        _uiState.update { it.copy(voiceAlerts = enabled) }
        prefs.edit { putBoolean("voice_alerts", enabled) }
    }

    fun setOllamaHost(host: String) {
        _uiState.update { it.copy(ollamaHost = host, ollamaStatus = ConnectionStatus.UNKNOWN) }
        prefs.edit { putString("ollama_host", host) }
    }

    fun setRagHost(host: String) {
        _uiState.update { it.copy(ragHost = host, ragStatus = ConnectionStatus.UNKNOWN) }
        prefs.edit { putString("rag_host", host) }
    }

    fun testOllamaConnection() {
        _uiState.update { it.copy(ollamaStatus = ConnectionStatus.CHECKING) }
        viewModelScope.launch {
            val status = aiRepository.testOllamaConnection(_uiState.value.ollamaHost)
            _uiState.update { it.copy(ollamaStatus = status) }
        }
    }

    fun testRagConnection() {
        _uiState.update { it.copy(ragStatus = ConnectionStatus.CHECKING) }
        viewModelScope.launch {
            val status = aiRepository.testRagConnection(_uiState.value.ragHost)
            _uiState.update { it.copy(ragStatus = status) }
        }
    }
}
