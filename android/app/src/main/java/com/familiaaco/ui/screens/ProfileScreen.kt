package com.familiaaco.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.familiaaco.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = ProfileViewModel(context) as T
    })

    val state by viewModel.profileState.collectAsState()
    val senhaState by viewModel.senhaState.collectAsState()

    var nome by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var senhaAtual by remember { mutableStateOf("") }
    var novaSenha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    var editando by remember { mutableStateOf(false) }
    var alterandoSenha by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.carregarPerfil() }

    LaunchedEffect(state) {
        if (state is ProfileViewModel.ProfileState.Success) {
            val u = (state as ProfileViewModel.ProfileState.Success).usuario
            if (nome.isEmpty()) { nome = u.nome; telefone = u.telefone ?: "" }
        }
    }

    LaunchedEffect(senhaState) {
        if (senhaState is ProfileViewModel.SenhaState.Success) {
            kotlinx.coroutines.delay(2000)
            viewModel.resetSenhaState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meu Perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (state) {
                is ProfileViewModel.ProfileState.Loading ->
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is ProfileViewModel.ProfileState.Success -> {
                    val usuario = (state as ProfileViewModel.ProfileState.Success).usuario

                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text("Dados Pessoais", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.weight(1f))
                                IconButton(onClick = { editando = !editando }) {
                                    Icon(if (editando) Icons.Default.Close else Icons.Default.Edit, null)
                                }
                            }
                            if (!editando) {
                                InfoRow("Nome", usuario.nome)
                                InfoRow("Email", usuario.email)
                                InfoRow("Telefone", usuario.telefone ?: "—")
                                InfoRow("Função", usuario.role)
                            } else {
                                OutlinedTextField(value = nome, onValueChange = { nome = it },
                                    label = { Text("Nome") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                OutlinedTextField(value = telefone, onValueChange = { telefone = it },
                                    label = { Text("Telefone") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                                if (state is ProfileViewModel.ProfileState.Loading) {
                                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator()
                                    }
                                } else {
                                    Button(
                                        onClick = { viewModel.atualizarPerfil(nome, telefone.ifEmpty { null }); editando = false },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = nome.isNotBlank()
                                    ) { Text("Salvar alterações") }
                                }
                            }
                        }
                    }

                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text("Alterar Senha", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.weight(1f))
                                IconButton(onClick = { alterandoSenha = !alterandoSenha }) {
                                    Icon(if (alterandoSenha) Icons.Default.Close else Icons.Default.Edit, null)
                                }
                            }
                            if (alterandoSenha) {
                                OutlinedTextField(value = senhaAtual, onValueChange = { senhaAtual = it },
                                    label = { Text("Senha atual") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
                                OutlinedTextField(value = novaSenha, onValueChange = { novaSenha = it },
                                    label = { Text("Nova senha") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
                                OutlinedTextField(value = confirmarSenha, onValueChange = { confirmarSenha = it },
                                    label = { Text("Confirmar nova senha") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    isError = confirmarSenha.isNotEmpty() && novaSenha != confirmarSenha)
                                if (senhaState is ProfileViewModel.SenhaState.Error)
                                    Text((senhaState as ProfileViewModel.SenhaState.Error).message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                if (senhaState is ProfileViewModel.SenhaState.Success)
                                    Text("Senha alterada com sucesso!", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                                Button(
                                    onClick = {
                                        viewModel.alterarSenha(senhaAtual, novaSenha)
                                        alterandoSenha = false; senhaAtual = ""; novaSenha = ""; confirmarSenha = ""
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = senhaAtual.isNotBlank() && novaSenha.length >= 6 && novaSenha == confirmarSenha
                                ) { Text("Alterar senha") }
                            }
                        }
                    }
                }
                is ProfileViewModel.ProfileState.Error ->
                    Text((state as ProfileViewModel.ProfileState.Error).message, color = MaterialTheme.colorScheme.error)
                else -> {}
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text("$label: ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
