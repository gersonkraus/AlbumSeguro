# Design: App Viewer Exclusivo — Nicollas

**Data:** 2026-04-27
**Status:** Aprovado

---

## Contexto

O AlbumSeguro precisa de uma versão dedicada do app Android para a criança Nicollas. O objetivo é um app de visualização simplificado, instalado por sideload (sem Google Play), com token de acesso fixo e suporte a atualizações remotas via backend próprio.

---

## Escopo

### O que está incluído
- Product flavor `nicollas` no projeto Android existente
- `NiCollasActivity` como launcher exclusivo do flavor
- `NiCollasAlbumScreen` — visualizador simplificado (sem logout, sem troca de token)
- Mecanismo OTA self-hosted: backend serve versão + APK, app baixa e instala automaticamente
- Endpoints backend para upload de APK (admin) e consulta de versão (público)

### O que não está incluído
- Mudanças no app admin (`full` flavor)
- Autenticação admin para o Nicollas
- Push notifications para avisar sobre updates

---

## Arquitetura Android

### Product Flavor

`android/app/build.gradle.kts` — adicionar `productFlavors`:

```kotlin
flavorDimensions += "target"
productFlavors {
    create("full") {
        dimension = "target"
        applicationId = "com.familiaaco"
        // app admin atual — adicionar flavor não muda o comportamento existente
    }
    create("nicollas") {
        dimension = "target"
        applicationId = "com.familiaaco.nicollas"
        versionCode = 1
        versionName = "1.0.0"
        buildConfigField("String", "CHILD_TOKEN", "\"<TOKEN_DO_NICOLLAS>\"")
    }
}
```

O token do Nicollas no backend terá `tokenExpiracao: null` (sem expiração).

### Novos arquivos (source set `src/nicollas/`)

| Arquivo | Responsabilidade |
|---|---|
| `AndroidManifest.xml` | Define `NiCollasActivity` como launcher; adiciona `REQUEST_INSTALL_PACKAGES`; declara `FileProvider` para compartilhar APK com o instalador do sistema |
| `NiCollasActivity.kt` | Entry point: checa versão OTA → navega para `NiCollasAlbumScreen` |
| `NiCollasAlbumScreen.kt` | `ChildAlbumScreen` sem botão de logout, sem troca de token, sem acesso admin |

### Fluxo de inicialização

```
NiCollasActivity.onCreate()
    → GET /api/public/app-version
    → versionCode remoto > BuildConfig.VERSION_CODE?
        SIM → DownloadManager baixa APK automaticamente
              → ao concluir → PackageInstaller disparado automaticamente
              → sistema Android exibe diálogo "Instalar?" (obrigatório, segurança do SO)
        NÃO → NavHost → NiCollasAlbumScreen(token = BuildConfig.CHILD_TOKEN)
```

**Observação sobre FileProvider:** o APK baixado fica no diretório de cache do app. Para passá-lo ao `PackageInstaller`, é obrigatório usar `FileProvider` (Android 7+) para gerar um `content://` URI — URI do tipo `file://` é bloqueado pelo sistema.

**Observação sobre permissão:** Na primeira instalação o Android pede permissão para instalar de fontes externas. Após concedida, as atualizações seguintes só exibem o diálogo de sistema "Instalar?" — sem interação extra no app.

### NiCollasAlbumScreen

Baseada em `ChildAlbumScreen`, com as seguintes remoções:
- Botão de logout / `clearChildToken()`
- Botão de troca de token / navegação para `TokenInputScreen`
- Qualquer referência a telas admin

O token é passado diretamente: `NiCollasAlbumScreen(token = BuildConfig.CHILD_TOKEN)`. Reutiliza `ChildAlbumViewModel` e `MediaRepository` sem alterações.

---

## Backend — Mecanismo OTA

### Model: `AppUpdate`

`backend/src/models/AppUpdate.js`

```javascript
{
  versionCode: { type: Number, required: true },
  versionName: { type: String, required: true },
  apkFileName: { type: String, required: true },  // nome do arquivo em uploads/apk/
  notes: { type: String, default: "" },
  uploadedAt: { type: Date, default: Date.now }
}
```

Apenas o registro mais recente é o "atual" (consultado via `findOne().sort({ uploadedAt: -1 })`).

### Endpoints

| Método | Rota | Auth | Descrição |
|---|---|---|---|
| `POST` | `/api/admin/app-update` | admin | Upload do APK + versionCode + versionName |
| `GET` | `/api/public/app-version` | público | Retorna versão atual + downloadUrl |
| `GET` | `/api/public/apk/download` | público | Serve o arquivo APK para download |

#### POST `/api/admin/app-update`
- `multipart/form-data`: campo `apk` (arquivo) + `versionCode` (number) + `versionName` (string) + `notes` (string, opcional)
- Salva o APK em `uploads/apk/app-nicollas-<versionCode>.apk`
- Cria novo documento `AppUpdate` no banco
- Remove o APK anterior do disco (mantém só o mais recente)

#### GET `/api/public/app-version`
```json
{
  "versionCode": 2,
  "versionName": "1.1.0",
  "downloadUrl": "https://<servidor>/api/public/apk/download"
}
```

#### GET `/api/public/apk/download`
- Busca o `apkFileName` mais recente no banco
- Serve o arquivo com `Content-Disposition: attachment; filename="app-nicollas.apk"`

### Arquivos backend a criar/modificar

| Arquivo | Ação |
|---|---|
| `src/models/AppUpdate.js` | Criar |
| `src/routes/appUpdate.routes.js` | Criar (3 endpoints acima) |
| `src/app.js` | Registrar as novas rotas |
| `uploads/apk/` | Criar pasta (gitignored) |

---

## Geração e distribuição do APK

Para gerar o APK do Nicollas:
```bash
cd android
./gradlew assembleNiCollasRelease
# APK em: app/build/outputs/apk/nicollas/release/app-nicollas-release.apk
```

Instalação inicial no dispositivo:
```bash
adb install app-nicollas-release.apk
# ou enviar o arquivo e instalar manualmente
```

Atualizações subsequentes: admin faz upload via `POST /api/admin/app-update`.

---

## Verificação / Testes

1. **Flavor**: build `assembleNiCollasRelease` deve gerar APK com `applicationId = com.familiaaco.nicollas`
2. **Token fixo**: app abre direto na galeria do Nicollas sem tela de input
3. **Sem logout**: não existe botão de logout na `NiCollasAlbumScreen`
4. **OTA — sem update**: versionCode local == remoto → app abre normalmente
5. **OTA — com update**: versionCode remoto > local → download automático → diálogo de instalação do sistema → após instalação, app na nova versão
6. **Backend upload**: `POST /api/admin/app-update` com APK válido retorna 201; `GET /api/public/app-version` retorna o novo versionCode
7. **Token sem expiração**: token do Nicollas no banco com `tokenExpiracao: null` → acesso ao álbum não é negado
