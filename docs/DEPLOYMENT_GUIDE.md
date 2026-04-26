# Guia de Deployment

## Backend (Node.js)

### Opção 1: Railway.app
```bash
npm install -g railway
railway login
railway init
railway up
```

### Opção 2: Render.com
1. Push para GitHub
2. Conectar repositório no Render
3. Auto-deploy em cada push para main

### Opção 3: Firebase Functions
```bash
npm install -g firebase-tools
firebase login
firebase init functions
firebase deploy --only functions
```

### Variáveis de Ambiente (produção)
```env
MONGODB_URI=mongodb+srv://user:pass@cluster.mongodb.net/albumseguro
JWT_SECRET=chave-super-secreta-producao
FIREBASE_CREDENTIALS={"type":"service_account","project_id":"..."}
FIREBASE_STORAGE_BUCKET=seu-projeto.appspot.com
NODE_ENV=production
ALLOWED_ORIGINS=https://seu-dominio.com
```

**Atenção:** Nunca commitar o `.env` com dados reais.

## Android App

### Build de Debug (para testes)
```bash
cd android
./gradlew assembleDebug
# APK em: app/build/outputs/apk/debug/app-debug.apk
```

### Instalar no emulador/dispositivo
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Build de Release
```bash
./gradlew bundleRelease
# AAB em: app/build/outputs/bundle/release/app-release.aab
```

### Antes do deploy em produção
- Atualizar `BASE_URL` em `ApiClient.kt` para a URL do servidor
- Alterar `android:usesCleartextTraffic="false"` no AndroidManifest.xml
- Assinar o APK/AAB com keystore privada

## MongoDB

Usar MongoDB Atlas (gratuito até 512MB):
1. Criar conta em mongodb.com/atlas
2. Criar cluster gratuito
3. Obter connection string
4. Adicionar IP do servidor na allowlist
