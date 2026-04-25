package com.familiaaco.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.familiaaco.data.models.MidiaDTO
import com.familiaaco.ui.theme.PrimaryColor
import com.familiaaco.viewmodel.ChildAlbumViewModel

@Composable
fun ChildAlbumScreen(navController: NavController, token: String) {
    val context = LocalContext.current
    val viewModel: ChildAlbumViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = ChildAlbumViewModel(context) as T
    })
    val albumState by viewModel.albumState.collectAsState()

    LaunchedEffect(token) { viewModel.carregarAlbum(token) }

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
                        items(midias) { midia -> MidiaGridItem(midia) }
                    }
                }
            }
            is ChildAlbumViewModel.AlbumState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text((albumState as ChildAlbumViewModel.AlbumState.Error).message)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.carregarAlbum(token) }) { Text("Tentar Novamente") }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun MidiaGridItem(midia: MidiaDTO) {
    Card(modifier = Modifier.aspectRatio(1f)) {
        AsyncImage(model = midia.url, contentDescription = midia.descricao,
            modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
    }
}
