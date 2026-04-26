package com.familiaaco.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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

    LaunchedEffect(loginState) {
        if (loginState is LoginViewModel.LoginState.Success) {
            navController.navigate("admin_dashboard") { popUpTo("login") { inclusive = true } }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Família Acolhedora", style = MaterialTheme.typography.headlineLarge, color = PrimaryColor, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Email") }, leadingIcon = { Icon(Icons.Default.Email, null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), singleLine = true
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = senha, onValueChange = { senha = it },
            label = { Text("Senha") }, leadingIcon = { Icon(Icons.Default.Lock, null) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), singleLine = true
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { viewModel.login(email, senha) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            enabled = email.isNotEmpty() && senha.isNotEmpty() && loginState !is LoginViewModel.LoginState.Loading
        ) {
            if (loginState is LoginViewModel.LoginState.Loading)
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            else Text("Entrar")
        }
        if (loginState is LoginViewModel.LoginState.Error) {
            Spacer(Modifier.height(16.dp))
            Text((loginState as LoginViewModel.LoginState.Error).message, color = MaterialTheme.colorScheme.error)
        }
    }
}
