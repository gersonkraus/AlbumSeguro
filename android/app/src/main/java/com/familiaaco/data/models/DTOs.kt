package com.familiaaco.data.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

// Auth
data class LoginRequest(
    val email: String,
    val senha: String
) : Serializable

data class RegistroRequest(
    val nome: String,
    val email: String,
    val senha: String,
    val telefone: String? = null
) : Serializable

data class AuthResponse(
    val message: String,
    val token: String,
    val refreshToken: String? = null,
    val usuario: UsuarioDTO
) : Serializable

data class UsuarioResponse(
    val usuario: UsuarioDTO
) : Serializable

data class UsuarioDTO(
    val _id: String,
    val nome: String,
    val email: String,
    val telefone: String?,
    val role: String,
    val permissoes: List<String>,
    val ativo: Boolean,
    val dataCriacao: String
) : Serializable

// Crianças
data class CriarCriancaRequest(
    val nome: String,
    val dataNascimento: String,
    val descricao: String? = null
) : Serializable

data class EditarCriancaRequest(
    val nome: String?,
    val dataNascimento: String?,
    val descricao: String?
) : Serializable

data class CriancaResponse(
    val message: String?,
    val crianca: CriancaDTO
) : Serializable

data class CriancaDTO(
    val _id: String,
    val nome: String,
    val dataNascimento: String,
    val fotoPerfil: String?,
    val descricao: String?,
    val tokenAcesso: String?,
    val ativo: Boolean,
    val dataCriacao: String
) : Serializable

data class ListaCriancasResponse(
    val criancas: List<CriancaDTO>
) : Serializable

// Mídia
data class MidiaResponse(
    val message: String,
    val midia: MidiaDTO
) : Serializable

data class MidiaDTO(
    val _id: String,
    val criancaId: String,
    val tipo: String,
    val url: String,
    val thumbnailUrl: String?,
    val descricao: String?,
    val dataMomento: String,
    val dataCadastro: String,
    val tamanho: Long,
    val duracao: Long?
) : Serializable

data class ListaMidiaResponse(
    val midias: List<MidiaDTO>,
    val total: Int = 0,
    val hasMore: Boolean = false
) : Serializable

data class EditarMidiaRequest(
    val descricao: String?
) : Serializable

// Token
data class GerarTokenRequest(
    val diasValidade: Int = 30
) : Serializable

data class TokenResponse(
    val message: String,
    val token: String,
    val childAlbumUrl: String?
) : Serializable

// Admin
data class EditarAdminRequest(
    val permissoes: List<String>?,
    val ativo: Boolean?
) : Serializable

data class ListaAdminsResponse(
    val admins: List<UsuarioDTO>
) : Serializable

data class AppConfigDTO(
    val _id: String,
    val childAlbumBaseUrl: String
) : Serializable

data class AppConfigResponse(
    val message: String? = null,
    val config: AppConfigDTO
) : Serializable

data class AtualizarAppConfigRequest(
    val childAlbumBaseUrl: String
) : Serializable

// Foto de perfil
data class FotoPerfilResponse(
    val message: String,
    val fotoPerfil: String
) : Serializable

// Perfil
data class AtualizarPerfilRequest(
    val nome: String,
    val telefone: String? = null
) : Serializable

data class AlterarSenhaRequest(
    val senhaAtual: String,
    val novaSenha: String
) : Serializable

// Logs
data class UsuarioSimples(
    val nome: String,
    val email: String
) : Serializable

data class LogDTO(
    val _id: String,
    val acao: String,
    val status: String,
    val usuarioId: UsuarioSimples?,
    val dataCriacao: String
) : Serializable

data class ListaLogsResponse(
    val logs: List<LogDTO>
) : Serializable

// Album (public child view)
data class AlbumResponse(
    val crianca: CriancaPublicaDTO,
    val midias: List<MidiaDTO>,
    val total: Int = 0,
    val hasMore: Boolean = false
) : Serializable

data class CriancaPublicaDTO(
    val _id: String,
    val nome: String,
    val fotoPerfil: String?,
    val descricao: String?
) : Serializable
