# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**AlbumSeguro** is a secure digital photo/video album system for foster children ("crianças acolhidas"). The app has two user types:
- **Admin / Super Admin**: manages children's profiles and uploads media
- **Child**: accesses their personal album via a unique secure token (or QR code)

The system must be LGPD compliant (Brazilian data protection law).

## Critical: Dual-Environment File Sync

The project lives in **two locations** and both must stay in sync:

| Environment | Path | Usage |
|---|---|---|
| **WSL (Ubuntu)** | `~/AlbumSeguro/` | Primary — edit here |
| **Windows** | `C:\Projetos\AlbumSeguro\` | Android Studio reads from here |

After editing any Android `.kt` file in WSL, copy it to Windows:
```powershell
Copy-Item "\\wsl.localhost\Ubuntu\home\gerson\AlbumSeguro\android\app\src\main\java\com\familiaaco\ui\screens\NomeDoArquivo.kt" `
          -Destination "C:\Projetos\AlbumSeguro\android\app\src\main\java\com\familiaaco\ui\screens\NomeDoArquivo.kt" `
          -Force
```

Backend `.js` files only live in WSL and do not need copying.

## Architecture

```
AlbumSeguro/
├── backend/         Node.js + Express REST API
├── android/         Android app (Kotlin + Jetpack Compose)
├── web/             Public album frontend (React + TypeScript + Vite)
└── docs/            API, DB schema, security, deployment guides
```

### Backend (`backend/`)
- **Runtime**: Node.js, Express 4
- **Database**: MongoDB via Mongoose
- **Auth**: JWT — access token (1h) + refresh token (30d) via `JWT_SECRET` / `JWT_REFRESH_SECRET`; bcryptjs password hashing
- **Storage**: Firebase Storage (via `firebase-admin`) for media files
- **Entry point**: `backend/server.js` → `backend/src/app.js`
- **Routes** under `/api/`: `auth`, `children`, `media`, `admin`, `logs`, `album` (public token access), `public`
- **Middleware**: `helmet`, `express-rate-limit` (100 req/15 min), `cors`, `compression`, `morgan`
- **Media**: `multer` (memoryStorage) for uploads, `qrcode` for token QR generation, `nodemailer` for email

Auth middleware chain:
1. `authMiddleware` — verifies JWT, loads `req.user`
2. `adminMiddleware` — role must be `admin` or `super_admin`
3. `superAdminMiddleware` — role must be `super_admin`
4. `requirePermission(perm)` — checks `req.user.permissoes` array

### Data Models
- **User**: admins/super_admins with `role`, `permissoes[]`, JWT-based auth; soft-deleted via `ativo: false`
- **Child**: foster child profile with unique `tokenAcesso` for album access; hard-deleted
- **Media**: photo/video linked to a child; `privacidade` field controls visibility; hard-deleted
- **Log**: audit trail of all sensitive actions (`LOGIN`, `UPLOAD_MIDIA`, `ACESSAR_ALBUM`, etc.)

### Android (`android/`)
- **Language**: Kotlin + Jetpack Compose + Material 3
- **Package**: `com.familiaaco`
- **Pattern**: MVVM — `Screen → ViewModel → Repository → ApiService (Retrofit)`
- **Network**: Retrofit + OkHttp with `AuthInterceptor` to attach Bearer tokens; auto-refreshes on 401
- **Video**: Media3 ExoPlayer; **Images**: Coil (`AsyncImage`)
- **Local storage**: `TokenManager` (EncryptedSharedPreferences) — stores access token, refresh token, user role
- **`BASE_URL`**: `http://192.168.1.188:3000/api/` (Wi-Fi); use `http://10.0.2.2:3000/api/` for emulator
- **minSdk**: 26 (Android 8.0)

Key screens: `LoginScreen`, `AdminDashboardScreen`, `ChildrenListScreen`, `ChildDetailScreen` (largest — gallery + token + filters), `MediaUploadScreen`, `ChildAlbumScreen`, `TokenInputScreen`, `QRScannerScreen`, `VideoPlayerScreen`

### Web (`web/`)
- **Stack**: React 18, TypeScript, Vite, Tailwind CSS, Axios
- Public album viewer — child enters token, sees their photos/videos

## Backend Commands

```bash
cd backend
npm install          # install dependencies
npm run dev          # start with nodemon (hot reload)
npm start            # production start
npm test             # jest with coverage
npm run test:watch   # jest in watch mode
npm run lint         # eslint src/
npm run lint:fix     # eslint --fix
```

Run a single test file:
```bash
cd backend && npx jest tests/auth.test.js --coverage
```

Syntax-check without running:
```bash
find src/routes src/models src/middleware src/services -name '*.js' | xargs -I{} node --check {}
```

Tests use `mongodb-memory-server` (see `tests/setup.js`) — no real MongoDB needed.

## Web Commands

```bash
cd web
npm install
npm run dev      # Vite dev server
npm run build    # tsc + vite build → dist/
npm run preview  # preview production build
npm run lint     # eslint (zero warnings)
```

## Environment Variables

Copy `backend/.env.example` to `backend/.env`. Required vars:
- `MONGODB_URI` — MongoDB connection string
- `JWT_SECRET` — signs access tokens
- `JWT_REFRESH_SECRET` — signs refresh tokens (must differ from JWT_SECRET)
- `PORT` — defaults to 3000
- `ALLOWED_ORIGINS` — comma-separated CORS origins
- `FIREBASE_CREDENTIALS` — JSON string of Firebase service account
- `FIREBASE_STORAGE_BUCKET` — Firebase Storage bucket name
- `SMTP_*` — optional email config (SMTP_HOST, SMTP_PORT, SMTP_USER, SMTP_PASS)

## Key Conventions

- All API responses use `{ error: "..." }` for errors and consistent JSON shapes
- Mongoose `toJSON()` on User strips `senha` before serializing
- Media `privacidade` values: `'apenas_crianca'` | `'admins_e_crianca'`
- Log `acao` is an enum — always use the defined constants
- Child `tokenAcesso` has a sparse unique index — null is allowed, but tokens must be unique
- Android uses Portuguese field names mirroring the backend DTOs (`nome`, `dataNascimento`, etc.)
- Video navigation args must be encoded: `URLEncoder.encode(url, "UTF-8")` before passing to NavGraph
- ViewModels scoped per entity use `key = "nome_$id"` in the factory
- `@OptIn` for experimental Compose APIs (e.g. `ExperimentalMaterial3Api`) goes at the `@Composable` function level, never inside lambdas
