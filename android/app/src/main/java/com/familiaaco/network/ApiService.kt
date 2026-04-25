package com.familiaaco.network

import com.familiaaco.data.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Auth
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/registrar")
    suspend fun registrar(@Body request: RegistroRequest): Response<UsuarioResponse>

    @GET("auth/perfil")
    suspend fun getPerfil(): Response<UsuarioResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    // Crianças
    @POST("children/")
    suspend fun criarCrianca(@Body request: CriarCriancaRequest): Response<CriancaResponse>

    @GET("children/")
    suspend fun listarCriancas(): Response<ListaCriancasResponse>

    @GET("children/{id}")
    suspend fun obterCrianca(@Path("id") id: String): Response<CriancaResponse>

    @PUT("children/{id}")
    suspend fun editarCrianca(
        @Path("id") id: String,
        @Body request: EditarCriancaRequest
    ): Response<CriancaResponse>

    @DELETE("children/{id}")
    suspend fun deletarCrianca(@Path("id") id: String): Response<Unit>

    @POST("children/{id}/gerar-token")
    suspend fun gerarToken(@Path("id") id: String): Response<TokenResponse>

    // Mídia
    @Multipart
    @POST("media/{criancaId}/upload")
    suspend fun uploadMidia(
        @Path("criancaId") criancaId: String,
        @Part file: MultipartBody.Part,
        @Part("descricao") descricao: RequestBody,
        @Part("dataMomento") dataMomento: RequestBody
    ): Response<MidiaResponse>

    @GET("media/{criancaId}")
    suspend fun listarMidia(@Path("criancaId") criancaId: String): Response<ListaMidiaResponse>

    @DELETE("media/{midiaId}")
    suspend fun deletarMidia(@Path("midiaId") midiaId: String): Response<Unit>

    // Admin
    @GET("admin/")
    suspend fun listarAdmins(): Response<ListaAdminsResponse>

    @PUT("admin/{id}")
    suspend fun editarAdmin(
        @Path("id") id: String,
        @Body request: EditarAdminRequest
    ): Response<UsuarioResponse>

    @DELETE("admin/{id}")
    suspend fun deletarAdmin(@Path("id") id: String): Response<Unit>

    // Album (public — no auth needed)
    @GET("album/{token}")
    suspend fun getAlbumByToken(@Path("token") token: String): Response<AlbumResponse>
}
