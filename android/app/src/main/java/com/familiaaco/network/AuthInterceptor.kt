package com.familiaaco.network

import com.familiaaco.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenManager.getToken()
        val originalRequest = chain.request()

        val request = if (token != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(request)

        if (response.code == 401 && !originalRequest.url.toString().contains("/auth/refresh")) {
            val refreshToken = tokenManager.getRefreshToken() ?: return response
            val newAccessToken = runRefresh(refreshToken) ?: return response

            tokenManager.saveToken(newAccessToken)
            response.close()

            val retried = originalRequest.newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .build()
            return chain.proceed(retried)
        }

        return response
    }

    private fun runRefresh(refreshToken: String): String? {
        return try {
            val client = OkHttpClient()
            val baseUrl = ApiClient.BASE_URL
            val body = JSONObject().put("refreshToken", refreshToken).toString()
                .toRequestBody("application/json".toMediaType())
            val req = Request.Builder()
                .url("${baseUrl}auth/refresh")
                .post(body)
                .build()
            val resp = client.newCall(req).execute()
            if (resp.isSuccessful) {
                val json = JSONObject(resp.body?.string() ?: "")
                json.optString("token").takeIf { it.isNotEmpty() }
            } else null
        } catch (_: Exception) { null }
    }
}
