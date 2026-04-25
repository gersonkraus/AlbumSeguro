package com.familiaaco.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familiaaco.data.models.CriancaDTO
import com.familiaaco.repository.ChildrenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChildrenViewModel(context: Context) : ViewModel() {
    private val childrenRepository = ChildrenRepository(context)
    private val _childrenState = MutableStateFlow<ChildrenState>(ChildrenState.Idle)
    val childrenState: StateFlow<ChildrenState> = _childrenState
    private val _tokenState = MutableStateFlow<TokenState>(TokenState.Idle)
    val tokenState: StateFlow<TokenState> = _tokenState

    fun listarCriancas() {
        viewModelScope.launch {
            _childrenState.value = ChildrenState.Loading
            childrenRepository.listarCriancas()
                .onSuccess { _childrenState.value = ChildrenState.Success(it) }
                .onFailure { _childrenState.value = ChildrenState.Error(it.message ?: "Erro") }
        }
    }

    fun criarCrianca(nome: String, dataNascimento: String, descricao: String?) {
        viewModelScope.launch {
            _childrenState.value = ChildrenState.Loading
            childrenRepository.criarCrianca(nome, dataNascimento, descricao)
                .onSuccess { listarCriancas() }
                .onFailure { _childrenState.value = ChildrenState.Error(it.message ?: "Erro") }
        }
    }

    fun gerarToken(criancaId: String) {
        viewModelScope.launch {
            _tokenState.value = TokenState.Loading
            childrenRepository.gerarToken(criancaId)
                .onSuccess { _tokenState.value = TokenState.Success(it) }
                .onFailure { _tokenState.value = TokenState.Error(it.message ?: "Erro") }
        }
    }

    sealed class ChildrenState {
        object Idle : ChildrenState()
        object Loading : ChildrenState()
        data class Success(val criancas: List<CriancaDTO>) : ChildrenState()
        data class Error(val message: String) : ChildrenState()
    }

    sealed class TokenState {
        object Idle : TokenState()
        object Loading : TokenState()
        data class Success(val token: String) : TokenState()
        data class Error(val message: String) : TokenState()
    }
}
