package com.familiaaco.ui.screens

import android.app.DatePickerDialog
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
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

    var selectedFileUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var descricao by remember { mutableStateOf("") }
    val hoje = Calendar.getInstance()
    var dataMomentoApi by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var dataMomentoExibicao by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            val cal = Calendar.getInstance().apply { set(year, month, day) }
            dataMomentoApi = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
            dataMomentoExibicao = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.time)
        },
        hoje.get(Calendar.YEAR),
        hoje.get(Calendar.MONTH),
        hoje.get(Calendar.DAY_OF_MONTH)
    )
    var uploadSummary by remember { mutableStateOf<MediaViewModel.MediaState.UploadSummary?>(null) }

    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) {
            selectedFileUris = (selectedFileUris + uris).distinctBy { it.toString() }
        }
    }

    // Observar estado do upload
    LaunchedEffect(mediaState) {
        when (mediaState) {
            is MediaViewModel.MediaState.UploadSummary -> {
                uploadSummary = mediaState as MediaViewModel.MediaState.UploadSummary
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
            onClick = { fileLauncher.launch(arrayOf("image/*", "video/*")) },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            enabled = mediaState !is MediaViewModel.MediaState.Loading
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text("Selecionar fotos e vídeos")
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { fileLauncher.launch(arrayOf("image/*", "video/*")) },
                enabled = mediaState !is MediaViewModel.MediaState.Loading,
                modifier = Modifier.weight(1f)
            ) {
                Text("Adicionar mais")
            }
            OutlinedButton(
                onClick = { selectedFileUris = emptyList() },
                enabled = selectedFileUris.isNotEmpty() && mediaState !is MediaViewModel.MediaState.Loading,
                modifier = Modifier.weight(1f)
            ) {
                Text("Limpar")
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = if (selectedFileUris.isEmpty()) {
                "Nenhum arquivo selecionado"
            } else {
                "${selectedFileUris.size} arquivo(s) selecionado(s)"
            },
            style = MaterialTheme.typography.bodyMedium
        )

        if (selectedFileUris.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 180.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                selectedFileUris.take(8).forEach { uri ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getFileName(context, uri) ?: "arquivo",
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        IconButton(
                            onClick = {
                                selectedFileUris = selectedFileUris.filterNot { it == uri }
                            },
                            enabled = mediaState !is MediaViewModel.MediaState.Loading
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remover arquivo")
                        }
                    }
                }
                if (selectedFileUris.size > 8) {
                    Text(
                        text = "+${selectedFileUris.size - 8} arquivo(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
            value = dataMomentoExibicao,
            onValueChange = { },
            label = { Text("Data do momento") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            enabled = mediaState !is MediaViewModel.MediaState.Loading,
            trailingIcon = {
                IconButton(
                    onClick = { if (mediaState !is MediaViewModel.MediaState.Loading) datePickerDialog.show() },
                    enabled = mediaState !is MediaViewModel.MediaState.Loading
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = "Selecionar data")
                }
            }
        )

        Spacer(Modifier.height(24.dp))

        val uploadingState = mediaState as? MediaViewModel.MediaState.Uploading
        if (uploadingState != null) {
            val progress = uploadingState.current.toFloat() / uploadingState.total.toFloat()
            Text(
                text = "Enviando ${uploadingState.current}/${uploadingState.total}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = uploadingState.fileName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
        }

        // Botão de envio
        Button(
            onClick = {
                if (criancaId != null && selectedFileUris.isNotEmpty()) {
                    val files = selectedFileUris.mapNotNull { uriToFile(context, it) }
                    if (files.isNotEmpty()) {
                        viewModel.uploadMidias(criancaId, files, descricao, dataMomentoApi)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            enabled = selectedFileUris.isNotEmpty() &&
                     criancaId != null && 
                     mediaState !is MediaViewModel.MediaState.Loading &&
                     mediaState !is MediaViewModel.MediaState.Uploading
        ) {
            if (mediaState is MediaViewModel.MediaState.Loading || mediaState is MediaViewModel.MediaState.Uploading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Enviar ${selectedFileUris.size} arquivo(s)")
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

    if (uploadSummary != null) {
        val summary = uploadSummary!!
        val falhasPreview = summary.failedFileNames.take(5)
        AlertDialog(
            onDismissRequest = { uploadSummary = null },
            title = { Text("Upload concluído") },
            text = {
                Text(
                    buildString {
                        append("Enviados: ${summary.successCount}/${summary.total}")
                        if (summary.failCount > 0) {
                            append("\nFalharam: ${summary.failCount}")
                            append("\n")
                            falhasPreview.forEach { append("\n• $it") }
                            if (summary.failedFileNames.size > falhasPreview.size) {
                                append("\n... e mais ${summary.failedFileNames.size - falhasPreview.size}")
                            }
                        }
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (summary.failCount == 0) {
                        navController.popBackStack()
                    } else {
                        selectedFileUris = selectedFileUris.filter { uri ->
                            val nome = getFileName(context, uri)
                            nome != null && summary.failedFileNames.contains(nome)
                        }
                    }
                    uploadSummary = null
                }) {
                    Text(if (summary.failCount == 0) "Concluir" else "Tentar novamente")
                }
            },
            dismissButton = {
                if (summary.failCount > 0) {
                    TextButton(onClick = {
                        uploadSummary = null
                    }) {
                        Text("Fechar")
                    }
                }
            }
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
