package com.familiaaco.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familiaaco.data.models.AppConfigDTO
import com.familiaaco.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppConfigViewModel(context: Context) : ViewModel() {
    private val adminRepository = AdminRepository(context)
    private val _state = MutableStateFlow<AppConfigState>(AppConfigState.Idle)
    val state: StateFlow<AppConfigState> = _state

    fun carregarConfig() {
        viewModelScope.launch {
            _state.value = AppConfigState.Loading
            adminRepository.getAppConfig()
                .onSuccess { _state.value = AppConfigState.Success(it) }
                .onFailure { _state.value = AppConfigState.Error(it.message ?: "Erro ao carregar configuração") }
        }
    }

    fun salvarConfig(childAlbumBaseUrl: String) {
        viewModelScope.launch {
            _state.value = AppConfigState.Loading
            adminRepository.atualizarAppConfig(childAlbumBaseUrl)
                .onSuccess { _state.value = AppConfigState.Success(it, "Configuração salva com sucesso") }
                .onFailure { _state.value = AppConfigState.Error(it.message ?: "Erro ao salvar configuração") }
        }
    }

    sealed class AppConfigState {
        object Idle : AppConfigState()
        object Loading : AppConfigState()
        data class Success(val config: AppConfigDTO, val message: String? = null) : AppConfigState()
        data class Error(val message: String) : AppConfigState()
    }
}
