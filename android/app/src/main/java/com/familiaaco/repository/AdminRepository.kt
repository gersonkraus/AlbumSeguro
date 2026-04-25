package com.familiaaco.repository

import android.content.Context
import com.familiaaco.data.models.EditarAdminRequest
import com.familiaaco.data.models.UsuarioDTO
import com.familiaaco.network.ApiClient

class AdminRepository(private val context: Context) {
    private val apiService = ApiClient.getApiService(context)

    suspend fun listarAdmins(): Result<List<UsuarioDTO>> = try {
        val r = apiService.listarAdmins()
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!.admins)
        else Result.failure(Exception("Falha ao listar admins"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun editarAdmin(id: String, permissoes: List<String>?, ativo: Boolean?): Result<UsuarioDTO> = try {
        val r = apiService.editarAdmin(id, EditarAdminRequest(permissoes, ativo))
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!.usuario)
        else Result.failure(Exception("Falha ao editar admin"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun deletarAdmin(id: String): Result<Unit> = try {
        val r = apiService.deletarAdmin(id)
        if (r.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Falha ao desativar admin"))
    } catch (e: Exception) { Result.failure(e) }
}
