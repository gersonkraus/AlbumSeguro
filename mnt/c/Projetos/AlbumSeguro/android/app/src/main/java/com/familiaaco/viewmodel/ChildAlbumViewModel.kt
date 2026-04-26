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

    fun carregarAlbum(token: String) {
        viewModelScope.launch {
            _albumState.value = AlbumState.Loading
            mediaRepository.getAlbumByToken(token)
                .onSuccess { _albumState.value = AlbumState.Success(it.midias) }
                .onFailure { _albumState.value = AlbumState.Error(it.message ?: "Erro ao carregar álbum") }
        }
    }

    sealed class AlbumState {
        object Idle : AlbumState()
        object Loading : AlbumState()
        data class Success(val midias: List<MidiaDTO>) : AlbumState()
        data class Error(val message: String) : AlbumState()
    }
}
