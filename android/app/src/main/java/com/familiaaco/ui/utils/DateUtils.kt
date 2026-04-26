package com.familiaaco.ui.utils

import java.text.SimpleDateFormat
import java.util.*

fun formatarDataParaExibicao(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return ""
    return try {
        val entrada = when {
            isoDate.length >= 19 -> SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            isoDate.length == 10 -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            else -> return isoDate
        }
        entrada.isLenient = true
        val date = entrada.parse(isoDate) ?: return isoDate
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
    } catch (_: Exception) {
        isoDate.take(10)
    }
}

fun formatarDataHoraParaExibicao(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return ""
    return try {
        val entrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        entrada.isLenient = true
        val date = entrada.parse(isoDate) ?: return isoDate
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date)
    } catch (_: Exception) {
        isoDate.take(16).replace("T", " ")
    }
}
