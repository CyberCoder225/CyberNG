package com.v2ray.ang.ui.settings

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.v2ray.ang.handler.SettingsManager
import com.v2ray.ang.ui.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * State for the simplified settings screen. It only exposes the connection mode.
 */
class SettingsViewModel(application: Application) : BaseViewModel(application) {

    private val _connectionMode = MutableStateFlow(SettingsManager.getConnectionMode())
    val connectionMode: StateFlow<String> = _connectionMode.asStateFlow()

    /**
     * Saves [mode] off the main thread. Selecting the current mode does nothing.
     */
    fun setConnectionMode(mode: String) {
        if (mode == _connectionMode.value) return
        _connectionMode.value = mode
        viewModelScope.launch(Dispatchers.IO) {
            SettingsManager.setConnectionMode(mode)
        }
    }
}
