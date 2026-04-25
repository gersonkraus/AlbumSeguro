package com.familiaaco.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familiaaco.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(context: Context) : ViewModel() {
    private val authRepository = AuthRepository(context)
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(email: String, senha: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            authRepository.login(email, senha)
                .onSuccess { _loginState.value = LoginState.Success }
                .onFailure { _loginState.value = LoginState.Error(it.message ?: "Erro desconhecido") }
        }
    }

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }
}
