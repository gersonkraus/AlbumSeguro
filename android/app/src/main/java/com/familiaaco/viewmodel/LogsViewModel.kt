package com.familiaaco.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familiaaco.data.models.LogDTO
import com.familiaaco.repository.LogsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LogsViewModel(context: Context) : ViewModel() {
    private val repo = LogsRepository(context)
    private val _logsState = MutableStateFlow<LogsState>(LogsState.Idle)
    val logsState: StateFlow<LogsState> = _logsState

    fun carregarLogs() {
        viewModelScope.launch {
            _logsState.value = LogsState.Loading
            repo.listarLogs()
                .onSuccess { _logsState.value = LogsState.Success(it) }
                .onFailure { _logsState.value = LogsState.Error(it.message ?: "Erro ao carregar logs") }
        }
    }

    sealed class LogsState {
        object Idle : LogsState()
        object Loading : LogsState()
        data class Success(val logs: List<LogDTO>) : LogsState()
        data class Error(val message: String) : LogsState()
    }
}
