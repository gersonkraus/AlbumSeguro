# Nicollas Viewer App — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Criar o flavor Android `nicollas` — um app viewer dedicado com token fixo, sem logout, e atualização OTA automática via backend próprio.

**Architecture:** Product flavor `nicollas` no projeto Android existente, com `NiCollasActivity` como launcher (checa versão OTA → baixa e instala automaticamente se houver update → abre `NiCollasAlbumScreen` com token fixo via `BuildConfig.CHILD_TOKEN`). O backend recebe upload do APK novo via endpoint admin e serve a versão atual via endpoint público.

**Tech Stack:** Kotlin + Jetpack Compose + Retrofit + OkHttp + DownloadManager + FileProvider (Android); Node.js + Express + Multer + Mongoose (backend).

---

## Mapa de arquivos

### Backend
| Arquivo | Ação |
|---|---|
| `backend/src/models/AppUpdate.js` | CRIAR — model Mongoose para metadados do APK |
| `backend/src/routes/public.routes.js` | MODIFICAR — adicionar GET `/app-version` e GET `/apk/download` |
| `backend/src/routes/admin.routes.js` | MODIFICAR — adicionar POST `/app-update` (upload APK) |
| `backend/uploads/apk/.gitkeep` | CRIAR — garante que a pasta existe no repo |
| `backend/.gitignore` | MODIFICAR — ignorar `*.apk` dentro de `uploads/apk/` |

### Android
| Arquivo | Ação |
|---|---|
| `android/app/build.gradle.kts` | MODIFICAR — adicionar `productFlavors` + `buildConfig = true` |
| `android/app/src/nicollas/AndroidManifest.xml` | CRIAR — `NiCollasActivity` como launcher, `FileProvider`, `REQUEST_INSTALL_PACKAGES` |
| `android/app/src/nicollas/res/xml/file_provider_paths.xml` | CRIAR — paths do FileProvider para APK de update |
| `android/app/src/nicollas/java/com/familiaaco/ui/screens/NiCollasAlbumScreen.kt` | CRIAR — viewer simplificado (sem logout, sem troca de token) |
| `android/app/src/nicollas/java/com/familiaaco/NiCollasActivity.kt` | CRIAR — launcher com checagem OTA automática |

---

## Task 1: Backend — AppUpdate model

**Files:**
- Create: `backend/src/models/AppUpdate.js`

- [ ] **Step 1: Criar o model**

```javascript
// backend/src/models/AppUpdate.js
const mongoose = require('mongoose');

const appUpdateSchema = new mongoose.Schema(
  {
    versionCode: { type: Number, required: true },
    versionName: { type: String, required: true },
    apkFileName: { type: String, required: true },
    notes: { type: String, default: '' },
  },
  { timestamps: true }
);

module.exports = mongoose.model('AppUpdate', appUpdateSchema);
```

- [ ] **Step 2: Verificar sintaxe**

```bash
cd /home/gerson/AlbumSeguro
node --check backend/src/models/AppUpdate.js
```

Expected: sem output (nenhum erro de sintaxe).

- [ ] **Step 3: Commit**

```bash
git add backend/src/models/AppUpdate.js
git commit -m "feat(backend): adicionar model AppUpdate para OTA"
```

---

## Task 2: Backend — endpoints públicos (versão + download)

**Files:**
- Modify: `backend/src/routes/public.routes.js`

- [ ] **Step 1: Adicionar imports no topo de `public.routes.js`**

Após a linha `const Log = require('../models/Log');`, adicionar:

```javascript
const AppUpdate = require('../models/AppUpdate');
const path = require('path');
const fs = require('fs');
```

- [ ] **Step 2: Adicionar os dois endpoints antes de `module.exports`**

Adicionar imediatamente antes da linha `module.exports = router;`:

