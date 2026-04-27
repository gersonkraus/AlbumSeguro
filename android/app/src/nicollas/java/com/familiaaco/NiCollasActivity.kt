package com.familiaaco

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.familiaaco.network.ApiClient
import com.familiaaco.ui.screens.MediaViewerScreen
import com.familiaaco.ui.screens.NiCollasAlbumScreen
import com.familiaaco.ui.screens.VideoPlayerScreen
import com.familiaaco.ui.theme.FamiliaAcolhedoraTheme
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.io.File
import java.util.concurrent.TimeUnit

private interface NiCollasVersionApi {
    @GET("public/app-version")
    suspend fun getAppVersion(): AppVersionResponse
}

private data class AppVersionResponse(
    @SerializedName("versionCode") val versionCode: Int,
    @SerializedName("versionName") val versionName: String,
    @SerializedName("downloadUrl") val downloadUrl: String,
)

class NiCollasActivity : ComponentActivity() {

    private var downloadId: Long = -1L
    private var receiverRegistered = false

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            if (id == downloadId) {
                if (receiverRegistered) {
                    unregisterReceiver(this)
                    receiverRegistered = false
                }
                installApk()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupCoil()
        checkForUpdate()
    }

    private fun setupCoil() {
        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .memoryCache { MemoryCache.Builder(this).maxSizePercent(0.25).build() }
                .diskCache {
                    DiskCache.Builder()
                        .directory(cacheDir.resolve("coil"))
                        .maxSizeBytes(100L * 1024 * 1024)
                        .build()
                }
                .crossfade(300)
                .build()
        )
    }

    private fun checkForUpdate() {
        lifecycleScope.launch {
            val hasUpdate = withContext(Dispatchers.IO) {
                try {
                    val api = Retrofit.Builder()
                        .baseUrl(ApiClient.BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(
                            OkHttpClient.Builder()
                                .connectTimeout(10, TimeUnit.SECONDS)
                                .readTimeout(10, TimeUnit.SECONDS)
                                .build()
                        )
                        .build()
                        .create(NiCollasVersionApi::class.java)
                    val response = api.getAppVersion()
                    if (response.versionCode > BuildConfig.VERSION_CODE) {
                        startDownload(response.downloadUrl)
                        true
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    false
                }
            }
            if (!hasUpdate) {
                openAlbum()
            }
        }
    }

    private fun startDownload(url: String) {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Atualizando Meu Álbum")
            .setDescription("Baixando nova versão...")
            .setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            .setDestinationInExternalFilesDir(this, null, "update/app-nicollas.apk")
        val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadId = dm.enqueue(request)
        registerReceiver(
            downloadReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )
        receiverRegistered = true
    }

    private fun installApk() {
        val apkFile = File(getExternalFilesDir(null), "update/app-nicollas.apk")
        if (!apkFile.exists()) {
            openAlbum()
            return
        }
        val apkUri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            apkFile
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        startActivity(intent)
    }

    private fun openAlbum() {
        setContent {
            FamiliaAcolhedoraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "nicollas_album"
                    ) {
                        composable("nicollas_album") {
                            NiCollasAlbumScreen(navController, BuildConfig.CHILD_TOKEN)
                        }
                        composable("media_viewer") {
                            MediaViewerScreen(navController)
                        }
                        composable("video_player/{url}") { backStackEntry ->
                            val encoded = backStackEntry.arguments?.getString("url")
                            val url = encoded?.let {
                                try {
                                    String(
                                        android.util.Base64.decode(
                                            it,
                                            android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP
                                        ),
                                        Charsets.UTF_8
                                    )
                                } catch (_: Exception) { null }
                            }
                            VideoPlayerScreen(navController, url)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (receiverRegistered) {
            unregisterReceiver(downloadReceiver)
            receiverRegistered = false
        }
    }
}
