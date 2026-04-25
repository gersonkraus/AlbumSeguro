package com.familiaaco.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.familiaaco.ui.theme.PrimaryColor
import com.familiaaco.viewmodel.ChildrenViewModel
import com.familiaaco.viewmodel.MediaViewModel

@Composable
fun ChildDetailScreen(navController: NavController, childId: String?) {
    if (childId == null) { LaunchedEffect(Unit) { navController.popBackStack() }; return }
    val context = LocalContext.current
    val childrenVm: ChildrenViewModel = viewModel(key = "children_$childId",
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = ChildrenViewModel(context) as T
        })
    val mediaVm: MediaViewModel = viewModel(key = "media_$childId",
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = MediaViewModel(context) as T
        })
    val tokenState by childrenVm.tokenState.collectAsState()
    val mediaState by mediaVm.mediaState.collectAsState()

    LaunchedEffect(childId) { mediaVm.listarMidia(childId) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) }
            Text("Detalhes", style = MaterialTheme.typography.headlineSmall, color = PrimaryColor, modifier = Modifier.weight(1f))
        }
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Token de Acesso", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                when (tokenState) {
                    is ChildrenViewModel.TokenState.Success ->
                        Text((tokenState as ChildrenViewModel.TokenState.Success).token, color = PrimaryColor)
                    is ChildrenViewModel.TokenState.Loading -> CircularProgressIndicator(Modifier.size(24.dp))
                    is ChildrenViewModel.TokenState.Error ->
                        Text((tokenState as ChildrenViewModel.TokenState.Error).message, color = MaterialTheme.colorScheme.error)
                    else -> Text("Token não gerado", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { childrenVm.gerarToken(childId) }, Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Refresh, null); Spacer(Modifier.width(8.dp)); Text("Gerar Novo Token")
                }
                Spacer(Modifier.height(4.dp))
                OutlinedButton(onClick = { navController.navigate("media_upload/$childId") }, Modifier.fillMaxWidth()) {
                    Text("Adicionar Mídia")
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Galeria", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
        when (mediaState) {
            is MediaViewModel.MediaState.Loading ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is MediaViewModel.MediaState.Success -> {
                val midias = (mediaState as MediaViewModel.MediaState.Success).midias
                if (midias.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Nenhuma mídia cadastrada") }
                } else {
                    LazyVerticalGrid(columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(midias) { midia ->
                            Card(modifier = Modifier.aspectRatio(1f)) {
                                AsyncImage(model = midia.url, contentDescription = midia.descricao,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                            }
                        }
                    }
                }
            }
            is MediaViewModel.MediaState.Error ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text((mediaState as MediaViewModel.MediaState.Error).message)
                }
            else -> {}
        }
    }
}
