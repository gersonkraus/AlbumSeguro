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

    @PUT("auth/perfil")
    suspend fun atualizarPerfil(@Body request: AtualizarPerfilRequest): Response<UsuarioResponse>

    @PUT("auth/senha")
    suspend fun alterarSenha(@Body request: AlterarSenhaRequest): Response<Unit>

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

    @Multipart
    @POST("children/{id}/foto")
    suspend fun uploadFotoPerfil(
        @Path("id") id: String,
        @Part foto: MultipartBody.Part
    ): Response<FotoPerfilResponse>

    @POST("children/{id}/gerar-token")
    suspend fun gerarToken(
        @Path("id") id: String,
        @Body request: GerarTokenRequest = GerarTokenRequest()
    ): Response<TokenResponse>

    // Mídia
    @Multipart
    @POST("media/{criancaId}/upload")
    suspend fun uploadMidia(
        @Path("criancaId") criancaId: String,
        @Part file: MultipartBody.Part,
        @Part("descricao") descricao: RequestBody,
        @Part("dataMomento") dataMomento: RequestBody,
        @Part thumbnail: MultipartBody.Part? = null
    ): Response<MidiaResponse>

    @GET("media/{criancaId}")
    suspend fun listarMidia(
        @Path("criancaId") criancaId: String,
        @Query("tipo") tipo: String? = null,
        @Query("ordem") ordem: String? = null,
        @Query("page") page: Int = 0,
        @Query("limit") limit: Int = 30
    ): Response<ListaMidiaResponse>

    @DELETE("media/{midiaId}")
    suspend fun deletarMidia(@Path("midiaId") midiaId: String): Response<Unit>

    @PUT("media/{midiaId}")
    suspend fun editarMidia(
        @Path("midiaId") midiaId: String,
        @Body request: EditarMidiaRequest
    ): Response<MidiaResponse>

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

    @GET("admin/config")
    suspend fun getAppConfig(): Response<AppConfigResponse>

    @PUT("admin/config")
    suspend fun atualizarAppConfig(@Body request: AtualizarAppConfigRequest): Response<AppConfigResponse>

    // Logs
    @GET("logs/")
    suspend fun listarLogs(): Response<ListaLogsResponse>

    // Album (public — no auth needed)
    @GET("album/{token}")
    suspend fun getAlbumByToken(
        @Path("token") token: String,
        @Query("page") page: Int = 0,
        @Query("limit") limit: Int = 30
    ): Response<AlbumResponse>
}
