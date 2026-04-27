package com.familiaaco.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.familiaaco.ui.theme.PrimaryColor
import com.familiaaco.viewmodel.ChildAlbumViewModel

@Composable
fun NiCollasAlbumScreen(navController: NavController, token: String) {
    val context = LocalContext.current
    val viewModel: ChildAlbumViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                ChildAlbumViewModel(context) as T
        }
    )
    val albumState by viewModel.albumState.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val gridState = rememberLazyGridState()

    LaunchedEffect(token) { viewModel.carregarAlbum(token) }
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .collect { lastIndex ->
                val total = gridState.layoutInfo.totalItemsCount
                if (lastIndex >= total - 6 && hasMore && !isLoadingMore) {
                    viewModel.carregarMais()
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Meu Álbum",
                style = MaterialTheme.typography.headlineSmall,
                color = PrimaryColor
            )
        }

        when (albumState) {
            is ChildAlbumViewModel.AlbumState.Loading ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

            is ChildAlbumViewModel.AlbumState.Success -> {
                val midias = (albumState as ChildAlbumViewModel.AlbumState.Success).midias
                if (midias.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Nenhuma foto ou vídeo ainda.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        state = gridState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
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
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            }

            is ChildAlbumViewModel.AlbumState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (albumState as ChildAlbumViewModel.AlbumState.Error).message,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.carregarAlbum(token) }) {
                            Text("Tentar Novamente")
                        }
                    }
                }
            }

            else -> {}
        }
    }
}
