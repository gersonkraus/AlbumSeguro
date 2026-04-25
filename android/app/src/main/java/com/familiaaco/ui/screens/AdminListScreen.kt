package com.familiaaco.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.familiaaco.data.models.UsuarioDTO
import com.familiaaco.ui.theme.PrimaryColor
import com.familiaaco.viewmodel.AdminViewModel

@Composable
fun AdminListScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: AdminViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = AdminViewModel(context) as T
    })
    val state by viewModel.adminState.collectAsState()
    LaunchedEffect(Unit) { viewModel.listarAdmins() }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) }
            Text("Gerenciar Admins", style = MaterialTheme.typography.headlineSmall, color = PrimaryColor, modifier = Modifier.weight(1f))
        }
        when (state) {
            is AdminViewModel.AdminState.Loading ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is AdminViewModel.AdminState.Success -> {
                val admins = (state as AdminViewModel.AdminState.Success).admins
                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    items(admins) { admin ->
                        AdminCard(admin = admin, onDeactivate = { viewModel.deletarAdmin(admin._id) })
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
            is AdminViewModel.AdminState.Error ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text((state as AdminViewModel.AdminState.Error).message)
                }
            else -> {}
        }
    }
}

@Composable
fun AdminCard(admin: UsuarioDTO, onDeactivate: () -> Unit) {
    var showConfirm by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (admin.ativo) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(admin.nome, style = MaterialTheme.typography.titleMedium)
                Text(admin.email, style = MaterialTheme.typography.bodySmall)
                Text("Papel: ${admin.role}", style = MaterialTheme.typography.labelSmall)
                if (!admin.ativo) Text("Inativo", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }
            if (admin.ativo) IconButton(onClick = { showConfirm = true }) { Icon(Icons.Default.Block, "Desativar") }
        }
    }
    if (showConfirm) {
        AlertDialog(onDismissRequest = { showConfirm = false },
            title = { Text("Desativar Admin") },
            text = { Text("Deseja desativar ${admin.nome}?") },
            confirmButton = { TextButton(onClick = { showConfirm = false; onDeactivate() }) { Text("Confirmar") } },
            dismissButton = { TextButton(onClick = { showConfirm = false }) { Text("Cancelar") } })
    }
}
