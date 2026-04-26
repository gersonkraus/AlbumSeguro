package com.familiaaco.repository

import android.content.Context
import com.familiaaco.data.models.AtualizarPerfilRequest
import com.familiaaco.data.models.AlterarSenhaRequest
import com.familiaaco.data.models.UsuarioDTO
import com.familiaaco.network.ApiClient

class ProfileRepository(private val context: Context) {
    private val api = ApiClient.getApiService(context)

    suspend fun getPerfil(): Result<UsuarioDTO> = try {
        val r = api.getPerfil()
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!.usuario)
        else Result.failure(Exception("Erro ${r.code()}: Falha ao carregar perfil"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun atualizarPerfil(nome: String, telefone: String?): Result<UsuarioDTO> = try {
        val r = api.atualizarPerfil(AtualizarPerfilRequest(nome, telefone))
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!.usuario)
        else Result.failure(Exception("Erro ${r.code()}: Falha ao atualizar perfil"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun alterarSenha(senhaAtual: String, novaSenha: String): Result<Unit> = try {
        val r = api.alterarSenha(AlterarSenhaRequest(senhaAtual, novaSenha))
        if (r.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Erro ${r.code()}: Falha ao alterar senha"))
    } catch (e: Exception) { Result.failure(e) }
}
