package com.familiaaco.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.familiaaco.ui.utils.extrairTokenDeEntrada

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NFCScannerScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val nfcAdapter = remember { NfcAdapter.getDefaultAdapter(context) }
    var statusMessage by remember { mutableStateOf("Aproxime a tag NFC do aparelho") }
    var reading by remember { mutableStateOf(false) }

    DisposableEffect(activity, nfcAdapter) {
        if (activity != null && nfcAdapter != null) {
            val callback = NfcAdapter.ReaderCallback { tag ->
                reading = true
                val token = lerTokenDaTag(tag)
                activity.runOnUiThread {
                    if (!token.isNullOrBlank()) {
                        navController.navigate("child_album/$token") {
                            popUpTo("nfc_scanner") { inclusive = true }
                        }
                    } else {
                        statusMessage = "Não foi possível ler um token válido nesta tag."
                    }
                    reading = false
                }
            }

            nfcAdapter.enableReaderMode(
                activity,
                callback,
                NfcAdapter.FLAG_READER_NFC_A or
                        NfcAdapter.FLAG_READER_NFC_B or
                        NfcAdapter.FLAG_READER_NFC_F or
                        NfcAdapter.FLAG_READER_NFC_V,
                null
            )
        }

        onDispose {
            if (activity != null && nfcAdapter != null) {
                nfcAdapter.disableReaderMode(activity)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TopAppBar(
            title = { Text("Ler Tag NFC") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                }
            }
        )

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Nfc,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            if (reading) {
                CircularProgressIndicator()
            }

            Spacer(Modifier.height(8.dp))

            Button(onClick = { navController.navigate("child_token") }) {
                Text("Digitar token")
            }
        }
    }
}

private fun lerTokenDaTag(tag: Tag): String? {
    return try {
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            ndef.connect()
            val message = ndef.ndefMessage
            ndef.close()
            extrairTokenDeMensagemNdef(message)
        } else {
            val ndefFormatable = NdefFormatable.get(tag)
            if (ndefFormatable != null) {
                null
            } else {
                null
            }
        }
    } catch (_: Exception) {
        null
    }
}

private fun extrairTokenDeMensagemNdef(message: NdefMessage?): String? {
    if (message == null) return null
    for (record in message.records) {
        val tokenFromUri = record.toUri()?.toString()?.let { extrairTokenDeEntrada(it) }
        if (!tokenFromUri.isNullOrBlank()) return tokenFromUri

        val payload = record.payload ?: continue
        val text = try {
            if (payload.isNotEmpty()) {
                val languageCodeLength = payload[0].toInt() and 0x3F
                if (payload.size > languageCodeLength + 1) {
                    String(payload, languageCodeLength + 1, payload.size - languageCodeLength - 1).trim()
                } else {
                    String(payload).trim()
                }
            } else {
                ""
            }
        } catch (_: Exception) {
            continue
        }
        val token = extrairTokenDeEntrada(text)
        if (!token.isNullOrBlank()) return token
    }
    return null
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
