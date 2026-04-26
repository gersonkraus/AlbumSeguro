package com.familiaaco.repository

import android.content.Context
import com.familiaaco.data.local.TokenManager
import com.familiaaco.data.models.LoginRequest
import com.familiaaco.data.models.RegistroRequest
import com.familiaaco.network.ApiClient

class AuthRepository(private val context: Context) {
    private val apiService = ApiClient.getApiService(context)
    private val tokenManager = TokenManager(context)

    suspend fun login(email: String, senha: String): Result<String> {
        return try {
            val response = apiService.login(LoginRequest(email, senha))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                tokenManager.saveToken(body.token)
                tokenManager.saveUserRole(body.usuario.role)
                body.refreshToken?.let { tokenManager.saveRefreshToken(it) }
                Result.success(body.token)
            } else {
                Result.failure(Exception("Erro ${response.code()}: Login falhou"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registrar(nome: String, email: String, senha: String, telefone: String?): Result<String> {
        return try {
            val response = apiService.registrar(RegistroRequest(nome, email, senha, telefone))
            if (response.isSuccessful && response.body() != null) {
                Result.success("Admin registrado com sucesso")
            } else {
                Result.failure(Exception("Erro ${response.code()}: Falha ao registrar"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        try {
            apiService.logout()
            tokenManager.clearAll()
        } catch (e: Exception) {
            tokenManager.clearAll()
        }
    }

    fun getToken(): String? = tokenManager.getToken()

    fun isLoggedIn(): Boolean = tokenManager.getToken() != null

    fun isAdmin(): Boolean {
        val role = tokenManager.getUserRole()
        return role == "admin" || role == "super_admin"
    }
}
