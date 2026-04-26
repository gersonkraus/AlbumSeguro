# Media Viewer Imersivo + Performance + Thumbnails — Plano de Implementação

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Adicionar visualizador fullscreen de mídias estilo galeria Android com swipe/zoom, corrigir bugs existentes, adicionar paginação, cache Coil e thumbnails de vídeo.

**Architecture:** `MediaViewerScreen` dedicada compartilhada entre `ChildDetailScreen` e `ChildAlbumScreen` via `object MediaViewerArgs`. Backend recebe parâmetros `page`/`limit` nas rotas de mídia e gera thumbnail de vídeo com `fluent-ffmpeg` como fallback quando o Android não envia thumbnail.

**Tech Stack:** Kotlin + Jetpack Compose (HorizontalPager, TransformableState, Media3 ExoPlayer), Coil 2.5 com DiskCache, Node.js/Express, fluent-ffmpeg/@ffmpeg-installer, Firebase Storage.

---

## Mapa de arquivos

| Arquivo | Ação |
|---------|------|
| `android/.../screens/VideoPlayerScreen.kt` | Modificar — corrigir bugs lifecycle ExoPlayer |
| `backend/src/routes/media.routes.js` | Modificar — paginação + thumbnail upload com ffmpeg |
| `backend/src/routes/album.routes.js` | Modificar — paginação |
| `backend/package.json` | Modificar — adicionar fluent-ffmpeg |
| `android/.../data/models/DTOs.kt` | Modificar — ListaMidiaResponse com total/hasMore |
| `android/.../network/ApiService.kt` | Modificar — paginação + thumbnail part |
| `android/.../repository/MediaRepository.kt` | Modificar — paginação + thumbnail |
| `android/.../viewmodel/MediaViewModel.kt` | Modificar — estado paginado |
| `android/.../MainActivity.kt` | Modificar — Coil singleton |
| `android/.../screens/MediaViewerScreen.kt` | **CRIAR** — viewer imersivo com HorizontalPager |
| `android/.../ui/navigation/NavGraph.kt` | Modificar — rotas viewer |
| `android/.../screens/ChildDetailScreen.kt` | Modificar — grid paginado + navegar para viewer |
| `android/.../screens/ChildAlbumScreen.kt` | Modificar — corrigir clique foto + viewer |
| `android/.../screens/MediaUploadScreen.kt` | Modificar — gerar thumbnail de vídeo |

---

## Task 1: Corrigir bugs do VideoPlayerScreen

**Files:**
- Modify: `android/app/src/main/java/com/familiaaco/ui/screens/VideoPlayerScreen.kt`

- [ ] **Step 1: Corrigir lifecycle do ExoPlayer e ícone de erro**

Substituir o bloco `DisposableEffect` inteiro (linhas 86–103) e o ícone de erro (linha 133):

```kotlin
// Substituir o DisposableEffect existente por este:
var wasPlayingBeforePause = false
DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_PAUSE -> {
                wasPlayingBeforePause = exoPlayer.isPlaying
                exoPlayer.pause()
            }
            Lifecycle.Event.ON_RESUME -> {
                if (wasPlayingBeforePause) exoPlayer.play()
            }
            else -> {}
        }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
        lifecycleOwner.lifecycle.removeObserver(observer)
        exoPlayer.release()
    }
}
```

No bloco de erro (dentro do `if (hasError)`), substituir o ícone:
```kotlin
// linha 133: de Icons.Default.ArrowBack para:
Icon(
    imageVector = Icons.Default.ErrorOutline,
    contentDescription = null,
    tint = MaterialTheme.colorScheme.error,
    modifier = Modifier.size(48.dp)
)
```

- [ ] **Step 2: Verificar sintaxe**

```bash
node --check /dev/null  # sem verificador Kotlin, checar visualmente se o arquivo está correto
```

Conferir que: (a) `ON_STOP` não aparece mais no observer, (b) `exoPlayer.release()` só existe em `onDispose`, (c) ícone de erro é `ErrorOutline`.

- [ ] **Step 3: Commit**

```bash
git add android/app/src/main/java/com/familiaaco/ui/screens/VideoPlayerScreen.kt
git commit -m "fix(android): corrigir dupla liberação ExoPlayer e ícone de erro em VideoPlayerScreen"
```

---

## Task 2: Backend — paginação na rota de mídia

**Files:**
- Modify: `backend/src/routes/media.routes.js`

- [ ] **Step 1: Adicionar parâmetros page/limit no endpoint GET /:criancaId**

Substituir o handler `router.get('/:criancaId', ...)` inteiro (linhas 82–111):

```javascript
router.get('/:criancaId', authMiddleware, async (req, res) => {
  try {
    const { criancaId } = req.params;
    const { tipo, ordem = 'desc', dataInicio, dataFim, page = 0, limit = 30 } = req.query;
    const pageNum = Math.max(0, parseInt(page) || 0);
    const limitNum = Math.min(100, Math.max(1, parseInt(limit) || 30));

    const crianca = await Child.findById(criancaId);
    if (!crianca) {
      return res.status(404).json({ error: 'Criança não encontrada' });
    }

    const filtro = { criancaId };
    if (tipo && tipo !== 'todos') filtro.tipo = tipo;
    if (dataInicio || dataFim) {
      filtro.dataMomento = {};
      if (dataInicio) filtro.dataMomento.$gte = new Date(dataInicio);
      if (dataFim) filtro.dataMomento.$lte = new Date(dataFim);
    }

    const sortMap = { asc: { dataMomento: 1 }, desc: { dataMomento: -1 }, tamanho: { tamanho: -1 } };
    const sort = sortMap[ordem] || { dataMomento: -1 };

    const total = await Media.countDocuments(filtro);
    const midias = await Media.find(filtro)
      .populate('cadastroPor', 'nome')
      .sort(sort)
      .skip(pageNum * limitNum)
      .limit(limitNum);

    res.json({ midias, total, hasMore: (pageNum + 1) * limitNum < total });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});
```

