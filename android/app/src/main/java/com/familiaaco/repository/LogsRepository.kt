package com.familiaaco.repository

import android.content.Context
import com.familiaaco.data.models.LogDTO
import com.familiaaco.network.ApiClient

class LogsRepository(private val context: Context) {
    private val api = ApiClient.getApiService(context)

    suspend fun listarLogs(): Result<List<LogDTO>> = try {
        val r = api.listarLogs()
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!.logs)
        else Result.failure(Exception("Erro ${r.code()}: Falha ao listar logs"))
    } catch (e: Exception) { Result.failure(e) }
}
