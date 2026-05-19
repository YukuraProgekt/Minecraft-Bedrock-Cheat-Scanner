package com.rollixmc.anticheat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ScanResult(
    val path: String,
    val reason: String,
    val md5Hash: String = ""
)

sealed class UiState {
    object Idle : UiState()
    data class Scanning(val scanned: Int = 0, val total: Int = 0) : UiState()
    data class Results(val items: List<ScanResult>) : UiState()
}

class MainViewModel : ViewModel() {

    private val _state = MutableStateFlow<UiState>(UiState.Idle)
    val state = _state.asStateFlow()

    private val scanner = CheatScanner()

    fun scan() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = UiState.Scanning(0, 0)

            val results = scanner.scanAll { scanned, total ->
                _state.value = UiState.Scanning(scanned, total)
            }

            _state.value = UiState.Results(results)
        }
    }
}