# Design: Media Viewer Imersivo + Performance + Thumbnails de Vídeo

**Data:** 2026-04-26
**Escopo:** Android app + Backend Node.js
**Status:** Aprovado

---

## Objetivo

Melhorar a visualização de fotos e vídeos no AlbumSeguro para oferecer uma experiência fluida semelhante à galeria nativa do Android, corrigir bugs existentes e reduzir o tempo de carregamento do grid de mídias.

---

## Seção 1 — Arquitetura Geral

### Arquivos impactados

**Android:**
- `ui/screens/MediaViewerScreen.kt` — NOVO
- `ui/screens/ChildDetailScreen.kt` — altera
- `ui/screens/ChildAlbumScreen.kt` — altera
- `ui/navigation/NavGraph.kt` — altera
- `ui/screens/MediaUploadScreen.kt` — altera (thumbnail)
- `network/ApiService.kt` — altera (paginação, thumbnail)
- `repository/MediaRepository.kt` — altera (paginação, thumbnail)
- `viewmodel/MediaViewModel.kt` — altera (estado paginado)
- `data/models/DTOs.kt` — altera (`ListaMidiaResponse`)
- `MainActivity.kt` — altera (Coil singleton)

**Backend:**
- `src/routes/media.js` — altera (paginação, ffmpeg fallback)
- `src/services/storageService.js` — altera (upload thumbnail)
- `package.json` — adiciona `fluent-ffmpeg`, `@ffmpeg-installer/ffmpeg`

### Fluxo de navegação para o viewer

1. Usuário toca em uma mídia no grid (`ChildDetailScreen` ou `ChildAlbumScreen`)
2. App navega para `media_viewer/{criancaId}/{startIndex}` (admin) ou `media_viewer/album/{token}/{startIndex}` (criança)
3. `MediaViewerScreen` abre com animação fade + slide-up
4. Viewer exibe a mídia no índice inicial com swipe horizontal para navegar
5. Fechar: botão X ou gesture back retorna à tela anterior

---

## Seção 2 — MediaViewerScreen

### Layout

- Fundo preto, fullscreen, barra de status escondida via `WindowInsetsController`
- `HorizontalPager` ocupa toda a tela; cada página representa uma `MidiaDTO`
- **Foto:** `AsyncImage` com `TransformableState` para pinch-to-zoom (escala 1x–5x) e drag quando zoom > 1x
- **Vídeo:** `AndroidView(PlayerView)` com ExoPlayer instanciado somente para a página visível

### Overlays

- **Topo:** fundo gradiente escuro, botão X (fechar) à esquerda, índice "3 / 12" à direita
- **Base:** fundo gradiente escuro, descrição da mídia + data formatada
- Ambos os overlays somem após 3s de inatividade via `AnimatedVisibility(visible, fadeIn/fadeOut)`
- Toque simples na tela alterna visibilidade dos overlays

### Ciclo de vida do ExoPlayer no viewer

- `LaunchedEffect(pagerState.currentPage)` pausa e libera o player da página anterior, cria um novo player para a página atual
- `DisposableEffect` libera o player ativo ao sair da composição
- Sem dupla liberação — apenas `onDispose` é responsável pelo `release()` final

### Rota no NavGraph

```
composable("media_viewer/{criancaId}/{startIndex}") { ... }   // admin
composable("media_viewer/album/{token}/{startIndex}") { ... } // criança
```

A lista de mídias é lida do `MediaViewModel` ou `ChildAlbumViewModel` pelo `key` existente — não serializada na URL.

### Dependências

Nenhuma nova dependência: `HorizontalPager` e `TransformableState` já estão em `androidx.compose.foundation` incluída no BOM atual.

---

## Seção 3 — Performance: Cache + Paginação

### Coil — configuração global

Instância `ImageLoader` singleton criada em `MainActivity`:

```kotlin
val imageLoader = ImageLoader.Builder(this)
    .memoryCache { MemoryCache.Builder(this).maxSizePercent(0.25).build() }
    .diskCache {
        DiskCache.Builder()
            .directory(cacheDir.resolve("coil"))
            .maxSizeBytes(100L * 1024 * 1024) // 100MB
            .build()
    }
    .crossfade(300)
    .build()
Coil.setImageLoader(imageLoader)
```

Todas as `AsyncImage` passam a usar `placeholder(ShimmerPainter)` e `error(Icons.Broken)`.

`ShimmerPainter` é um `Painter` customizado simples com `InfiniteTransition` + `LinearGradient` (sem dependência extra).

### Paginação no Android

`MediaViewModel` adquire:
- `_midias: MutableStateFlow<List<MidiaDTO>>` — acumula itens (append)
- `_isLoadingMore: MutableStateFlow<Boolean>`
- `_hasMore: MutableStateFlow<Boolean>`
- `_currentPage: Int` — controle interno

