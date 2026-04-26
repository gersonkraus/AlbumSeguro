package com.familiaaco.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familiaaco.data.models.MidiaDTO
import com.familiaaco.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChildAlbumViewModel(context: Context) : ViewModel() {
    private val mediaRepository = MediaRepository(context)

    private val _albumState = MutableStateFlow<AlbumState>(AlbumState.Idle)
    val albumState: StateFlow<AlbumState> = _albumState

    private val _hasMore = MutableStateFlow(false)
    val hasMore: StateFlow<Boolean> = _hasMore

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore

    private var currentPage = 0
    private var accumulatedMidias: List<MidiaDTO> = emptyList()
    private var currentToken: String = ""
    private val pageSize = 30

    fun carregarAlbum(token: String) {
        currentToken = token
        viewModelScope.launch {
            currentPage = 0
            accumulatedMidias = emptyList()
            _albumState.value = AlbumState.Loading
            mediaRepository.getAlbumByToken(token, 0, pageSize)
                .onSuccess { response ->
                    accumulatedMidias = response.midias
                    _hasMore.value = response.hasMore
                    _albumState.value = AlbumState.Success(token, accumulatedMidias)
                }
                .onFailure { _albumState.value = AlbumState.Error(it.message ?: "Erro ao carregar álbum") }
        }
    }

    fun carregarMais() {
        if (_isLoadingMore.value || !_hasMore.value || currentToken.isEmpty()) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            currentPage++
            mediaRepository.getAlbumByToken(currentToken, currentPage, pageSize)
                .onSuccess { response ->
                    accumulatedMidias = accumulatedMidias + response.midias
                    _hasMore.value = response.hasMore
                    val current = _albumState.value
                    if (current is AlbumState.Success) {
                        _albumState.value = current.copy(midias = accumulatedMidias)
                    }
                }
                .onFailure { currentPage-- }
            _isLoadingMore.value = false
        }
    }

    sealed class AlbumState {
        object Idle : AlbumState()
        object Loading : AlbumState()
        data class Success(val token: String, val midias: List<MidiaDTO>) : AlbumState()
        data class Error(val message: String) : AlbumState()
    }
}