- [ ] **Step 2: Verificar sintaxe**

```bash
cd /home/gerson/AlbumSeguro/backend && node --check src/routes/media.routes.js
```

Saída esperada: nenhuma saída (sem erros).

- [ ] **Step 3: Testar manualmente com curl**

```bash
cd /home/gerson/AlbumSeguro/backend && npm run dev &
# Obter token de admin fazendo login, depois:
# curl -H "Authorization: Bearer <token>" "http://localhost:3000/api/media/<criancaId>?page=0&limit=2"
# Resposta esperada: { midias: [...], total: N, hasMore: true/false }
```

- [ ] **Step 4: Commit**

```bash
git add backend/src/routes/media.routes.js
git commit -m "feat(backend): adicionar paginação offset/limit na listagem de mídia"
```

---

## Task 3: Backend — paginação na rota pública do álbum

**Files:**
- Modify: `backend/src/routes/album.routes.js`

- [ ] **Step 1: Adicionar parâmetros page/limit no endpoint GET /:token**

Substituir o trecho de busca de mídias (linhas 31–36):

```javascript
// Antes:
const midias = await Media.find({
  criancaId: crianca._id,
  privacidade: { $in: ['apenas_crianca', 'admins_e_crianca'] },
})
  .select('-cadastroPor')
  .sort({ dataMomento: -1 });

// Depois (adicionar após a busca da criança, antes da criação do Log):
const { page = 0, limit = 30 } = req.query;
const pageNum = Math.max(0, parseInt(page) || 0);
const limitNum = Math.min(100, Math.max(1, parseInt(limit) || 30));

const filtroMidia = {
  criancaId: crianca._id,
  privacidade: { $in: ['apenas_crianca', 'admins_e_crianca'] },
};

const total = await Media.countDocuments(filtroMidia);
const midias = await Media.find(filtroMidia)
  .select('-cadastroPor')
  .sort({ dataMomento: -1 })
  .skip(pageNum * limitNum)
  .limit(limitNum);
```

E atualizar a resposta final para incluir `total` e `hasMore`:

```javascript
res.json({
  crianca: {
    _id: crianca._id,
    nome: crianca.nome,
    fotoPerfil: crianca.fotoPerfil,
    descricao: crianca.descricao,
  },
  midias,
  total,
  hasMore: (pageNum + 1) * limitNum < total,
});
```

- [ ] **Step 2: Verificar sintaxe**

```bash
cd /home/gerson/AlbumSeguro/backend && node --check src/routes/album.routes.js
```

Saída esperada: nenhuma saída.

- [ ] **Step 3: Commit**

```bash
git add backend/src/routes/album.routes.js
git commit -m "feat(backend): adicionar paginação na rota pública do álbum"
```

---

## Task 4: Backend — instalar fluent-ffmpeg e adicionar geração de thumbnail no upload

**Files:**
- Modify: `backend/package.json`
- Modify: `backend/src/routes/media.routes.js`

- [ ] **Step 1: Instalar dependências ffmpeg**

```bash
cd /home/gerson/AlbumSeguro/backend && npm install fluent-ffmpeg @ffmpeg-installer/ffmpeg
```

Saída esperada: `added N packages`.

- [ ] **Step 2: Adicionar imports e função gerarThumbnailComFfmpeg em media.routes.js**

Adicionar após as linhas de `require` existentes no topo do arquivo:

```javascript
const ffmpegInstaller = require('@ffmpeg-installer/ffmpeg');
const ffmpeg = require('fluent-ffmpeg');
const os = require('os');
const path = require('path');
const fs = require('fs');
ffmpeg.setFfmpegPath(ffmpegInstaller.path);

async function gerarThumbnailComFfmpeg(buffer, criancaId) {
  const tmpVideo = path.join(os.tmpdir(), `vid_${Date.now()}.mp4`);
  const tmpThumb = path.join(os.tmpdir(), `thumb_${Date.now()}.jpg`);
  try {
    fs.writeFileSync(tmpVideo, buffer);
    await new Promise((resolve, reject) => {
      ffmpeg(tmpVideo)
        .screenshots({
          timestamps: ['00:00:01'],
          filename: path.basename(tmpThumb),
          folder: path.dirname(tmpThumb),
        })
        .on('end', resolve)
        .on('error', reject);
    });
    const thumbBuffer = fs.readFileSync(tmpThumb);
    return await storageService.uploadArquivo(
      { buffer: thumbBuffer, mimetype: 'image/jpeg', originalname: 'thumbnail.jpg' },
      `criancas/${criancaId}/thumbs/${Date.now()}_thumb.jpg`
    );
  } catch (_) {
    return null;
  } finally {
    try { fs.unlinkSync(tmpVideo); } catch (_) {}
    try { fs.unlinkSync(tmpThumb); } catch (_) {}
  }
}
```

