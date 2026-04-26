package com.familiaaco.repository

import android.content.Context
import com.familiaaco.data.models.CriarCriancaRequest
import com.familiaaco.data.models.CriancaDTO
import com.familiaaco.data.models.EditarCriancaRequest
import com.familiaaco.data.models.TokenResponse
import com.familiaaco.network.ApiClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ChildrenRepository(private val context: Context) {
    private val apiService = ApiClient.getApiService(context)

    suspend fun criarCrianca(nome: String, dataNascimento: String, descricao: String?): Result<CriancaDTO> {
        return try {
            val response = apiService.criarCrianca(CriarCriancaRequest(nome, dataNascimento, descricao))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.crianca)
            } else {
                Result.failure(Exception("Erro ${response.code()}: Falha ao criar criança"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listarCriancas(): Result<List<CriancaDTO>> {
        return try {
            val response = apiService.listarCriancas()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.criancas)
            } else {
                Result.failure(Exception("Erro ${response.code()}: Falha ao listar crianças"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obterCrianca(id: String): Result<CriancaDTO> {
        return try {
            val response = apiService.obterCrianca(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.crianca)
            } else {
                Result.failure(Exception("Erro ${response.code()}: Falha ao obter criança"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadFotoPerfil(id: String, file: File, mime: String): Result<String> {
        return try {
            val reqBody = file.asRequestBody(mime.toMediaType())
            val part = MultipartBody.Part.createFormData("foto", file.name, reqBody)
            val response = apiService.uploadFotoPerfil(id, part)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.fotoPerfil)
            } else {
                Result.failure(Exception("Erro ${response.code()}: Falha ao fazer upload da foto"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun editarCrianca(id: String, nome: String?, dataNascimento: String?, descricao: String?): Result<CriancaDTO> {
        return try {
            val response = apiService.editarCrianca(id, EditarCriancaRequest(nome, dataNascimento, descricao))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.crianca)
            } else {
                Result.failure(Exception("Erro ${response.code()}: Falha ao editar criança"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletarCrianca(id: String): Result<Unit> {
        return try {
            val response = apiService.deletarCrianca(id)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Erro ${response.code()}: Falha ao deletar criança"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun gerarToken(id: String, diasValidade: Int = 30): Result<TokenResponse> {
        return try {
            val response = apiService.gerarToken(id, com.familiaaco.data.models.GerarTokenRequest(diasValidade))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erro ${response.code()}: Falha ao gerar token"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
