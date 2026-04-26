package com.familiaaco.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familiaaco.data.models.UsuarioDTO
import com.familiaaco.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminViewModel(context: Context) : ViewModel() {
    private val adminRepository = AdminRepository(context)
    private val _adminState = MutableStateFlow<AdminState>(AdminState.Idle)
    val adminState: StateFlow<AdminState> = _adminState

    fun criarAdmin(nome: String, email: String, senha: String, onResult: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            _adminState.value = AdminState.Loading
            adminRepository.criarAdmin(nome, email, senha)
                .onSuccess {
                    listarAdmins()
                    onResult?.invoke(true)
                }
                .onFailure {
                    _adminState.value = AdminState.Error(it.message ?: "Erro ao criar admin")
                    onResult?.invoke(false)
                }
        }
    }

    fun listarAdmins() {
        viewModelScope.launch {
            _adminState.value = AdminState.Loading
            adminRepository.listarAdmins()
                .onSuccess { _adminState.value = AdminState.Success(it) }
                .onFailure { _adminState.value = AdminState.Error(it.message ?: "Erro") }
        }
    }

    fun editarAdmin(id: String, permissoes: List<String>?, ativo: Boolean?) {
        viewModelScope.launch {
            _adminState.value = AdminState.Loading
            adminRepository.editarAdmin(id, permissoes, ativo)
                .onSuccess { listarAdmins() }
                .onFailure { _adminState.value = AdminState.Error(it.message ?: "Erro") }
        }
    }

    fun deletarAdmin(id: String) {
        viewModelScope.launch {
            adminRepository.deletarAdmin(id)
                .onSuccess { listarAdmins() }
                .onFailure { _adminState.value = AdminState.Error(it.message ?: "Erro") }
        }
    }

    sealed class AdminState {
        object Idle : AdminState()
        object Loading : AdminState()
        data class Success(val admins: List<UsuarioDTO>) : AdminState()
        data class Error(val message: String) : AdminState()
    }
}
