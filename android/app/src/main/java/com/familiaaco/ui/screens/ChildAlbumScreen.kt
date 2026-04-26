package com.familiaaco.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.familiaaco.data.local.TokenManager
import com.familiaaco.data.models.MidiaDTO
import com.familiaaco.ui.theme.PrimaryColor
import com.familiaaco.viewmodel.ChildAlbumViewModel
import android.util.Base64
import java.util.Locale

private fun isVideoMidia(midia: MidiaDTO): Boolean {
    if (midia.tipo.equals("video", ignoreCase = true)) return true
    val url = midia.url.lowercase(Locale.ROOT)
    return url.contains(".mp4") || url.contains(".mov") || url.contains(".avi") ||
            url.contains(".mkv") || url.contains(".webm") || url.contains(".m4v")
}

@Composable
fun ChildAlbumScreen(navController: NavController, token: String) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val viewModel: ChildAlbumViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = ChildAlbumViewModel(context) as T
    })
    val albumState by viewModel.albumState.collectAsState()

    LaunchedEffect(token) { viewModel.carregarAlbum(token) }
    LaunchedEffect(albumState) {
        val success = albumState as? ChildAlbumViewModel.AlbumState.Success
        if (success != null) {
            tokenManager.saveChildToken(success.token)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) }
            Text("Meu Álbum", style = MaterialTheme.typography.headlineSmall, color = PrimaryColor, modifier = Modifier.weight(1f))
        }
        when (albumState) {
            is ChildAlbumViewModel.AlbumState.Loading ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is ChildAlbumViewModel.AlbumState.Success -> {
                val midias = (albumState as ChildAlbumViewModel.AlbumState.Success).midias
                if (midias.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Nenhuma foto ou vídeo ainda.", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    LazyVerticalGrid(columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(midias) { midia ->
                            MidiaGridItem(midia = midia, onClick = {
                                if (isVideoMidia(midia)) {
                                    val encoded = Base64.encodeToString(midia.url.toByteArray(Charsets.UTF_8), Base64.URL_SAFE or Base64.NO_WRAP)
                                    navController.navigate("video_player/$encoded")
                                }
                            })
                        }
                    }
                }
            }
            is ChildAlbumViewModel.AlbumState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text((albumState as ChildAlbumViewModel.AlbumState.Error).message)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.carregarAlbum(token) }) { Text("Tentar Novamente") }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = {
                            tokenManager.clearChildToken()
                            navController.navigate("child_token") {
                                popUpTo("child_album/{token}") { inclusive = true }
                            }
                        }) {
                            Text("Usar outro acesso")
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun MidiaGridItem(midia: MidiaDTO, onClick: () -> Unit) {
    val isVideo = isVideoMidia(midia)
    Card(modifier = Modifier.aspectRatio(1f).clickable { onClick() }) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = midia.thumbnailUrl ?: midia.url,
                contentDescription = midia.descricao,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            if (isVideo) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
    }
}
