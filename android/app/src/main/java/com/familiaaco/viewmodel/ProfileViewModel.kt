package com.familiaaco.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familiaaco.data.models.UsuarioDTO
import com.familiaaco.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(context: Context) : ViewModel() {
    private val repo = ProfileRepository(context)
    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState: StateFlow<ProfileState> = _profileState
    private val _senhaState = MutableStateFlow<SenhaState>(SenhaState.Idle)
    val senhaState: StateFlow<SenhaState> = _senhaState

    fun carregarPerfil() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            repo.getPerfil()
                .onSuccess { _profileState.value = ProfileState.Success(it) }
                .onFailure { _profileState.value = ProfileState.Error(it.message ?: "Erro") }
        }
    }

    fun atualizarPerfil(nome: String, telefone: String?) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            repo.atualizarPerfil(nome, telefone)
                .onSuccess { _profileState.value = ProfileState.Success(it) }
                .onFailure { _profileState.value = ProfileState.Error(it.message ?: "Erro") }
        }
    }

    fun alterarSenha(senhaAtual: String, novaSenha: String) {
        viewModelScope.launch {
            _senhaState.value = SenhaState.Loading
            repo.alterarSenha(senhaAtual, novaSenha)
                .onSuccess { _senhaState.value = SenhaState.Success }
                .onFailure { _senhaState.value = SenhaState.Error(it.message ?: "Erro") }
        }
    }

    sealed class ProfileState {
        object Idle : ProfileState()
        object Loading : ProfileState()
        data class Success(val usuario: UsuarioDTO) : ProfileState()
        data class Error(val message: String) : ProfileState()
    }

    fun resetSenhaState() {
        _senhaState.value = SenhaState.Idle
    }

    sealed class SenhaState {
        object Idle : SenhaState()
        object Loading : SenhaState()
        object Success : SenhaState()
        data class Error(val message: String) : SenhaState()
    }
}
