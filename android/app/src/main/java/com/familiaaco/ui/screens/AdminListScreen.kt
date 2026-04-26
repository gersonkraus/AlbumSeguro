package com.familiaaco.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.familiaaco.data.models.UsuarioDTO
import com.familiaaco.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminListScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: AdminViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = AdminViewModel(context) as T
    })
    val state by viewModel.adminState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var novoNome by remember { mutableStateOf("") }
    var novoEmail by remember { mutableStateOf("") }
    var novaSenha by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.listarAdmins() }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Novo Admin") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = novoNome, onValueChange = { novoNome = it },
                        label = { Text("Nome") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = novoEmail, onValueChange = { novoEmail = it },
                        label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                    OutlinedTextField(value = novaSenha, onValueChange = { novaSenha = it },
                        label = { Text("Senha") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCreateDialog = false
                        viewModel.criarAdmin(novoNome.trim(), novoEmail.trim(), novaSenha) { sucesso ->
                            if (sucesso) {
                                novoNome = ""; novoEmail = ""; novaSenha = ""
                            }
                        }
                    },
                    enabled = novoNome.isNotBlank() && novoEmail.isNotBlank() && novaSenha.length >= 6
                ) { Text("Criar") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admins", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) { Icon(Icons.Default.Add, "Novo Admin") }
        }
    ) { paddingValues ->
        when (state) {
            is AdminViewModel.AdminState.Loading ->
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is AdminViewModel.AdminState.Success -> {
                val admins = (state as AdminViewModel.AdminState.Success).admins
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(admins) { admin ->
                        AdminCard(admin = admin, onDeactivate = { viewModel.deletarAdmin(admin._id) })
                    }
                }
            }
            is AdminViewModel.AdminState.Error ->
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text((state as AdminViewModel.AdminState.Error).message, color = MaterialTheme.colorScheme.error)
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
            if (admin.ativo) IconButton(onClick = { showConfirm = true }) { Icon(Icons.Default.Delete, "Desativar") }
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
