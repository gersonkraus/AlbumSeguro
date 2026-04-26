package com.familiaaco.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.awaitFirstDown
import androidx.compose.ui.input.pointer.pointerInput
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

@Composable
private fun PhotoPage(
    midia: MidiaDTO,
    scale: Float,
    offset: Offset,
    onScaleChange: (Float) -> Unit,
    onOffsetChange: (Offset) -> Unit
) {
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
            // Gesture handler customizado: só consome eventos quando multi-touch
            // ou single-touch com zoom ativo — nunca bloqueia o swipe do pager
            .pointerInput(scale, offset) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    var currentScale = scale
                    var currentOffset = offset
                    var multiTouchStarted = false
                    do {
                        val event = awaitPointerEvent()
                        val touchCount = event.changes.count { it.pressed }
                        if (touchCount >= 2) multiTouchStarted = true
                        if (multiTouchStarted || (touchCount == 1 && currentScale > 1f)) {
                            val zoomChange = if (touchCount >= 2) event.calculateZoom() else 1f
                            val panChange = event.calculatePan()
                            currentScale = (currentScale * zoomChange).coerceIn(1f, 5f)
                            currentOffset = if (currentScale > 1f) currentOffset + panChange
                                           else Offset.Zero
                            onScaleChange(currentScale)
                            onOffsetChange(currentOffset)
                            event.changes.forEach { it.consume() }
                        }
                        // single-touch com scale==1: não consome → pager recebe o swipe
                    } while (event.changes.any { it.pressed })
                }
            }
    )
}

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

    // Estado de zoom compartilhado — reseta ao trocar de página
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    LaunchedEffect(pagerState.currentPage) {
        scale = 1f
        offset = Offset.Zero
    }

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
            // Tap simples = alternar overlay | Tap duplo = zoom 1x↔2.5x
            .pointerInput(pagerState.currentPage, scale) {
                detectTapGestures(
                    onTap = { overlayVisible = !overlayVisible },
                    onDoubleTap = {
                        if (!isVideoMidia(midias[pagerState.currentPage])) {
                            if (scale > 1f) { scale = 1f; offset = Offset.Zero }
                            else scale = 2.5f
                        }
                    }
                )
            }
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { midias[it]._id },
            // Desabilita swipe do pager quando foto está com zoom ativo
            userScrollEnabled = scale <= 1f
        ) { page ->
            val midia = midias[page]
            if (isVideoMidia(midia)) {
                VideoPage(midia = midia, isCurrentPage = page == pagerState.currentPage)
            } else {
                PhotoPage(
                    midia = midia,
                    scale = scale,
                    offset = offset,
                    onScaleChange = { if (page == pagerState.currentPage) scale = it },
                    onOffsetChange = { if (page == pagerState.currentPage) offset = it }
                )
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
                    .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)))
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
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column {
                    if (!currentMidia.descricao.isNullOrBlank()) {
                        Text(text = currentMidia.descricao, color = Color.White, style = MaterialTheme.typography.bodyMedium)
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
