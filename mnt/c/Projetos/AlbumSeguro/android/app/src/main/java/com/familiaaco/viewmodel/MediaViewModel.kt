package com.familiaaco.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familiaaco.data.models.MidiaDTO
import com.familiaaco.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class MediaViewModel(context: Context) : ViewModel() {
    private val mediaRepository = MediaRepository(context)
    private val _mediaState = MutableStateFlow<MediaState>(MediaState.Idle)
    val mediaState: StateFlow<MediaState> = _mediaState

    fun listarMidia(criancaId: String) {
        viewModelScope.launch {
            _mediaState.value = MediaState.Loading
            mediaRepository.listarMidia(criancaId)
                .onSuccess { _mediaState.value = MediaState.Success(it) }
                .onFailure { _mediaState.value = MediaState.Error(it.message ?: "Erro") }
        }
    }

    fun uploadMidia(criancaId: String, file: File, descricao: String, dataMomento: String) {
        viewModelScope.launch {
            _mediaState.value = MediaState.Loading
            mediaRepository.uploadMidia(criancaId, file, descricao, dataMomento)
                .onSuccess { listarMidia(criancaId) }
                .onFailure { _mediaState.value = MediaState.Error(it.message ?: "Erro no upload") }
        }
    }

    fun deletarMidia(midiaId: String, criancaId: String) {
        viewModelScope.launch {
            mediaRepository.deletarMidia(midiaId)
                .onSuccess { listarMidia(criancaId) }
                .onFailure { _mediaState.value = MediaState.Error(it.message ?: "Erro ao deletar") }
        }
    }

    sealed class MediaState {
        object Idle : MediaState()
        object Loading : MediaState()
        data class Success(val midias: List<MidiaDTO>) : MediaState()
        data class Error(val message: String) : MediaState()
    }
}
