package com.familiaaco.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.familiaaco.viewmodel.ChildrenViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CreateChildScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: ChildrenViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = ChildrenViewModel(context) as T
    })
    var nome by remember { mutableStateOf("") }
    var dataNascimentoApi by remember { mutableStateOf("") }
    var dataNascimentoExibicao by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    val state by viewModel.childrenState.collectAsState()
    val hoje = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            val cal = Calendar.getInstance().apply { set(year, month, day) }
            dataNascimentoApi = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
            dataNascimentoExibicao = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.time)
        },
        hoje.get(Calendar.YEAR),
        hoje.get(Calendar.MONTH),
        hoje.get(Calendar.DAY_OF_MONTH)
    )

    LaunchedEffect(state) {
        if (state is ChildrenViewModel.ChildrenState.Success) navController.popBackStack()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) }
            Text("Nova Criança", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(value = nome, onValueChange = { nome = it },
            label = { Text("Nome completo") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = dataNascimentoExibicao,
            onValueChange = { },
            label = { Text("Data de nascimento") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = { datePickerDialog.show() }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Selecionar data")
                }
            }
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = descricao, onValueChange = { descricao = it },
            label = { Text("Descrição (opcional)") }, modifier = Modifier.fillMaxWidth(), maxLines = 4)
        Spacer(Modifier.height(24.dp))
        if (state is ChildrenViewModel.ChildrenState.Error) {
            Text((state as ChildrenViewModel.ChildrenState.Error).message, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }
        Button(
            onClick = { viewModel.criarCrianca(nome.trim(), dataNascimentoApi.trim(), descricao.trim().ifEmpty { null }) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            enabled = nome.isNotBlank() && dataNascimentoApi.isNotBlank() && state !is ChildrenViewModel.ChildrenState.Loading
        ) {
            if (state is ChildrenViewModel.ChildrenState.Loading)
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            else Text("Criar Criança")
        }
    }
}
