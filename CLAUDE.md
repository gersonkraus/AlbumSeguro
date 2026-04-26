# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**AlbumSeguro** is a secure digital photo/video album system for foster children ("crianças acolhidas"). The app has two user types:
- **Admin / Super Admin**: manages children's profiles and uploads media
- **Child**: accesses their personal album via a unique secure token (or QR code)

The system must be LGPD compliant (Brazilian data protection law).

## Architecture

```
AlbumSeguro/
├── backend/         Node.js + Express REST API
├── android/         Android app (Kotlin + Jetpack Compose)
└── docs/            API, DB schema, security, deployment guides
```

### Backend (`backend/`)
- **Runtime**: Node.js, Express 4
- **Database**: MongoDB via Mongoose
- **Auth**: JWT (30-day expiry) + bcryptjs password hashing
- **Storage**: Firebase Storage (via `firebase-admin`) for media files
- **Entry point**: `backend/server.js` → `backend/src/app.js`
- **Routes** under `/api/`: `auth`, `children`, `media`, `admin`, `logs`
- **Middleware**: `helmet`, `express-rate-limit` (100 req/15 min), `cors`, `compression`, `morgan`
- **Media**: `multer` for uploads, `qrcode` for token QR generation, `nodemailer` for email

Three auth levels enforced via middleware chain:
1. `authMiddleware` — verifies JWT, loads `req.user`
2. `adminMiddleware` — role must be `admin` or `super_admin`
3. `superAdminMiddleware` — role must be `super_admin`
4. `requirePermission(perm)` — checks `req.user.permissoes` array

### Data Models
- **User**: admins/super_admins with `role`, `permissoes[]`, JWT-based auth
- **Child**: foster child profile with unique `tokenAcesso` for album access
- **Media**: photo/video linked to a child; `privacidade` field controls visibility
- **Log**: audit trail of all sensitive actions (`LOGIN`, `UPLOAD_MIDIA`, `ACESSAR_ALBUM`, etc.)

### Android (`android/`)
- **Language**: Kotlin + Jetpack Compose
- **Package**: `com.familiaaco`
- **Pattern**: MVVM — `Screen → ViewModel → Repository → ApiService (Retrofit)`
- **Network**: Retrofit + OkHttp with `AuthInterceptor` to attach Bearer tokens
- **Local storage**: `PreferencesManager` + `TokenStore` (EncryptedSharedPreferences)

Key screens: `LoginScreen`, `AdminDashboardScreen`, `ChildrenListScreen`, `MediaUploadScreen`, `ChildAlbumScreen`, `TokenInputScreen`, `QRScannerScreen`

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

## Environment Variables

Copy `backend/.env.example` to `backend/.env`. Required vars:
- `MONGODB_URI` — MongoDB connection string
- `JWT_SECRET` — secret for signing JWTs
- `PORT` — defaults to 3000
- `ALLOWED_ORIGINS` — comma-separated CORS origins
- Firebase service account credentials

## Key Conventions

- All API responses use `{ error: "..." }` for errors and consistent JSON shapes
- Mongoose `toJSON()` on User strips `senha` before serializing
- Media `privacidade` values: `'apenas_crianca'` | `'admins_e_crianca'`
- Log `acao` is an enum — always use the defined constants
- Child `tokenAcesso` has a sparse unique index — null is allowed, but tokens must be unique
- Android uses Portuguese field names mirroring the backend DTOs (`nome`, `dataNascimento`, etc.)