- [ ] **Step 3: Alterar o multer de upload.single para upload.fields e atualizar o handler POST**

Substituir a linha do multer:
```javascript
// De:
router.post('/:criancaId/upload', authMiddleware, adminMiddleware, upload.single('file'), async (req, res) => {

// Para:
router.post('/:criancaId/upload', authMiddleware, adminMiddleware, upload.fields([
  { name: 'file', maxCount: 1 },
  { name: 'thumbnail', maxCount: 1 },
]), async (req, res) => {
```

Dentro do handler, substituir as referências a `req.file` e adicionar lógica de thumbnail:

```javascript
  try {
    const { criancaId } = req.params;
    const { descricao, dataMomento } = req.body;

    // upload.fields coloca arquivos em req.files['field'][index]
    const videoFile = req.files['file']?.[0];
    const thumbnailFile = req.files['thumbnail']?.[0];

    if (!videoFile) {
      return res.status(400).json({ error: 'Arquivo não enviado' });
    }

    const crianca = await Child.findById(criancaId);
    if (!crianca) {
      return res.status(404).json({ error: 'Criança não encontrada' });
    }

    const urlDownload = await storageService.uploadArquivo(
      videoFile,
      `criancas/${criancaId}/${Date.now()}_${videoFile.originalname}`
    );

    const mimeType = (videoFile.mimetype || '').toLowerCase();
    const nomeArquivo = (videoFile.originalname || '').toLowerCase();
    const isVideoMime = mimeType.startsWith('video/');
    const isVideoByExt = /\.(mp4|mov|avi|mkv|webm|m4v)$/i.test(nomeArquivo);
    const isVideo = isVideoMime || isVideoByExt;

    let thumbnailUrl = null;
    if (isVideo) {
      if (thumbnailFile) {
        thumbnailUrl = await storageService.uploadArquivo(
          thumbnailFile,
          `criancas/${criancaId}/thumbs/${Date.now()}_thumb.jpg`
        );
      } else {
        thumbnailUrl = await gerarThumbnailComFfmpeg(videoFile.buffer, criancaId);
      }
    }

    const novaMidia = new Media({
      criancaId,
      tipo: isVideo ? 'video' : 'foto',
      url: urlDownload,
      thumbnailUrl,
      descricao,
      dataMomento: dataMomento || new Date(),
      cadastroPor: req.user._id,
      tamanho: videoFile.size,
    });

    await novaMidia.save();

    if (!crianca.historicoPessoas.includes(req.user._id)) {
      crianca.historicoPessoas.push(req.user._id);
      await crianca.save();
    }

    await Log.create({
      usuarioId: req.user._id,
      acao: 'UPLOAD_MIDIA',
      recursoId: novaMidia._id,
      status: 'sucesso',
      ipAddress: req.ip,
    });

    res.status(201).json({ message: 'Arquivo enviado com sucesso', midia: novaMidia });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
```

- [ ] **Step 4: Verificar sintaxe**

```bash
cd /home/gerson/AlbumSeguro/backend && node --check src/routes/media.routes.js
```

- [ ] **Step 5: Commit**

```bash
git add backend/src/routes/media.routes.js backend/package.json backend/package-lock.json
git commit -m "feat(backend): geração de thumbnail de vídeo com fluent-ffmpeg no upload"
```

---

## Task 5: Android — atualizar DTOs e ApiService

**Files:**
- Modify: `android/app/src/main/java/com/familiaaco/data/models/DTOs.kt`
- Modify: `android/app/src/main/java/com/familiaaco/network/ApiService.kt`

- [ ] **Step 1: Adicionar campos total e hasMore em ListaMidiaResponse (DTOs.kt)**

Localizar `data class ListaMidiaResponse` e substituir:

```kotlin
data class ListaMidiaResponse(
    val midias: List<MidiaDTO>,
    val total: Int = 0,
    val hasMore: Boolean = false
) : Serializable
```

Também adicionar `hasMore` e `total` em `AlbumResponse`:

```kotlin
data class AlbumResponse(
    val crianca: CriancaPublicaDTO,
    val midias: List<MidiaDTO>,
    val total: Int = 0,
    val hasMore: Boolean = false
) : Serializable
```

- [ ] **Step 2: Atualizar ApiService — paginação em listarMidia e thumbnail em uploadMidia**

Substituir `uploadMidia` em `ApiService.kt`:

```kotlin
@Multipart
@POST("media/{criancaId}/upload")
suspend fun uploadMidia(
    @Path("criancaId") criancaId: String,
    @Part file: MultipartBody.Part,
    @Part("descricao") descricao: RequestBody,
    @Part("dataMomento") dataMomento: RequestBody,
    @Part thumbnail: MultipartBody.Part? = null
): Response<MidiaResponse>
```

Substituir `listarMidia` em `ApiService.kt`:

```kotlin
@GET("media/{criancaId}")
suspend fun listarMidia(
    @Path("criancaId") criancaId: String,
    @Query("tipo") tipo: String? = null,
    @Query("ordem") ordem: String? = null,
    @Query("page") page: Int = 0,
    @Query("limit") limit: Int = 30
): Response<ListaMidiaResponse>
```

Adicionar versão paginada do álbum público — substituir `getAlbumByToken`:

