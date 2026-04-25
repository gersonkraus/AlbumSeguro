package com.familiaaco.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.familiaaco.ui.theme.PrimaryColor

@Composable
fun ChildTokenInputScreen(navController: NavController) {
    var token by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Bem-vindo!", style = MaterialTheme.typography.headlineLarge, color = PrimaryColor, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        Icon(Icons.Default.QrCode, null, modifier = Modifier.size(80.dp), tint = PrimaryColor)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(
            value = token,
            onValueChange = { token = it.uppercase(); errorMessage = null },
            label = { Text("Digite seu token") },
            modifier = Modifier.fillMaxWidth(),
            isError = errorMessage != null,
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (token.length == 32) {
                    navController.navigate("child_album/$token") { popUpTo("child_token") { inclusive = true } }
                } else {
                    errorMessage = "Token inválido. Deve ter 32 caracteres."
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) { Text("Acessar Álbum") }
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = { navController.navigate("qr_scanner") }) { Text("Escanear QR Code") }
        errorMessage?.let {
            Spacer(Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        }
    }
}
