package com.familiaaco.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.familiaaco.viewmodel.AppConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppConfigScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: AppConfigViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = AppConfigViewModel(context) as T
    })
    val state by viewModel.state.collectAsState()

    var childAlbumBaseUrl by remember { mutableStateOf("") }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.carregarConfig()
    }

    LaunchedEffect(state) {
        when (state) {
            is AppConfigViewModel.AppConfigState.Success -> {
                val success = state as AppConfigViewModel.AppConfigState.Success
                childAlbumBaseUrl = success.config.childAlbumBaseUrl
                infoMessage = success.message
            }
            is AppConfigViewModel.AppConfigState.Error -> {
                infoMessage = (state as AppConfigViewModel.AppConfigState.Error).message
            }
            else -> Unit
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TopAppBar(
            title = { Text("Configuração do App") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                }
            }
        )

        Spacer(Modifier.height(16.dp))

        Text(
            "Defina a URL base para links do álbum infantil. Ex.: https://meudominio.com",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = childAlbumBaseUrl,
            onValueChange = {
                childAlbumBaseUrl = it
                infoMessage = null
            },
            label = { Text("URL base do álbum infantil") },
            placeholder = { Text("https://seu-dominio.com") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is AppConfigViewModel.AppConfigState.Loading
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "O token será anexado automaticamente como /album/{TOKEN}.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            TextButton(
                onClick = { viewModel.carregarConfig() },
                enabled = state !is AppConfigViewModel.AppConfigState.Loading,
                modifier = Modifier.weight(1f)
            ) { Text("Recarregar") }

            Button(
                onClick = {
                    viewModel.salvarConfig(childAlbumBaseUrl.trim())
                },
                enabled = state !is AppConfigViewModel.AppConfigState.Loading,
                modifier = Modifier.weight(1f)
            ) {
                if (state is AppConfigViewModel.AppConfigState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.height(18.dp))
                } else {
                    Text("Salvar")
                }
            }
        }

        infoMessage?.let {
            Spacer(Modifier.height(16.dp))
            Text(
                text = it,
                color = if (state is AppConfigViewModel.AppConfigState.Error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (state is AppConfigViewModel.AppConfigState.Loading) {
            Spacer(Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator()
            }
        }
    }
}