```kotlin
@GET("album/{token}")
suspend fun getAlbumByToken(
    @Path("token") token: String,
    @Query("page") page: Int = 0,
    @Query("limit") limit: Int = 30
): Response<AlbumResponse>
```

- [ ] **Step 3: Commit**

```bash
git add android/app/src/main/java/com/familiaaco/data/models/DTOs.kt \
        android/app/src/main/java/com/familiaaco/network/ApiService.kt
git commit -m "feat(android): adicionar paginação e thumbnail nos DTOs e ApiService"
```

---

## Task 6: Android — atualizar MediaRepository

**Files:**
- Modify: `android/app/src/main/java/com/familiaaco/repository/MediaRepository.kt`

- [ ] **Step 1: Atualizar uploadMidia para enviar thumbnail opcional**

Substituir a função `uploadMidia` em `MediaRepository.kt`:

```kotlin
suspend fun uploadMidia(
    criancaId: String,
    file: File,
    descricao: String,
    dataMomento: String,
    thumbnailFile: File? = null
): Result<MidiaDTO> {
    return try {
        val ext = file.extension.lowercase()
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "application/octet-stream"
        val filePart = MultipartBody.Part.createFormData("file", file.name, file.asRequestBody(mime.toMediaType()))
        val descricaoPart = descricao.toRequestBody("text/plain".toMediaType())
        val dataPart = dataMomento.toRequestBody("text/plain".toMediaType())
        val thumbnailPart = thumbnailFile?.let {
            MultipartBody.Part.createFormData("thumbnail", it.name, it.asRequestBody("image/jpeg".toMediaType()))
        }
        val response = apiService.uploadMidia(criancaId, filePart, descricaoPart, dataPart, thumbnailPart)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!.midia)
        } else {
            Result.failure(Exception("Erro ${response.code()}: Falha ao fazer upload"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

- [ ] **Step 2: Atualizar listarMidia para aceitar page/limit e retornar ListaMidiaResponse**

Substituir a função `listarMidia`:

```kotlin
suspend fun listarMidia(
    criancaId: String,
    tipo: String? = null,
    ordem: String? = null,
    page: Int = 0,
    limit: Int = 30
): Result<ListaMidiaResponse> {
    return try {
        val response = apiService.listarMidia(criancaId, tipo, ordem, page, limit)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Erro ${response.code()}: Falha ao listar mídia"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

- [ ] **Step 3: Atualizar getAlbumByToken para aceitar page/limit**

Substituir a função `getAlbumByToken`:

```kotlin
suspend fun getAlbumByToken(token: String, page: Int = 0, limit: Int = 30): Result<AlbumResponse> {
    return try {
        val response = apiService.getAlbumByToken(token, page, limit)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Erro ${response.code()}: Álbum não encontrado"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add android/app/src/main/java/com/familiaaco/repository/MediaRepository.kt
git commit -m "feat(android): paginação e thumbnail opcional no MediaRepository"
```

---

## Task 7: Android — refatorar MediaViewModel com estado paginado

**Files:**
- Modify: `android/app/src/main/java/com/familiaaco/viewmodel/MediaViewModel.kt`

- [ ] **Step 1: Reescrever MediaViewModel.kt com suporte a paginação**

Substituir o arquivo inteiro:

```kotlin
package com.familiaaco.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familiaaco.data.models.MidiaDTO
import com.familiaaco.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class MediaViewModel(context: Context) : ViewModel() {
    private val mediaRepository = MediaRepository(context)

    private val _mediaState = MutableStateFlow<MediaState>(MediaState.Idle)
    val mediaState: StateFlow<MediaState> = _mediaState

    private val _hasMore = MutableStateFlow(false)
    val hasMore: StateFlow<Boolean> = _hasMore

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore

    private var currentPage = 0
    private var accumulatedMidias: List<MidiaDTO> = emptyList()
    private val pageSize = 30

    fun listarMidia(criancaId: String, tipo: String? = null, ordem: String? = null) {
        viewModelScope.launch {
            currentPage = 0
            accumulatedMidias = emptyList()
            _mediaState.value = MediaState.Loading
            mediaRepository.listarMidia(criancaId, tipo, ordem, 0, pageSize)
                .onSuccess { response ->
                    accumulatedMidias = response.midias
                    _hasMore.value = response.hasMore
                    _mediaState.value = MediaState.Success(accumulatedMidias)
                }
                .onFailure { _mediaState.value = MediaState.Error(it.message ?: "Erro") }
        }
    }

    fun carregarMaisMidia(criancaId: String, tipo: String? = null, ordem: String? = null) {
        if (_isLoadingMore.value || !_hasMore.value) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            currentPage++
            mediaRepository.listarMidia(criancaId, tipo, ordem, currentPage, pageSize)
                .onSuccess { response ->
                    accumulatedMidias = accumulatedMidias + response.midias
                    _hasMore.value = response.hasMore
                    _mediaState.value = MediaState.Success(accumulatedMidias)
                }
                .onFailure { currentPage-- }
            _isLoadingMore.value = false
        }
    }

    fun uploadMidia(criancaId: String, file: File, descricao: String, dataMomento: String, thumbnail: File? = null) {
        uploadMidias(criancaId, listOf(file), listOf(thumbnail), descricao, dataMomento)
    }

    fun uploadMidias(
        criancaId: String,
        files: List<File>,
        thumbnails: List<File?> = emptyList(),
        descricao: String,
        dataMomento: String
    ) {
        viewModelScope.launch {
            if (files.isEmpty()) {
                _mediaState.value = MediaState.Error("Nenhum arquivo selecionado")
                return@launch
            }
            val total = files.size
            var successCount = 0
            val failedFiles = mutableListOf<String>()
            files.forEachIndexed { index, file ->
                _mediaState.value = MediaState.Uploading(index + 1, total, file.name)
                val thumbnail = thumbnails.getOrNull(index)
                val result = mediaRepository.uploadMidia(criancaId, file, descricao, dataMomento, thumbnail)
                if (result.isFailure) failedFiles.add(file.name) else successCount++
            }
            _mediaState.value = MediaState.UploadSummary(
                successCount = successCount,
                failCount = failedFiles.size,
                total = total,
                failedFileNames = failedFiles
            )
        }
    }

    fun editarMidia(midiaId: String, descricao: String, criancaId: String) {
        viewModelScope.launch {
            _mediaState.value = MediaState.Loading
            mediaRepository.editarMidia(midiaId, descricao)
                .onSuccess { listarMidia(criancaId) }
                .onFailure { _mediaState.value = MediaState.Error(it.message ?: "Erro ao editar") }
        }
    }

    fun deletarMidia(midiaId: String, criancaId: String) {
        viewModelScope.launch {
            _mediaState.value = MediaState.Loading
            mediaRepository.deletarMidia(midiaId)
                .onSuccess { listarMidia(criancaId) }
                .onFailure { _mediaState.value = MediaState.Error(it.message ?: "Erro ao deletar") }
        }
    }

    sealed class MediaState {
        object Idle : MediaState()
        object Loading : MediaState()
        data class Uploading(val current: Int, val total: Int, val fileName: String) : MediaState()
        data class UploadSummary(
            val successCount: Int,
            val failCount: Int,
            val total: Int,
            val failedFileNames: List<String>
        ) : MediaState()
        data class Success(val midias: List<MidiaDTO>) : MediaState()
        data class Error(val message: String) : MediaState()
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add android/app/src/main/java/com/familiaaco/viewmodel/MediaViewModel.kt
git commit -m "feat(android): refatorar MediaViewModel com suporte a paginação"
```

---

## Task 8: Android — atualizar ChildAlbumViewModel com paginação

**Files:**
- Modify: `android/app/src/main/java/com/familiaaco/viewmodel/ChildAlbumViewModel.kt`

- [ ] **Step 1: Adicionar suporte a paginação no ChildAlbumViewModel**

Substituir o arquivo inteiro:

```kotlin
package com.familiaaco.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familiaaco.data.models.MidiaDTO
import com.familiaaco.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChildAlbumViewModel(context: Context) : ViewModel() {
    private val mediaRepository = MediaRepository(context)

    private val _albumState = MutableStateFlow<AlbumState>(AlbumState.Idle)
    val albumState: StateFlow<AlbumState> = _albumState

    private val _hasMore = MutableStateFlow(false)
    val hasMore: StateFlow<Boolean> = _hasMore

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore

    private var currentPage = 0
    private var accumulatedMidias: List<MidiaDTO> = emptyList()
    private var currentToken: String = ""
    private val pageSize = 30

    fun carregarAlbum(token: String) {
        currentToken = token
        viewModelScope.launch {
            currentPage = 0
            accumulatedMidias = emptyList()
            _albumState.value = AlbumState.Loading
            mediaRepository.getAlbumByToken(token, 0, pageSize)
                .onSuccess { response ->
                    accumulatedMidias = response.midias
                    _hasMore.value = response.hasMore
                    _albumState.value = AlbumState.Success(token, accumulatedMidias, response.crianca.nome)
                }
                .onFailure { _albumState.value = AlbumState.Error(it.message ?: "Erro ao carregar álbum") }
        }
    }

    fun carregarMais() {
        if (_isLoadingMore.value || !_hasMore.value || currentToken.isEmpty()) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            currentPage++
            mediaRepository.getAlbumByToken(currentToken, currentPage, pageSize)
                .onSuccess { response ->
                    accumulatedMidias = accumulatedMidias + response.midias
                    _hasMore.value = response.hasMore
                    val current = _albumState.value
                    if (current is AlbumState.Success) {
                        _albumState.value = current.copy(midias = accumulatedMidias)
                    }
                }
                .onFailure { currentPage-- }
            _isLoadingMore.value = false
        }
    }

    sealed class AlbumState {
        object Idle : AlbumState()
        object Loading : AlbumState()
        data class Success(val token: String, val midias: List<MidiaDTO>, val nomeAlbum: String = "") : AlbumState()
        data class Error(val message: String) : AlbumState()
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add android/app/src/main/java/com/familiaaco/viewmodel/ChildAlbumViewModel.kt
git commit -m "feat(android): paginação em ChildAlbumViewModel"
```

---

## Task 9: Android — configurar Coil com cache em MainActivity

**Files:**
- Modify: `android/app/src/main/java/com/familiaaco/MainActivity.kt`

- [ ] **Step 1: Adicionar import e configurar Coil singleton**

Substituir o arquivo inteiro:

```kotlin
package com.familiaaco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.familiaaco.ui.navigation.NavGraph
import com.familiaaco.ui.theme.FamiliaAcolhedoraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .memoryCache {
                    MemoryCache.Builder(this)
                        .maxSizePercent(0.25)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(cacheDir.resolve("coil"))
                        .maxSizeBytes(100L * 1024 * 1024)
                        .build()
                }
                .crossfade(300)
                .build()
        )

        setContent {
            FamiliaAcolhedoraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController)
                }
            }
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add android/app/src/main/java/com/familiaaco/MainActivity.kt
git commit -m "feat(android): configurar Coil com MemoryCache 25% e DiskCache 100MB"
```

---

## Task 10: Android — criar MediaViewerScreen

**Files:**
- Create: `android/app/src/main/java/com/familiaaco/ui/screens/MediaViewerScreen.kt`

- [ ] **Step 1: Criar objeto singleton para passar dados ao viewer**

No início do novo arquivo, antes do `@Composable`:

```kotlin
package com.familiaaco.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.familiaaco.data.models.MidiaDTO
import com.familiaaco.ui.utils.formatarDataParaExibicao
import kotlinx.coroutines.delay
import java.util.Locale

object MediaViewerArgs {
    var midias: List<MidiaDTO> = emptyList()
    var startIndex: Int = 0
}

private fun isVideoMidia(midia: MidiaDTO): Boolean {
    if (midia.tipo.equals("video", ignoreCase = true)) return true
    val url = midia.url.lowercase(Locale.ROOT)
    return url.contains(".mp4") || url.contains(".mov") || url.contains(".avi") ||
            url.contains(".mkv") || url.contains(".webm") || url.contains(".m4v")
}
```

- [ ] **Step 2: Implementar PhotoPage composable**

Adicionar após as funções privadas acima:

```kotlin
@Composable
private fun PhotoPage(midia: MidiaDTO) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        if (scale > 1f) offset += panChange
    }
    LaunchedEffect(scale) {
        if (scale <= 1f) offset = Offset.Zero
    }
    AsyncImage(
        model = midia.url,
        contentDescription = midia.descricao,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
            .transformable(state = transformState)
    )
}
```

- [ ] **Step 3: Implementar VideoPage composable**

```kotlin
@Composable
private fun VideoPage(midia: MidiaDTO, isCurrentPage: Boolean) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(midia.url)))
            prepare()
            playWhenReady = false
        }
    }
    LaunchedEffect(isCurrentPage) {
        if (isCurrentPage) exoPlayer.play() else exoPlayer.pause()
    }
    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                keepScreenOn = true
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
```

- [ ] **Step 4: Implementar MediaViewerScreen composable principal**

```kotlin
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaViewerScreen(navController: NavController) {
    val midias = MediaViewerArgs.midias
    val startIndex = MediaViewerArgs.startIndex

    if (midias.isEmpty()) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    var overlayVisible by remember { mutableStateOf(true) }
    val pagerState = rememberPagerState(initialPage = startIndex) { midias.size }

    LaunchedEffect(overlayVisible, pagerState.currentPage) {
        if (overlayVisible) {
            delay(3000)
            overlayVisible = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { overlayVisible = !overlayVisible }
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { midias[it]._id },
            userScrollEnabled = true
        ) { page ->
            val midia = midias[page]
            val isCurrentPage = page == pagerState.currentPage
            if (isVideoMidia(midia)) {
                VideoPage(midia = midia, isCurrentPage = isCurrentPage)
            } else {
                PhotoPage(midia = midia)
            }
        }

        // Overlay topo
        AnimatedVisibility(
            visible = overlayVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                        )
                    )
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                }
                Text(
                    text = "${pagerState.currentPage + 1} / ${midias.size}",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        // Overlay base
        val currentMidia = midias[pagerState.currentPage]
        AnimatedVisibility(
            visible = overlayVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column {
                    if (!currentMidia.descricao.isNullOrBlank()) {
                        Text(
                            text = currentMidia.descricao,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    Text(
                        text = formatarDataParaExibicao(currentMidia.dataMomento),
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add android/app/src/main/java/com/familiaaco/ui/screens/MediaViewerScreen.kt
git commit -m "feat(android): criar MediaViewerScreen imersiva com HorizontalPager e pinch-to-zoom"
```

---

## Task 11: Android — atualizar NavGraph com rota do viewer

**Files:**
- Modify: `android/app/src/main/java/com/familiaaco/ui/navigation/NavGraph.kt`

- [ ] **Step 1: Adicionar rota media_viewer**

Adicionar dentro do `NavHost`, após a rota `video_player`:

```kotlin
composable("media_viewer") {
    MediaViewerScreen(navController)
}
```

- [ ] **Step 2: Commit**

```bash
git add android/app/src/main/java/com/familiaaco/ui/navigation/NavGraph.kt
git commit -m "feat(android): adicionar rota media_viewer no NavGraph"
```

---

## Task 12: Android — atualizar ChildDetailScreen (grid paginado + viewer)

**Files:**
- Modify: `android/app/src/main/java/com/familiaaco/ui/screens/ChildDetailScreen.kt`

- [ ] **Step 1: Adicionar import de LazyGridState e dos novos estados do ViewModel**

Adicionar imports necessários no topo (se não existirem):

```kotlin
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.snapshotFlow
import com.familiaaco.ui.screens.MediaViewerArgs
```

- [ ] **Step 2: Adicionar coleção dos novos estados do ViewModel no corpo da função**

Após as linhas que coletam `mediaState` (linha ~109), adicionar:

```kotlin
val hasMore by mediaVm.hasMore.collectAsState()
val isLoadingMore by mediaVm.isLoadingMore.collectAsState()
val gridState = rememberLazyGridState()
```

- [ ] **Step 3: Substituir o LaunchedEffect de carregamento de mídia para resetar ao mudar filtro**

A linha existente (linha ~153):
```kotlin
LaunchedEffect(childId, filtroTipo, filtroOrdem) {
    mediaVm.listarMidia(childId, filtroTipo.takeIf { it != "todos" }, filtroOrdem)
}
```
Permanece como está — o ViewModel já reseta a lista ao chamar `listarMidia`.

- [ ] **Step 4: Adicionar LaunchedEffect para paginação por scroll**

Após os LaunchedEffects existentes, adicionar:

```kotlin
LaunchedEffect(gridState) {
    snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
        .collect { lastIndex ->
            val total = gridState.layoutInfo.totalItemsCount
            if (lastIndex >= total - 6 && hasMore && !isLoadingMore) {
                mediaVm.carregarMaisMidia(
                    childId,
                    filtroTipo.takeIf { it != "todos" },
                    filtroOrdem
                )
            }
        }
}
```

- [ ] **Step 5: Substituir o visualizador de mídia (AlertDialog) pela navegação para MediaViewerScreen**

Remover o bloco inteiro do `AlertDialog` de `midiaVisualizada` (linhas 158–183 do arquivo original).

Remover também a variável `var midiaVisualizada by remember { mutableStateOf<...?>(null) }`.

No `onClick` das células do grid (dentro do `LazyVerticalGrid`), substituir:

```kotlin
// De:
onClick = {
    if (isVideoMidia(midia)) {
        val encoded = android.util.Base64.encodeToString(...)
        navController.navigate("video_player/$encoded")
    } else {
        midiaVisualizada = midia
    }
},

// Para:
onClick = {
    val midias = (mediaState as? MediaViewModel.MediaState.Success)?.midias ?: emptyList()
    val idx = midias.indexOf(midia).coerceAtLeast(0)
    MediaViewerArgs.midias = midias
    MediaViewerArgs.startIndex = idx
    navController.navigate("media_viewer")
},
```

- [ ] **Step 6: Adicionar state no LazyVerticalGrid**

Atualizar o `LazyVerticalGrid` para usar `gridState`:

```kotlin
LazyVerticalGrid(
    columns = GridCells.Fixed(3),
    state = gridState,
    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
    horizontalArrangement = Arrangement.spacedBy(6.dp),
    verticalArrangement = Arrangement.spacedBy(6.dp),
    contentPadding = PaddingValues(vertical = 8.dp)
) {
    items(midias) { midia ->
        // ... código existente das células, com onClick substituído acima
    }
    if (isLoadingMore) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        }
    }
}
```

- [ ] **Step 7: Commit**

```bash
git add android/app/src/main/java/com/familiaaco/ui/screens/ChildDetailScreen.kt
git commit -m "feat(android): grid paginado e navegação para MediaViewerScreen em ChildDetailScreen"
```

---

## Task 13: Android — corrigir ChildAlbumScreen (clique em fotos + viewer + paginação)

**Files:**
- Modify: `android/app/src/main/java/com/familiaaco/ui/screens/ChildAlbumScreen.kt`

- [ ] **Step 1: Adicionar imports necessários**

```kotlin
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.runtime.snapshotFlow
import com.familiaaco.ui.screens.MediaViewerArgs
```

- [ ] **Step 2: Adicionar coleção dos novos estados e gridState**

Logo após `val albumState by viewModel.albumState.collectAsState()`, adicionar:

```kotlin
val hasMore by viewModel.hasMore.collectAsState()
val isLoadingMore by viewModel.isLoadingMore.collectAsState()
val gridState = rememberLazyGridState()
```

- [ ] **Step 3: Adicionar LaunchedEffect de paginação por scroll**

Após o `LaunchedEffect(albumState)` existente:

```kotlin
LaunchedEffect(gridState) {
    snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
        .collect { lastIndex ->
            val total = gridState.layoutInfo.totalItemsCount
            if (lastIndex >= total - 6 && hasMore && !isLoadingMore) {
                viewModel.carregarMais()
            }
        }
}
```

- [ ] **Step 4: Substituir o LazyVerticalGrid para usar gridState e corrigir clique em fotos**

Substituir o bloco `LazyVerticalGrid` inteiro dentro do estado `Success`:

```kotlin
val midias = (albumState as ChildAlbumViewModel.AlbumState.Success).midias
if (midias.isEmpty()) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Nenhuma foto ou vídeo ainda.", style = MaterialTheme.typography.bodyLarge)
    }
} else {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        modifier = Modifier.fillMaxSize().padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(midias) { midia ->
            MidiaGridItem(midia = midia, onClick = {
                val idx = midias.indexOf(midia).coerceAtLeast(0)
                MediaViewerArgs.midias = midias
                MediaViewerArgs.startIndex = idx
                navController.navigate("media_viewer")
            })
        }
        if (isLoadingMore) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add android/app/src/main/java/com/familiaaco/ui/screens/ChildAlbumScreen.kt
git commit -m "fix(android): corrigir clique em fotos e adicionar paginação em ChildAlbumScreen"
```

---

## Task 14: Android — gerar thumbnail de vídeo em MediaUploadScreen

**Files:**
- Modify: `android/app/src/main/java/com/familiaaco/ui/screens/MediaUploadScreen.kt`

- [ ] **Step 1: Adicionar imports**

Adicionar no topo do arquivo:

```kotlin
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.webkit.MimeTypeMap
```

- [ ] **Step 2: Adicionar função de geração de thumbnail**

Adicionar como função privada no arquivo (fora do composable):

```kotlin
private fun gerarThumbnailParaUri(context: android.content.Context, uri: android.net.Uri): java.io.File? {
    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, uri)
        val bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        retriever.release()
        bitmap?.let {
            val thumbFile = java.io.File(context.cacheDir, "thumb_${System.currentTimeMillis()}.jpg")
            thumbFile.outputStream().use { out ->
                it.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }
            thumbFile
        }
    } catch (_: Exception) {
        null
    }
}

private fun isVideoUri(context: android.content.Context, uri: android.net.Uri): Boolean {
    val mime = context.contentResolver.getType(uri) ?: return false
    return mime.startsWith("video/")
}
```

- [ ] **Step 3: Atualizar o onClick do botão de envio para gerar thumbnails**

Localizar o bloco `onClick` do botão de envio (onde chama `viewModel.uploadMidias`):

```kotlin
// De:
val files = selectedFileUris.mapNotNull { uriToFile(context, it) }
if (files.isNotEmpty()) {
    viewModel.uploadMidias(criancaId, files, descricao, dataMomentoApi)
}

// Para:
val files = selectedFileUris.mapNotNull { uriToFile(context, it) }
val thumbnails = selectedFileUris.map { uri ->
    if (isVideoUri(context, uri)) gerarThumbnailParaUri(context, uri) else null
}
if (files.isNotEmpty()) {
    viewModel.uploadMidias(criancaId, files, thumbnails, descricao, dataMomentoApi)
}
```

- [ ] **Step 4: Commit**

```bash
git add android/app/src/main/java/com/familiaaco/ui/screens/MediaUploadScreen.kt
git commit -m "feat(android): gerar thumbnail de vídeo antes do upload com MediaMetadataRetriever"
```

---

## Task 15: Copiar arquivos Android para Windows

**Files:** todos os `.kt` modificados/criados neste plano.

- [ ] **Step 1: Copiar todos os arquivos modificados para C:\Projetos\AlbumSeguro**

Executar no PowerShell (Windows):

```powershell
$src = "\\wsl.localhost\Ubuntu\home\gerson\AlbumSeguro\android\app\src\main\java\com\familiaaco"
$dst = "C:\Projetos\AlbumSeguro\android\app\src\main\java\com\familiaaco"

$files = @(
    "MainActivity.kt",
    "ui\screens\VideoPlayerScreen.kt",
    "ui\screens\MediaViewerScreen.kt",
    "ui\screens\ChildDetailScreen.kt",
    "ui\screens\ChildAlbumScreen.kt",
    "ui\screens\MediaUploadScreen.kt",
    "ui\navigation\NavGraph.kt",
    "network\ApiService.kt",
    "repository\MediaRepository.kt",
    "viewmodel\MediaViewModel.kt",
    "viewmodel\ChildAlbumViewModel.kt",
    "data\models\DTOs.kt"
)

foreach ($f in $files) {
    $srcPath = Join-Path $src $f
    $dstPath = Join-Path $dst $f
    Copy-Item $srcPath -Destination $dstPath -Force
    Write-Host "Copiado: $f"
}
```

- [ ] **Step 2: Build no Android Studio**

Abrir `C:\Projetos\AlbumSeguro\android` no Android Studio e executar Build → Make Project. Verificar que não há erros de compilação.

- [ ] **Step 3: Commit final após build bem-sucedido**

```bash
git add -A
git commit -m "chore: sincronizar arquivos Android após build Windows confirmado"
```

---

## Verificação de cobertura do spec

| Requisito do spec | Task que implementa |
|---|---|
| MediaViewerScreen com HorizontalPager | Task 10 |
| Pinch-to-zoom em fotos | Task 10 (PhotoPage) |
| ExoPlayer por página no viewer | Task 10 (VideoPage) |
| Overlays fade com timer 3s | Task 10 (MediaViewerScreen) |
| Rota no NavGraph | Task 11 |
| Paginação backend media | Task 2 |
| Paginação backend album | Task 3 |
| fluent-ffmpeg thumbnail fallback | Task 4 |
| DTOs com total/hasMore | Task 5 |
| ApiService paginação + thumbnail | Task 5 |
| MediaRepository paginação + thumbnail | Task 6 |
| MediaViewModel paginado | Task 7 |
| ChildAlbumViewModel paginado | Task 8 |
| Coil DiskCache 100MB | Task 9 |
| ChildDetailScreen viewer + grid paginado | Task 12 |
| ChildAlbumScreen fix foto + viewer + paginação | Task 13 |
| MediaUploadScreen thumbnail geração | Task 14 |
| Bug ExoPlayer dupla liberação | Task 1 |
| Bug ON_RESUME força play | Task 1 |
| Bug ícone de erro | Task 1 |
| Bug foto sem ação ChildAlbumScreen | Task 13 |
| Cópia para Windows | Task 15 |
