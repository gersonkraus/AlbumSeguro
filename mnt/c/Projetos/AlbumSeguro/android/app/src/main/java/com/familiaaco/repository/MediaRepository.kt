package com.familiaaco.repository

import android.content.Context
import com.familiaaco.data.models.AlbumResponse
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
        dataMomento: String
    ): Result<MidiaDTO> {
        return try {
            val requestBody = file.asRequestBody("*/*".toMediaType())
            val filePart = MultipartBody.Part.createFormData("file", file.name, requestBody)
            val descricaoPart = descricao.toRequestBody("text/plain".toMediaType())
            val dataPart = dataMomento.toRequestBody("text/plain".toMediaType())
            val response = apiService.uploadMidia(criancaId, filePart, descricaoPart, dataPart)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.midia)
            } else {
                Result.failure(Exception("Falha ao fazer upload"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listarMidia(criancaId: String): Result<List<MidiaDTO>> {
        return try {
            val response = apiService.listarMidia(criancaId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.midias)
            } else {
                Result.failure(Exception("Falha ao listar mídia"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletarMidia(midiaId: String): Result<Unit> {
        return try {
            val response = apiService.deletarMidia(midiaId)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Falha ao deletar mídia"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAlbumByToken(token: String): Result<AlbumResponse> {
        return try {
            val response = apiService.getAlbumByToken(token)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Álbum não encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
