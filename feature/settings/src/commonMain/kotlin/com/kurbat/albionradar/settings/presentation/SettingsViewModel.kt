package com.kurbat.albionradar.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kurbat.albionradar.core.capture.CaptureStats
import com.kurbat.albionradar.core.capture.PacketCapturer
import com.kurbat.albionradar.core.settings.AdapterConfig
import com.kurbat.albionradar.core.settings.AppConfig
import com.kurbat.albionradar.core.settings.CaptureConfig
import com.kurbat.albionradar.core.settings.DisplayConfig
import com.kurbat.albionradar.core.settings.FilterConfig
import com.kurbat.albionradar.core.settings.persistence.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val packetCapturer: PacketCapturer
) : ViewModel() {

    private val _currentConfig = MutableStateFlow(AppConfig.Companion.default())
    val currentConfig: StateFlow<AppConfig> = _currentConfig.asStateFlow()

    private val _hasUnsavedChanges = MutableStateFlow(false)
    val hasUnsavedChanges: StateFlow<Boolean> = _hasUnsavedChanges.asStateFlow()

    val captureStats: StateFlow<CaptureStats> = packetCapturer.stats

    init {
        viewModelScope.launch {
            settingsRepository.updatesFlow.collect { config ->
                _currentConfig.value = config
            }
        }
    }

    fun updateFilter(filter: FilterConfig) {
        _currentConfig.value = _currentConfig.value.copy(filter = filter)
        _hasUnsavedChanges.value = true
    }

    fun updateDisplay(display: DisplayConfig) {
        _currentConfig.value = _currentConfig.value.copy(display = display)
        _hasUnsavedChanges.value = true
    }

    fun updateAdapter(adapter: AdapterConfig) {
        _currentConfig.value = _currentConfig.value.copy(adapter = adapter)
        _hasUnsavedChanges.value = true
    }

    fun updateCapture(capture: CaptureConfig) {
        _currentConfig.value = _currentConfig.value.copy(capture = capture)
        _hasUnsavedChanges.value = true
    }

    fun save(onSaved: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.save(_currentConfig.value)
            _hasUnsavedChanges.value = false
            onSaved()
        }
    }

    fun cancel(onCancelled: () -> Unit) {
        _hasUnsavedChanges.value = false
        onCancelled()
    }

    fun reset() {
        _currentConfig.value = AppConfig.default()
        _hasUnsavedChanges.value = true
    }
}
