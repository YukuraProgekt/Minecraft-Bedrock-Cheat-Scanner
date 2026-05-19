package com.rollixmc.anticheat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    sealed class UiState {
        object Idle : UiState()
        object Scanning : UiState()
        data class Results(val items: List<ScanResult>) : UiState()
    }

    private val _state = MutableStateFlow<UiState>(UiState.Idle)
    val state: StateFlow<UiState> = _state

    private val _events = Channel<Event>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun requestManageAllFiles() {
        viewModelScope.launch {
            _events.send(Event.ShowManageAllFilesIntent)
        }
    }

    fun scan() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.emit(UiState.Scanning)
            val scanner = CheatScanner()
            val results = scanner.scan(getApplication())
            _state.emit(UiState.Results(results))
        }
    }

    sealed class Event {
        object ShowManageAllFilesIntent : Event()
    }
}
