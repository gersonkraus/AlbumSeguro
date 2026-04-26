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

    private val _hasMore = MutableStateFlow(false)
    val hasMore: StateFlow<Boolean> = _hasMore

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore

    private var currentPage = 0
    private var accumulatedMidias: List<MidiaDTO> = emptyList()
    private val pageSize = 30

    fun listarMidia(criancaId: String, tipo: String? = null, ordem: String? = null) {
        viewModelScope.launch {
            currentPage = 0
            accumulatedMidias = emptyList()
            _mediaState.value = MediaState.Loading
            mediaRepository.listarMidia(criancaId, tipo, ordem, 0, pageSize)
                .onSuccess { response ->
                    accumulatedMidias = response.midias
                    _hasMore.value = response.hasMore
                    _mediaState.value = MediaState.Success(accumulatedMidias)
                }
                .onFailure { _mediaState.value = MediaState.Error(it.message ?: "Erro") }
        }
    }

    fun carregarMaisMidia(criancaId: String, tipo: String? = null, ordem: String? = null) {
        if (_isLoadingMore.value || !_hasMore.value) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            currentPage++
            mediaRepository.listarMidia(criancaId, tipo, ordem, currentPage, pageSize)
                .onSuccess { response ->
                    accumulatedMidias = accumulatedMidias + response.midias
                    _hasMore.value = response.hasMore
                    _mediaState.value = MediaState.Success(accumulatedMidias)
                }
                .onFailure { currentPage-- }
            _isLoadingMore.value = false
        }
    }

    fun uploadMidia(criancaId: String, file: File, descricao: String, dataMomento: String, thumbnail: File? = null) {
        uploadMidias(criancaId, listOf(file), listOf(thumbnail), descricao, dataMomento)
    }

    fun uploadMidias(
        criancaId: String,
        files: List<File>,
        thumbnails: List<File?> = emptyList(),
        descricao: String,
        dataMomento: String
    ) {
        viewModelScope.launch {
            if (files.isEmpty()) {
                _mediaState.value = MediaState.Error("Nenhum arquivo selecionado")
                return@launch
            }
            val total = files.size
            var successCount = 0
            val failedFiles = mutableListOf<String>()
            files.forEachIndexed { index, file ->
                _mediaState.value = MediaState.Uploading(index + 1, total, file.name)
                val thumbnail = thumbnails.getOrNull(index)
                val result = mediaRepository.uploadMidia(criancaId, file, descricao, dataMomento, thumbnail)
                if (result.isFailure) failedFiles.add(file.name) else successCount++
            }
            _mediaState.value = MediaState.UploadSummary(
                successCount = successCount,
                failCount = failedFiles.size,
                total = total,
                failedFileNames = failedFiles
            )
        }
    }

    fun editarMidia(midiaId: String, descricao: String, criancaId: String) {
        viewModelScope.launch {
            _mediaState.value = MediaState.Loading
            mediaRepository.editarMidia(midiaId, descricao)
                .onSuccess { listarMidia(criancaId) }
                .onFailure { _mediaState.value = MediaState.Error(it.message ?: "Erro ao editar") }
        }
    }

    fun deletarMidia(midiaId: String, criancaId: String) {
        viewModelScope.launch {
            _mediaState.value = MediaState.Loading
            mediaRepository.deletarMidia(midiaId)
                .onSuccess { listarMidia(criancaId) }
                .onFailure { _mediaState.value = MediaState.Error(it.message ?: "Erro ao deletar") }
        }
    }

    sealed class MediaState {
        object Idle : MediaState()
        object Loading : MediaState()
        data class Uploading(val current: Int, val total: Int, val fileName: String) : MediaState()
        data class UploadSummary(
            val successCount: Int,
            val failCount: Int,
            val total: Int,
            val failedFileNames: List<String>
        ) : MediaState()
        data class Success(val midias: List<MidiaDTO>) : MediaState()
        data class Error(val message: String) : MediaState()
    }
}
