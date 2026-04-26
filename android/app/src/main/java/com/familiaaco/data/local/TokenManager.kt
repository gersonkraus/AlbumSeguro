package com.familiaaco.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedSharedPreferences = EncryptedSharedPreferences.create(
        context,
        "token_storage",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String) {
        encryptedSharedPreferences.edit().putString("auth_token", token).apply()
    }

    fun getToken(): String? {
        return encryptedSharedPreferences.getString("auth_token", null)
    }

    fun saveRefreshToken(token: String) {
        encryptedSharedPreferences.edit().putString("refresh_token", token).apply()
    }

    fun getRefreshToken(): String? {
        return encryptedSharedPreferences.getString("refresh_token", null)
    }

    fun saveUserRole(role: String) {
        encryptedSharedPreferences.edit().putString("user_role", role).apply()
    }

    fun getUserRole(): String? {
        return encryptedSharedPreferences.getString("user_role", null)
    }

    fun clearAll() {
        encryptedSharedPreferences.edit().clear().apply()
    }
}
