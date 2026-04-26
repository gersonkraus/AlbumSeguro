package com.familiaaco.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familiaaco.data.models.CriancaDTO
import com.familiaaco.data.models.TokenResponse
import com.familiaaco.repository.ChildrenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class ChildrenViewModel(context: Context) : ViewModel() {
    private val childrenRepository = ChildrenRepository(context)
    private val _childrenState = MutableStateFlow<ChildrenState>(ChildrenState.Idle)
    val childrenState: StateFlow<ChildrenState> = _childrenState
    private val _tokenState = MutableStateFlow<TokenState>(TokenState.Idle)
    val tokenState: StateFlow<TokenState> = _tokenState
    private val _criancaAtual = MutableStateFlow<CriancaDTO?>(null)
    val criancaAtual: StateFlow<CriancaDTO?> = _criancaAtual

    fun carregarCrianca(id: String) {
        viewModelScope.launch {
            childrenRepository.obterCrianca(id)
                .onSuccess { crianca ->
                    _criancaAtual.value = crianca
                    if (!crianca.tokenAcesso.isNullOrBlank()) {
                        _tokenState.value = TokenState.Success(crianca.tokenAcesso, null)
                    }
                }
                .onFailure {
                    _criancaAtual.value = null
                    _childrenState.value = ChildrenState.Error(it.message ?: "Erro ao carregar criança")
                }
        }
    }

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

    fun uploadFotoPerfil(id: String, file: File, mime: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            childrenRepository.uploadFotoPerfil(id, file, mime)
                .onSuccess { onSuccess(it) }
                .onFailure { /* silencioso */ }
        }
    }

    fun editarCrianca(id: String, nome: String, dataNascimento: String, descricao: String?) {
        viewModelScope.launch {
            _childrenState.value = ChildrenState.Loading
            childrenRepository.editarCrianca(id, nome, dataNascimento, descricao)
                .onSuccess { listarCriancas() }
                .onFailure { _childrenState.value = ChildrenState.Error(it.message ?: "Erro") }
        }
    }

    fun deletarCrianca(id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            childrenRepository.deletarCrianca(id)
                .onSuccess { onSuccess() }
                .onFailure { _childrenState.value = ChildrenState.Error(it.message ?: "Erro ao deletar") }
        }
    }

    fun gerarToken(criancaId: String, diasValidade: Int = 30) {
        viewModelScope.launch {
            _tokenState.value = TokenState.Loading
            childrenRepository.gerarToken(criancaId, diasValidade)
                .onSuccess { _tokenState.value = TokenState.Success(it.token, it.childAlbumUrl) }
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
        data class Success(val token: String, val childAlbumUrl: String?) : TokenState()
        data class Error(val message: String) : TokenState()
    }
}
