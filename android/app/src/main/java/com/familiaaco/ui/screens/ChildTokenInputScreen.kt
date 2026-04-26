package com.familiaaco.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.familiaaco.data.local.TokenManager
import com.familiaaco.ui.theme.PrimaryColor
import com.familiaaco.ui.utils.extrairTokenDeEntrada

@Composable
fun ChildTokenInputScreen(navController: NavController) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    var token by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val savedToken = tokenManager.getChildToken()
        if (!savedToken.isNullOrBlank()) {
            navController.navigate("child_album/$savedToken")
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Bem-vindo!", style = MaterialTheme.typography.headlineLarge, color = PrimaryColor, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(80.dp), tint = PrimaryColor)
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
                val tokenExtraido = extrairTokenDeEntrada(token)
                if (!tokenExtraido.isNullOrBlank()) {
                    navController.navigate("child_album/$tokenExtraido")
                } else {
                    errorMessage = "Token ou link inválido."
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) { Text("Acessar Álbum") }
        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = { navController.navigate("qr_scanner") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Escanear QR Code")
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = { navController.navigate("nfc_scanner") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Nfc, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Ler Tag NFC")
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("login") }) {
            Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("Área administrativa")
        }

        errorMessage?.let {
            Spacer(Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        }
    }
}
