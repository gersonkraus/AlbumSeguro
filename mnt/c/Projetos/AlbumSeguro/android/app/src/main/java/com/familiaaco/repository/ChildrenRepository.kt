package com.familiaaco.repository

import android.content.Context
import com.familiaaco.data.models.CriarCriancaRequest
import com.familiaaco.data.models.CriancaDTO
import com.familiaaco.data.models.EditarCriancaRequest
import com.familiaaco.network.ApiClient

class ChildrenRepository(private val context: Context) {
    private val apiService = ApiClient.getApiService(context)

    suspend fun criarCrianca(nome: String, dataNascimento: String, descricao: String?): Result<CriancaDTO> {
        return try {
            val response = apiService.criarCrianca(CriarCriancaRequest(nome, dataNascimento, descricao))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.crianca)
            } else {
                Result.failure(Exception("Falha ao criar criança"))
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
                Result.failure(Exception("Falha ao listar crianças"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editarCrianca(id: String, nome: String?, dataNascimento: String?, descricao: String?): Result<CriancaDTO> {
        return try {
            val response = apiService.editarCrianca(id, EditarCriancaRequest(nome, dataNascimento, descricao))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.crianca)
            } else {
                Result.failure(Exception("Falha ao editar criança"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletarCrianca(id: String): Result<Unit> {
        return try {
            val response = apiService.deletarCrianca(id)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Falha ao deletar criança"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun gerarToken(id: String): Result<String> {
        return try {
            val response = apiService.gerarToken(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.token)
            } else {
                Result.failure(Exception("Falha ao gerar token"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
