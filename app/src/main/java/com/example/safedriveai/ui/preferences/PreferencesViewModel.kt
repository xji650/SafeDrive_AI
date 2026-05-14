package com.example.safedriveai.ui.preferences

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class UserPreferencesState(
    val useMetricSystem: Boolean = true,
    val enableNotifications: Boolean = true,
    val darkMode: DarkModeConfig = DarkModeConfig.FOLLOW_SYSTEM,
    val syncOnlyWifi: Boolean = true,
    val voiceAlerts: Boolean = true
)

enum class DarkModeConfig {
    FOLLOW_SYSTEM, LIGHT, DARK
}

@HiltViewModel
class PreferencesViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(UserPreferencesState())
    val uiState: StateFlow<UserPreferencesState> = _uiState.asStateFlow()

    fun toggleMetricSystem(enabled: Boolean) {
        _uiState.update { it.copy(useMetricSystem = enabled) }
    }

    fun toggleNotifications(enabled: Boolean) {
        _uiState.update { it.copy(enableNotifications = enabled) }
    }

    fun setDarkMode(mode: DarkModeConfig) {
        _uiState.update { it.copy(darkMode = mode) }
    }

    fun toggleSyncOnlyWifi(enabled: Boolean) {
        _uiState.update { it.copy(syncOnlyWifi = enabled) }
    }

    fun toggleVoiceAlerts(enabled: Boolean) {
        _uiState.update { it.copy(voiceAlerts = enabled) }
    }
}
