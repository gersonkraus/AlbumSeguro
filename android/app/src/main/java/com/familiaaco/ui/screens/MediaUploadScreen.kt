package com.familiaaco.ui.screens

import android.net.Uri
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
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MediaUploadScreen(navController: NavController, criancaId: String? = null) {
    val context = LocalContext.current
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var descricao by remember { mutableStateOf("") }
    var dataMomento by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }

    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedFileUri = uri
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) }
            Text("Upload de Mídia", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = { fileLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth().height(120.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CloudUpload, null, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text(selectedFileUri?.lastPathSegment ?: "Selecione uma mídia")
            }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = descricao, onValueChange = { descricao = it },
            label = { Text("Descrição (opcional)") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = dataMomento, onValueChange = { dataMomento = it },
            label = { Text("Data do momento") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            enabled = selectedFileUri != null
        ) { Text("Enviar Mídia") }
    }
}
