package com.familiaaco.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.familiaaco.viewmodel.MediaViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MediaUploadScreen(navController: NavController, criancaId: String? = null) {
    val context = LocalContext.current
    val viewModel: MediaViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = MediaViewModel(context) as T
    })
    val mediaState by viewModel.mediaState.collectAsState()

    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var descricao by remember { mutableStateOf("") }
    var dataMomento by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var showSuccess by remember { mutableStateOf(false) }

    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedFileUri = uri
        selectedFileName = uri?.let { getFileName(context, it) }
    }

    // Observar estado do upload
    LaunchedEffect(mediaState) {
        when (mediaState) {
            is MediaViewModel.MediaState.Success -> {
                showSuccess = true
                kotlinx.coroutines.delay(1500)
                navController.popBackStack()
            }
            else -> {}
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) }
            Text("Upload de Mídia", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(24.dp))

        // Botão de seleção
        Button(
            onClick = { fileLauncher.launch("image/*,video/*") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            enabled = mediaState !is MediaViewModel.MediaState.Loading
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CloudUpload, null, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text(selectedFileName ?: "Selecione uma foto ou vídeo")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Campos de texto
        OutlinedTextField(
            value = descricao,
            onValueChange = { descricao = it },
            label = { Text("Descrição (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            enabled = mediaState !is MediaViewModel.MediaState.Loading
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = dataMomento,
            onValueChange = { dataMomento = it },
            label = { Text("Data do momento") },
            modifier = Modifier.fillMaxWidth(),
            enabled = mediaState !is MediaViewModel.MediaState.Loading
        )

        Spacer(Modifier.height(24.dp))

        // Botão de envio
        Button(
            onClick = {
                if (criancaId != null && selectedFileUri != null) {
                    val file = uriToFile(context, selectedFileUri!!)
                    if (file != null) {
                        viewModel.uploadMidia(criancaId, file, descricao, dataMomento)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            enabled = selectedFileUri != null && 
                     criancaId != null && 
                     mediaState !is MediaViewModel.MediaState.Loading
        ) {
            if (mediaState is MediaViewModel.MediaState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Enviar Mídia")
            }
        }

        // Estados
        when (mediaState) {
            is MediaViewModel.MediaState.Error -> {
                Spacer(Modifier.height(16.dp))
                Text(
                    (mediaState as MediaViewModel.MediaState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {}
        }
    }

    // Dialog de sucesso
    if (showSuccess) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Sucesso!") },
            text = { Text("Mídia enviada com sucesso.") },
            confirmButton = { }
        )
    }
}

// Função auxiliar para obter nome do arquivo
private fun getFileName(context: android.content.Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) result = cursor.getString(index)
            }
        }
    }
    if (result == null) {
        result = uri.path?.substringAfterLast('/')
    }
    return result
}

// Função auxiliar para converter Uri em File
private fun uriToFile(context: android.content.Context, uri: Uri): File? {
    return try {
        val fileName = getFileName(context, uri) ?: "temp_file"
        val tempFile = File(context.cacheDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
