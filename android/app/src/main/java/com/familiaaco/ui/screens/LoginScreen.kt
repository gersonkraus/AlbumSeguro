package com.familiaaco.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.familiaaco.data.local.TokenManager
import com.familiaaco.ui.theme.PrimaryColor
import com.familiaaco.viewmodel.LoginViewModel

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: LoginViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = LoginViewModel(context) as T
    })
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    val loginState by viewModel.loginState.collectAsState()
    var verificando by remember { mutableStateOf(true) }

    // Auto-login: se já existe token salvo, pula direto para o dashboard
    LaunchedEffect(Unit) {
        val tokenManager = TokenManager(context)
        if (tokenManager.getToken() != null) {
            navController.navigate("admin_dashboard") { popUpTo("login") { inclusive = true } }
        } else {
            verificando = false
        }
    }

    LaunchedEffect(loginState) {
        if (loginState is LoginViewModel.LoginState.Success) {
            navController.navigate("admin_dashboard") { popUpTo("login") { inclusive = true } }
        }
    }

    // Splash enquanto verifica token
    if (verificando) {
        Box(
            modifier = Modifier.fillMaxSize().background(PrimaryColor),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(modifier = Modifier.size(88.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.20f)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PhotoAlbum, null, modifier = Modifier.size(44.dp), tint = Color.White)
                    }
                }
                Text("Álbum Seguro", style = MaterialTheme.typography.headlineLarge, color = Color.White)
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
            }
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryColor)
    ) {
        // Parte azul superior — logo e título
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 80.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(88.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.20f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PhotoAlbum,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        tint = Color.White
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Álbum Seguro",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Guardando memórias com carinho",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.80f),
                textAlign = TextAlign.Center
            )
        }

        // Card branco inferior com os campos
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Entrar na conta",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(28.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = senha,
                    onValueChange = { senha = it },
                    label = { Text("Senha") },
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(28.dp))
                Button(
                    onClick = { viewModel.login(email, senha) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = email.isNotEmpty() && senha.isNotEmpty() && loginState !is LoginViewModel.LoginState.Loading,
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    if (loginState is LoginViewModel.LoginState.Loading)
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                    else
                        Text("Entrar", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
                if (loginState is LoginViewModel.LoginState.Error) {
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = (loginState as LoginViewModel.LoginState.Error).message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
