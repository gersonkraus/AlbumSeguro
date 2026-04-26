package com.familiaaco.repository

import android.content.Context
import com.familiaaco.data.models.AppConfigDTO
import com.familiaaco.data.models.AtualizarAppConfigRequest
import com.familiaaco.data.models.EditarAdminRequest
import com.familiaaco.data.models.RegistroRequest
import com.familiaaco.data.models.UsuarioDTO
import com.familiaaco.network.ApiClient

class AdminRepository(private val context: Context) {
    private val apiService = ApiClient.getApiService(context)

    suspend fun criarAdmin(nome: String, email: String, senha: String): Result<UsuarioDTO> = try {
        val r = apiService.registrar(RegistroRequest(nome, email, senha))
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!.usuario)
        else Result.failure(Exception("Erro ${r.code()}: Falha ao criar admin"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun listarAdmins(): Result<List<UsuarioDTO>> = try {
        val r = apiService.listarAdmins()
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!.admins)
        else Result.failure(Exception("Erro ${r.code()}: Falha ao listar admins"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun editarAdmin(id: String, permissoes: List<String>?, ativo: Boolean?): Result<UsuarioDTO> = try {
        val r = apiService.editarAdmin(id, EditarAdminRequest(permissoes, ativo))
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!.usuario)
        else Result.failure(Exception("Erro ${r.code()}: Falha ao editar admin"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun deletarAdmin(id: String): Result<Unit> = try {
        val r = apiService.deletarAdmin(id)
        if (r.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Erro ${r.code()}: Falha ao desativar admin"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getAppConfig(): Result<AppConfigDTO> = try {
        val r = apiService.getAppConfig()
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!.config)
        else Result.failure(Exception("Erro ${r.code()}: Falha ao carregar configuração"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun atualizarAppConfig(childAlbumBaseUrl: String): Result<AppConfigDTO> = try {
        val r = apiService.atualizarAppConfig(AtualizarAppConfigRequest(childAlbumBaseUrl))
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!.config)
        else Result.failure(Exception("Erro ${r.code()}: Falha ao salvar configuração"))
    } catch (e: Exception) { Result.failure(e) }
}
