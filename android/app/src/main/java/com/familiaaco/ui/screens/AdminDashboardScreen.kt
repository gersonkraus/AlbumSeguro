package com.familiaaco.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.familiaaco.ui.theme.PrimaryColor

@Composable
fun AdminDashboardScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Painel Admin", style = MaterialTheme.typography.headlineSmall, color = PrimaryColor)
            IconButton(onClick = { navController.navigate("login") }) { Icon(Icons.Default.ExitToApp, "Sair") }
        }
        AdminMenuCard("Gerenciar Crianças", "Criar, editar e gerenciar crianças") { navController.navigate("children_list") }
        Spacer(Modifier.height(16.dp))
        AdminMenuCard("Upload de Mídia", "Adicionar fotos e vídeos") { navController.navigate("media_upload") }
        Spacer(Modifier.height(16.dp))
        AdminMenuCard("Gerenciar Admins", "Criar e gerenciar usuários") { navController.navigate("admin_list") }
    }
}

@Composable
fun AdminMenuCard(titulo: String, descricao: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().height(120.dp), onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(titulo, style = MaterialTheme.typography.headlineSmall)
            Text(descricao, style = MaterialTheme.typography.bodySmall)
        }
    }
}
