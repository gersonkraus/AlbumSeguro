package com.familiaaco.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
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
private fun PhotoPage(midia: MidiaDTO, onToggleOverlay: () -> Unit) {
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
            .pointerInput(scale) {
                detectTapGestures(
                    onTap = { onToggleOverlay() },
                    onDoubleTap = {
                        if (scale > 1f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 2.5f
                        }
                    }
                )
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
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { midias[it]._id }
        ) { page ->
            val midia = midias[page]
            if (isVideoMidia(midia)) {
                VideoPage(midia = midia, isCurrentPage = page == pagerState.currentPage)
            } else {
                PhotoPage(midia = midia, onToggleOverlay = { overlayVisible = !overlayVisible })
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