`listarMidia()` com `resetar = true` limpa a lista e volta ao page 0 (usado na mudança de filtro/ordem).

`carregarMaisMidia()` faz append quando `hasMore == true && !isLoadingMore`.

No grid (`LazyVerticalGrid`), um `LaunchedEffect` observa `LazyGridState`:

```kotlin
val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
if (lastVisible >= midias.size - 6 && hasMore && !isLoadingMore) {
    mediaVm.carregarMaisMidia(childId, filtroTipo, filtroOrdem)
}
```

Tamanho de página: **30 itens**.

### Paginação no Backend

```
GET /api/media/:criancaId?tipo=&ordem=&page=0&limit=30
GET /api/album/:token?page=0&limit=30
```

Implementação com `skip(page * limit).limit(limit)`:

```js
const page = parseInt(req.query.page) || 0;
const limit = parseInt(req.query.limit) || 30;
const total = await Media.countDocuments(query);
const midias = await Media.find(query).sort(sort).skip(page * limit).limit(limit);
res.json({ midias, total, hasMore: (page + 1) * limit < total });
```

`ListaMidiaResponse` no Android ganha `total: Int` e `hasMore: Boolean`.

---

## Seção 4 — Thumbnails de Vídeo

### Android — geração local antes do upload

Em `MediaUploadScreen`, ao selecionar vídeo:

```kotlin
val retriever = MediaMetadataRetriever()
retriever.setDataSource(context, videoUri)
val bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
retriever.release()
// salvar bitmap como JPEG em cacheDir
```

O arquivo de thumbnail é enviado como segundo `MultipartBody.Part` com nome `thumbnail`.

Se `getFrameAtTime` retornar `null` ou lançar exceção, o upload segue sem o campo `thumbnail`.

### Backend — fallback com fluent-ffmpeg

```js
const ffmpegInstaller = require('@ffmpeg-installer/ffmpeg');
const ffmpeg = require('fluent-ffmpeg');
ffmpeg.setFfmpegPath(ffmpegInstaller.path);
```

Se o campo `thumbnail` não vier no multipart:
1. Salva o vídeo em arquivo temporário
2. Extrai frame em 00:00:01
3. Faz upload do JPEG para Firebase Storage em `criancas/{id}/thumbs/{midiaId}.jpg`
4. Preenche `novaMidia.thumbnailUrl` com a URL pública
5. Remove o arquivo temporário

Se `ffmpeg` falhar, `thumbnailUrl` fica `null` — o app exibe ícone Play sobre fundo cinza escuro (sem caixa preta opaca).

---

## Seção 5 — Correções de Bugs

| # | Arquivo | Linha(s) | Problema | Correção |
|---|---------|----------|----------|----------|
| 1 | `ChildAlbumScreen.kt` | 73 | Fotos não têm ação no `onClick` | Navegar para `media_viewer` para qualquer tipo de mídia |
| 2 | `VideoPlayerScreen.kt` | 91–93 | ExoPlayer liberado duas vezes (`ON_STOP` + `onDispose`) | Remover `stop()` e `release()` do observer `ON_STOP`; manter apenas `onDispose` |
| 3 | `VideoPlayerScreen.kt` | 90 | `ON_RESUME` força `play()` mesmo se usuário pausou | Salvar `playWhenReady` antes de `ON_PAUSE`, restaurar em `ON_RESUME` |
| 4 | `VideoPlayerScreen.kt` | 133 | Ícone de erro é `ArrowBack` | Substituir por `Icons.Default.ErrorOutline` |
| 5 | `ChildDetailScreen.kt` | 158–183 | Visualizador de foto é `AlertDialog` com `aspectRatio(1f)` fixo — imagens portrait cortadas | Substituído pelo viewer imersivo (`MediaViewerScreen`) |

---

## Fora de Escopo

- Edição de fotos (crop, filtros)
- Download em batch
- Compartilhamento de mídias individuais fora do fluxo já existente
- Autenticação ou mudanças de schema no MongoDB

---

## Critérios de Sucesso

1. Tocar em qualquer mídia (foto ou vídeo) em qualquer tela abre o viewer imersivo
2. Swipe horizontal navega entre todas as mídias da lista atual
3. Pinch-to-zoom funciona em fotos (1x–5x)
4. Vídeos reproduzem dentro do viewer sem crash de dupla liberação
5. Grid carrega os primeiros 30 itens; novos itens são appendados ao rolar
6. Thumbnails de vídeo aparecem no grid (novos uploads)
7. Coil usa cache de disco de 100MB com crossfade de 300ms