```javascript
/**
 * Retorna a versão mais recente do app Nicollas
 * GET /api/public/app-version
 */
router.get('/app-version', async (req, res) => {
  try {
    const latest = await AppUpdate.findOne().sort({ createdAt: -1 });
    if (!latest) {
      return res.status(404).json({ error: 'Nenhuma versão disponível' });
    }
    const downloadUrl = `${req.protocol}://${req.get('host')}/api/public/apk/download`;
    res.json({
      versionCode: latest.versionCode,
      versionName: latest.versionName,
      downloadUrl,
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

/**
 * Serve o APK mais recente para download
 * GET /api/public/apk/download
 */
router.get('/apk/download', async (req, res) => {
  try {
    const latest = await AppUpdate.findOne().sort({ createdAt: -1 });
    if (!latest) {
      return res.status(404).json({ error: 'Nenhum APK disponível' });
    }
    const apkPath = path.join(__dirname, '../../uploads/apk', latest.apkFileName);
    if (!fs.existsSync(apkPath)) {
      return res.status(404).json({ error: 'Arquivo APK não encontrado no servidor' });
    }
    res.setHeader('Content-Disposition', 'attachment; filename="app-nicollas.apk"');
    res.setHeader('Content-Type', 'application/vnd.android.package-archive');
    res.sendFile(path.resolve(apkPath));
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});
```

- [ ] **Step 3: Verificar sintaxe**

```bash
node --check backend/src/routes/public.routes.js
```

Expected: sem output.

- [ ] **Step 4: Commit**

```bash
git add backend/src/routes/public.routes.js
git commit -m "feat(backend): adicionar endpoints públicos app-version e apk/download"
```

---

## Task 3: Backend — endpoint admin de upload do APK

**Files:**
- Modify: `backend/src/routes/admin.routes.js`

- [ ] **Step 1: Adicionar imports no topo de `admin.routes.js`**

Primeiro, atualizar a linha de import do auth middleware para incluir `adminMiddleware` (ela atualmente só tem `authMiddleware` e `superAdminMiddleware`):

```javascript
const { authMiddleware, superAdminMiddleware, adminMiddleware } = require('../middleware/auth');
```

Em seguida, após `const Log = require('../models/Log');`, adicionar:

```javascript
const AppUpdate = require('../models/AppUpdate');
const multer = require('multer');
const path = require('path');
const fs = require('fs');

const apkStorage = multer.diskStorage({
  destination: (req, file, cb) => {
    const dir = path.join(__dirname, '../../uploads/apk');
    fs.mkdirSync(dir, { recursive: true });
    cb(null, dir);
  },
  filename: (req, file, cb) => {
    cb(null, 'app-nicollas.apk');
  },
});
const uploadApk = multer({
  storage: apkStorage,
  limits: { fileSize: 100 * 1024 * 1024 },
  fileFilter: (req, file, cb) => {
    if (file.mimetype === 'application/vnd.android.package-archive' ||
        file.originalname.endsWith('.apk')) {
      cb(null, true);
    } else {
      cb(new Error('Apenas arquivos .apk são permitidos'));
    }
  },
});
```

- [ ] **Step 2: Adicionar o endpoint admin antes de `module.exports`**

Adicionar antes da última linha do arquivo (antes de `module.exports = router;`):

```javascript
/**
 * Upload de nova versão do APK do Nicollas
 * POST /api/admin/app-update
 */
router.post(
  '/app-update',
  authMiddleware,
  adminMiddleware,
  uploadApk.single('apk'),
  async (req, res) => {
    try {
      if (!req.file) {
        return res.status(400).json({ error: 'Arquivo APK é obrigatório' });
      }
      const versionCode = parseInt(req.body.versionCode, 10);
      const versionName = req.body.versionName?.trim();
      const notes = req.body.notes?.trim() || '';

      if (!versionCode || versionCode < 1) {
        return res.status(400).json({ error: 'versionCode deve ser um número inteiro positivo' });
      }
      if (!versionName) {
        return res.status(400).json({ error: 'versionName é obrigatório' });
      }

      const appUpdate = await AppUpdate.create({
        versionCode,
        versionName,
        apkFileName: req.file.filename,
        notes,
      });

      await Log.create({
        usuarioId: req.user._id,
        acao: 'UPLOAD_MIDIA',
        recursoId: appUpdate._id,
        status: 'sucesso',
        ipAddress: req.ip,
        userAgent: req.headers['user-agent'],
      });

      res.status(201).json({
        message: 'APK enviado com sucesso',
        versionCode: appUpdate.versionCode,
        versionName: appUpdate.versionName,
      });
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  }
);
```

- [ ] **Step 3: Verificar sintaxe**

```bash
node --check backend/src/routes/admin.routes.js
```

Expected: sem output.

- [ ] **Step 4: Commit**

```bash
git add backend/src/routes/admin.routes.js
git commit -m "feat(backend): adicionar endpoint admin para upload de APK OTA"
```

---

## Task 4: Backend — diretório uploads/apk e .gitignore

**Files:**
- Create: `backend/uploads/apk/.gitkeep`
- Modify: `backend/.gitignore`

- [ ] **Step 1: Criar diretório e .gitkeep**

```bash
mkdir -p /home/gerson/AlbumSeguro/backend/uploads/apk
touch /home/gerson/AlbumSeguro/backend/uploads/apk/.gitkeep
```

- [ ] **Step 2: Adicionar .gitignore para ignorar APKs (mas rastrear a pasta)**

Verificar se existe `backend/.gitignore`:

```bash
cat /home/gerson/AlbumSeguro/backend/.gitignore 2>/dev/null || echo "(não existe)"
```

Se não existir, criar. Se existir, adicionar no final:

```
# APK uploads (arquivos grandes, não versionar)
uploads/apk/*.apk
```

- [ ] **Step 3: Iniciar o backend e testar os endpoints**

```bash
cd /home/gerson/AlbumSeguro/backend
npm run dev &
sleep 3

# Testar app-version sem nenhum registro (deve retornar 404)
curl -s http://localhost:3000/api/public/app-version | python3 -m json.tool
```

Expected: `{"error": "Nenhuma versão disponível"}`

- [ ] **Step 4: Parar o backend**

```bash
kill %1 2>/dev/null || pkill -f "node.*server.js"
```

- [ ] **Step 5: Commit**

```bash
git add backend/uploads/apk/.gitkeep backend/.gitignore 2>/dev/null || git add backend/uploads/apk/.gitkeep
git commit -m "chore(backend): criar diretório uploads/apk e gitignore para APKs"
```

---

## Task 5: Android — product flavors no build.gradle.kts

**Files:**
- Modify: `android/app/build.gradle.kts`

**IMPORTANTE:** Antes de continuar, obtenha o token do Nicollas no backend:
```bash
# Se o token do Nicollas ainda não existe, gere via API:
# POST /api/children/:id/gerar-token com diasValidade omitido (token permanente = sem expiração no backend)
# Anote o token retornado — ele vai no buildConfigField abaixo.
```

- [ ] **Step 1: Adicionar `buildConfig = true` e `flavorDimensions` + `productFlavors`**

No arquivo `android/app/build.gradle.kts`, fazer as seguintes alterações:

**1a)** No bloco `buildFeatures { ... }`, adicionar `buildConfig = true`:

```kotlin
buildFeatures {
    compose = true
    buildConfig = true
}
```

**1b)** Após o bloco `buildTypes { ... }` e antes de `compileOptions { ... }`, adicionar:

```kotlin
flavorDimensions += "target"
productFlavors {
    create("full") {
        dimension = "target"
        applicationId = "com.familiaaco"
    }
    create("nicollas") {
        dimension = "target"
        applicationId = "com.familiaaco.nicollas"
        versionCode = 1
        versionName = "1.0.0"
        buildConfigField("String", "CHILD_TOKEN", "\"COLE_O_TOKEN_AQUI\"")
    }
}
```

Substituir `COLE_O_TOKEN_AQUI` pelo token real do Nicollas (32 caracteres hex maiúsculos).

- [ ] **Step 2: Verificar que o projeto sincroniza (no terminal WSL, checar sintaxe do Gradle)**

```bash
cd /home/gerson/AlbumSeguro/android
./gradlew tasks --quiet 2>&1 | head -20
```

Expected: lista de tasks sem erros de configuração.

- [ ] **Step 3: Commit**

```bash
git add android/app/build.gradle.kts
git commit -m "feat(android): adicionar product flavors full e nicollas"
```

---

## Task 6: Android — AndroidManifest e file_provider_paths do flavor nicollas

**Files:**
- Create: `android/app/src/nicollas/AndroidManifest.xml`
- Create: `android/app/src/nicollas/res/xml/file_provider_paths.xml`

- [ ] **Step 1: Criar diretórios**

```bash
mkdir -p /home/gerson/AlbumSeguro/android/app/src/nicollas/res/xml
mkdir -p /home/gerson/AlbumSeguro/android/app/src/nicollas/java/com/familiaaco/ui/screens
mkdir -p /home/gerson/AlbumSeguro/android/app/src/nicollas/java/com/familiaaco
```

- [ ] **Step 2: Criar `file_provider_paths.xml`**

```xml
<!-- android/app/src/nicollas/res/xml/file_provider_paths.xml -->
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <external-files-path name="apk_updates" path="update/" />
</paths>
```

- [ ] **Step 3: Criar `AndroidManifest.xml` do flavor nicollas**

```xml
<!-- android/app/src/nicollas/AndroidManifest.xml -->
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />

    <application>

        <!-- Remove MainActivity como launcher neste flavor -->
        <activity
            android:name=".MainActivity"
            android:exported="false">
            <intent-filter tools:node="removeAll" />
        </activity>

        <!-- NiCollasActivity é o launcher exclusivo deste flavor -->
        <activity
            android:name=".NiCollasActivity"
            android:exported="true"
            android:screenOrientation="portrait">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- FileProvider para compartilhar APK baixado com o instalador do sistema -->
        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.provider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_provider_paths" />
        </provider>

    </application>
</manifest>
```

- [ ] **Step 4: Commit**

```bash
git add android/app/src/nicollas/
git commit -m "feat(android/nicollas): adicionar manifest e FileProvider do flavor nicollas"
```

---

## Task 7: Android — NiCollasAlbumScreen.kt

**Files:**
- Create: `android/app/src/nicollas/java/com/familiaaco/ui/screens/NiCollasAlbumScreen.kt`

Esta tela é baseada em `ChildAlbumScreen` com remoção do botão de logout, botão de admin e da lógica de salvar token (desnecessária com token fixo).

- [ ] **Step 1: Criar o arquivo**

```kotlin
// android/app/src/nicollas/java/com/familiaaco/ui/screens/NiCollasAlbumScreen.kt
package com.familiaaco.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.familiaaco.ui.theme.PrimaryColor
import com.familiaaco.viewmodel.ChildAlbumViewModel

@Composable
fun NiCollasAlbumScreen(navController: NavController, token: String) {
    val context = LocalContext.current
    val viewModel: ChildAlbumViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                ChildAlbumViewModel(context) as T
        }
    )
    val albumState by viewModel.albumState.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val gridState = rememberLazyGridState()

    LaunchedEffect(token) { viewModel.carregarAlbum(token) }
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .collect { lastIndex ->
                val total = gridState.layoutInfo.totalItemsCount
                if (lastIndex >= total - 6 && hasMore && !isLoadingMore) {
                    viewModel.carregarMais()
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Meu Álbum",
                style = MaterialTheme.typography.headlineSmall,
                color = PrimaryColor
            )
        }

        when (albumState) {
            is ChildAlbumViewModel.AlbumState.Loading ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

            is ChildAlbumViewModel.AlbumState.Success -> {
                val midias = (albumState as ChildAlbumViewModel.AlbumState.Success).midias
                if (midias.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Nenhuma foto ou vídeo ainda.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        state = gridState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(midias) { midia ->
                            MidiaGridItem(midia = midia, onClick = {
                                val idx = midias.indexOf(midia).coerceAtLeast(0)
                                MediaViewerArgs.midias = midias
                                MediaViewerArgs.startIndex = idx
                                navController.navigate("media_viewer")
                            })
                        }
                        if (isLoadingMore) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            }

            is ChildAlbumViewModel.AlbumState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (albumState as ChildAlbumViewModel.AlbumState.Error).message,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.carregarAlbum(token) }) {
                            Text("Tentar Novamente")
                        }
                    }
                }
            }

            else -> {}
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add android/app/src/nicollas/java/com/familiaaco/ui/screens/NiCollasAlbumScreen.kt
git commit -m "feat(android/nicollas): adicionar NiCollasAlbumScreen (viewer simplificado)"
```

---

## Task 8: Android — NiCollasActivity.kt

**Files:**
- Create: `android/app/src/nicollas/java/com/familiaaco/NiCollasActivity.kt`

Esta Activity é o launcher do flavor nicollas. Ao criar, checa a versão OTA no servidor; se houver update, baixa e instala automaticamente; caso contrário, abre o álbum.

- [ ] **Step 1: Criar o arquivo**

```kotlin
// android/app/src/nicollas/java/com/familiaaco/NiCollasActivity.kt
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
            // Se hasUpdate == true, o BroadcastReceiver cuidará de instalar e depois
            // o usuário reinicia o app na nova versão automaticamente.
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
```

- [ ] **Step 2: Commit**

```bash
git add android/app/src/nicollas/java/com/familiaaco/NiCollasActivity.kt
git commit -m "feat(android/nicollas): adicionar NiCollasActivity com checagem OTA automática"
```

---

## Task 9: Gerar e verificar o APK do flavor nicollas

**Prerequisito:** O token do Nicollas no backend deve estar com `tokenExpiracao: null` (sem expiração). Verificar no banco ou regenerar via `POST /api/children/:id/gerar-token` com `diasValidade` não enviado — isso cria um token sem expiração. Anotar o token e colocá-lo em `buildConfigField("String", "CHILD_TOKEN", "\"TOKEN_AQUI\"")` na Task 5.

- [ ] **Step 1: Copiar arquivos android para o Windows (conforme CLAUDE.md)**

```bash
# Copiar todos os novos arquivos do flavor nicollas
cp /home/gerson/AlbumSeguro/android/app/build.gradle.kts \
   /mnt/c/Projetos/AlbumSeguro/android/app/build.gradle.kts

cp /home/gerson/AlbumSeguro/android/app/src/nicollas/AndroidManifest.xml \
   /mnt/c/Projetos/AlbumSeguro/android/app/src/nicollas/AndroidManifest.xml 2>/dev/null || \
   mkdir -p /mnt/c/Projetos/AlbumSeguro/android/app/src/nicollas && \
   cp -r /home/gerson/AlbumSeguro/android/app/src/nicollas \
         /mnt/c/Projetos/AlbumSeguro/android/app/src/

cp /mnt/c... # (ajustar paths conforme necessário)
```

Alternativa mais simples — copiar o diretório inteiro:
```powershell
# Rodar no PowerShell Windows:
Copy-Item "\\wsl.localhost\Ubuntu\home\gerson\AlbumSeguro\android\app\build.gradle.kts" `
          -Destination "C:\Projetos\AlbumSeguro\android\app\build.gradle.kts" -Force

Copy-Item "\\wsl.localhost\Ubuntu\home\gerson\AlbumSeguro\android\app\src\nicollas" `
          -Destination "C:\Projetos\AlbumSeguro\android\app\src\nicollas" `
          -Recurse -Force
```

- [ ] **Step 2: No Android Studio (Windows), sincronizar o Gradle**

Abrir o projeto em `C:\Projetos\AlbumSeguro\android\` e clicar em **Sync Project with Gradle Files**.

Verificar que aparecem as variants: `fullDebug`, `fullRelease`, `niCollasDebug`, `niCollasRelease`.

- [ ] **Step 3: Gerar o APK de release do Nicollas**

No Android Studio: **Build → Generate Signed APK** → selecionar variant `niCollasRelease`.

Ou via terminal WSL (se o Gradle estiver configurado no WSL):
```bash
cd /home/gerson/AlbumSeguro/android
./gradlew assembleNiCollasRelease 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL` e APK em `app/build/outputs/apk/nicollas/release/app-nicollas-release.apk`.

- [ ] **Step 4: Verificar applicationId do APK gerado**

```bash
# No WSL, verificar o applicationId do APK (requer aapt2 ou aapt no PATH)
aapt2 dump badging \
  /home/gerson/AlbumSeguro/android/app/build/outputs/apk/nicollas/release/app-nicollas-release.apk \
  | grep "package:"
```

Expected: `package: name='com.familiaaco.nicollas'`

- [ ] **Step 5: Commit final**

```bash
git add -A
git commit -m "feat: flavor nicollas completo — viewer dedicado com OTA self-hosted"
```

---

## Task 10: Verificação end-to-end

- [ ] **Step 1: Testar endpoints backend com backend rodando**

```bash
cd /home/gerson/AlbumSeguro/backend && npm run dev &
sleep 3

# 1. app-version sem APK cadastrado (404 esperado)
curl -s http://localhost:3000/api/public/app-version

# 2. Fazer upload de um APK de teste (usar o APK gerado na Task 9)
# Obter token admin primeiro:
TOKEN=$(curl -s -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@email.com","senha":"senha123"}' | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")

# Upload do APK
curl -s -X POST http://localhost:3000/api/admin/app-update \
  -H "Authorization: Bearer $TOKEN" \
  -F "apk=@/home/gerson/AlbumSeguro/android/app/build/outputs/apk/nicollas/release/app-nicollas-release.apk" \
  -F "versionCode=1" \
  -F "versionName=1.0.0" \
  -F "notes=Versão inicial"

# 3. app-version após upload (deve retornar versionCode 1)
curl -s http://localhost:3000/api/public/app-version | python3 -m json.tool

# 4. Download do APK (verificar que o arquivo é retornado)
curl -I http://localhost:3000/api/public/apk/download
```

Expected em (3): `{"versionCode": 1, "versionName": "1.0.0", "downloadUrl": "..."}`.
Expected em (4): `Content-Type: application/vnd.android.package-archive`, `Content-Disposition: attachment; filename="app-nicollas.apk"`.

- [ ] **Step 2: Instalar o APK no dispositivo e verificar comportamento**

```bash
adb install /home/gerson/AlbumSeguro/android/app/build/outputs/apk/nicollas/release/app-nicollas-release.apk
```

Verificar manualmente:
1. App abre direto no álbum do Nicollas (sem tela de input de token).
2. Não há botão de logout.
3. Fotos e vídeos carregam corretamente.
4. Toque em vídeo abre o player.

- [ ] **Step 3: Testar OTA — upload de versão 2**

Compilar o APK com `versionCode = 2` no `build.gradle.kts` do flavor nicollas, fazer upload via:

```bash
curl -s -X POST http://localhost:3000/api/admin/app-update \
  -H "Authorization: Bearer $TOKEN" \
  -F "apk=@app-nicollas-v2-release.apk" \
  -F "versionCode=2" \
  -F "versionName=1.1.0" \
  -F "notes=Teste de OTA"
```

Depois reiniciar o app (versionCode 1) no dispositivo e verificar:
- Download inicia automaticamente (notificação no Android).
- Diálogo de instalação do sistema aparece ao concluir.
- Após confirmar instalação, app atualiza para v2.

- [ ] **Step 4: Parar backend**

```bash
kill %1 2>/dev/null || pkill -f "node.*server.js"
```

---

## Notas de Deployment

- O diretório `uploads/apk/` deve existir no servidor de produção. No Render.com, o filesystem é efêmero — o APK é perdido a cada deploy. **Recomendação:** migrar o armazenamento do APK para o Firebase Storage (já usado para mídias) antes de ir para produção, servindo a URL do Firebase no `downloadUrl`.
- Para builds de produção, configurar um `keystore` de assinatura dedicado para o flavor `nicollas` em vez de `signingConfig = signingConfigs.getByName("debug")`.
- O `CHILD_TOKEN` hardcoded no `BuildConfig` é seguro para uso em sideload desde que o APK não seja distribuído publicamente.
