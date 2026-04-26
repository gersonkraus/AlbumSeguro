package com.familiaaco.network

import android.content.Context
import com.familiaaco.data.local.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // Para testes locais, substitua pelo IP da sua máquina na rede Wi-Fi (ex: "http://192.168.1.100:3000/api/")
    // Para emulador Android: "http://10.0.2.2:3000/api/"
    internal const val BASE_URL = "https://albumseguro-api.onrender.com/api/"

    fun getApiService(context: Context): ApiService {
        val tokenManager = TokenManager(context)

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor(AuthInterceptor(tokenManager))
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
            .create(ApiService::class.java)
    }
}
