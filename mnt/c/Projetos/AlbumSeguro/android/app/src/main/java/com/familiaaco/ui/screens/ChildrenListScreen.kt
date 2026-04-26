package com.familiaaco.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.familiaaco.data.models.CriancaDTO
import com.familiaaco.ui.theme.PrimaryColor
import com.familiaaco.viewmodel.ChildrenViewModel

@Composable
fun ChildrenListScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: ChildrenViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = ChildrenViewModel(context) as T
    })
    val state by viewModel.childrenState.collectAsState()

    LaunchedEffect(Unit) { viewModel.listarCriancas() }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) }
            Text("Crianças", style = MaterialTheme.typography.headlineSmall, color = PrimaryColor, modifier = Modifier.weight(1f))
            IconButton(onClick = { navController.navigate("create_child") }) { Icon(Icons.Default.Add, null) }
        }
        when (state) {
            is ChildrenViewModel.ChildrenState.Loading ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is ChildrenViewModel.ChildrenState.Success -> {
                val criancas = (state as ChildrenViewModel.ChildrenState.Success).criancas
                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    items(criancas) { crianca ->
                        CriancaCard(crianca, navController)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
            is ChildrenViewModel.ChildrenState.Error ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text((state as ChildrenViewModel.ChildrenState.Error).message)
                }
            else -> {}
        }
    }
}

@Composable
fun CriancaCard(crianca: CriancaDTO, navController: NavController) {
    Card(modifier = Modifier.fillMaxWidth().height(80.dp),
        onClick = { navController.navigate("child_detail/${crianca._id}") }) {
        Row(modifier = Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(crianca.nome, style = MaterialTheme.typography.titleMedium)
                Text(crianca.dataNascimento, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
