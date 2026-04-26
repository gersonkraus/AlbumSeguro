package com.familiaaco.repository

import android.content.Context
import android.webkit.MimeTypeMap
import com.familiaaco.data.models.AlbumResponse
import com.familiaaco.data.models.ListaMidiaResponse
import com.familiaaco.data.models.MidiaDTO
import com.familiaaco.network.ApiClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class MediaRepository(private val context: Context) {
    private val apiService = ApiClient.getApiService(context)

    suspend fun uploadMidia(
        criancaId: String,
        file: File,
        descricao: String,
        dataMomento: String,
        thumbnailFile: File? = null
    ): Result<MidiaDTO> {
        return try {
            val ext = file.extension.lowercase()
            val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "application/octet-stream"
            val filePart = MultipartBody.Part.createFormData("file", file.name, file.asRequestBody(mime.toMediaType()))
            val descricaoPart = descricao.toRequestBody("text/plain".toMediaType())
            val dataPart = dataMomento.toRequestBody("text/plain".toMediaType())
            val thumbnailPart = thumbnailFile?.let {
                MultipartBody.Part.createFormData("thumbnail", it.name, it.asRequestBody("image/jpeg".toMediaType()))
            }
            val response = apiService.uploadMidia(criancaId, filePart, descricaoPart, dataPart, thumbnailPart)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.midia)
            } else {
                Result.failure(Exception("Erro ${response.code()}: Falha ao fazer upload"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listarMidia(
        criancaId: String,
        tipo: String? = null,
        ordem: String? = null,
        page: Int = 0,
        limit: Int = 30
    ): Result<ListaMidiaResponse> {
        return try {
            val response = apiService.listarMidia(criancaId, tipo, ordem, page, limit)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erro ${response.code()}: Falha ao listar mídia"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editarMidia(midiaId: String, descricao: String): Result<MidiaDTO> {
        return try {
            val response = apiService.editarMidia(midiaId, com.familiaaco.data.models.EditarMidiaRequest(descricao))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.midia)
            } else {
                Result.failure(Exception("Erro ${response.code()}: Falha ao editar mídia"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletarMidia(midiaId: String): Result<Unit> {
        return try {
            val response = apiService.deletarMidia(midiaId)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Erro ${response.code()}: Falha ao deletar mídia"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAlbumByToken(token: String, page: Int = 0, limit: Int = 30): Result<AlbumResponse> {
        return try {
            val response = apiService.getAlbumByToken(token, page, limit)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erro ${response.code()}: Álbum não encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
