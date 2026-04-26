# AlbumSeguro — App de Álbum Seguro para Crianças Acolhidas

Sistema para compartilhamento seguro de fotos e vídeos entre voluntários e crianças acolhidas.

## Características

- Autenticação segura com JWT
- CRUD completo de crianças e mídia
- Upload de fotos e vídeos (Firebase Storage)
- Token de acesso único por criança
- QR Code para acesso ao álbum
- Android 8.0+ (minSdk 26)
- LGPD compliant

## Estrutura

```
AlbumSeguro/
├── backend/    # Node.js + Express + MongoDB
└── android/    # Kotlin + Jetpack Compose
```

## Setup Rápido

### Backend
```bash
cd backend
npm install
cp .env.example .env
# Preencher .env com MongoDB URI, JWT Secret e Firebase credentials
npm run dev
```

### Android
Abrir pasta `android/` no Android Studio e sincronizar o Gradle.

## Documentação

- [Guia de Deployment](docs/DEPLOYMENT_GUIDE.md)
- [Checklist de Testes](docs/CHECKLIST_TESTES.md)

## Segurança

- Senhas hasheadas com bcrypt (salt 10)
- JWT com expiração de 30 dias
- EncryptedSharedPreferences no Android
- Rate limiting: 100 req/15min
- Logs de auditoria completos
- LGPD: dados mínimos, soft-delete

## Variáveis de Ambiente (backend/.env)

| Variável | Descrição |
|---|---|
| `MONGODB_URI` | String de conexão MongoDB |
| `JWT_SECRET` | Chave secreta JWT |
| `FIREBASE_CREDENTIALS` | JSON das credenciais Firebase |
| `FIREBASE_STORAGE_BUCKET` | Bucket do Firebase Storage |
| `ALLOWED_ORIGINS` | Origens CORS permitidas |
