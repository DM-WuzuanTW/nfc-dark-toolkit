package com.wuzuan.nfcdarktoolkit.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wuzuan.nfcdarktoolkit.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 設定 ViewModel
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    val themeMode: StateFlow<String> = settingsRepository.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "system"
    )
    
    val autoSaveHistory: StateFlow<Boolean> = settingsRepository.autoSaveHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )
    
    val defaultWriteFormat: StateFlow<String> = settingsRepository.defaultWriteFormat.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "text"
    )
    
    val safeModeEnabled: StateFlow<Boolean> = settingsRepository.safeModeEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
    
    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }
    
    fun setAutoSaveHistory(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoSaveHistory(enabled)
        }
    }
    
    fun setDefaultWriteFormat(format: String) {
        viewModelScope.launch {
            settingsRepository.setDefaultWriteFormat(format)
        }
    }
    
    fun setSafeModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSafeModeEnabled(enabled)
        }
    }
}

