package com.familiaaco.ui.utils

import android.net.Uri
import java.util.Locale

fun extrairTokenDeEntrada(rawInput: String?): String? {
    if (rawInput.isNullOrBlank()) return null
    val raw = rawInput.trim()

    if (raw.length == 32 && raw.all { it.isLetterOrDigit() }) {
        return raw.uppercase(Locale.ROOT)
    }

    return try {
        val uri = Uri.parse(raw)
        val pathSegments = uri.pathSegments
        val albumIndex = pathSegments.indexOf("album")
        if (albumIndex >= 0 && albumIndex + 1 < pathSegments.size) {
            val token = pathSegments[albumIndex + 1].trim()
            if (token.length == 32 && token.all { it.isLetterOrDigit() }) {
                token.uppercase(Locale.ROOT)
            } else {
                null
            }
        } else {
            val token = uri.getQueryParameter("token")?.trim()
            if (!token.isNullOrBlank() && token.length == 32 && token.all { it.isLetterOrDigit() }) {
                token.uppercase(Locale.ROOT)
            } else {
                null
            }
        }
    } catch (_: Exception) {
        null
    }
}
