# AGENTS.md — Guia para Agentes de IA

Este arquivo descreve as especificações completas do projeto **AlbumSeguro** para que agentes de IA possam entender a estrutura, regras e convenções antes de fazer qualquer modificação.

---

## Visão Geral

**AlbumSeguro** é um sistema de álbum digital seguro para crianças em situação de acolhimento familiar. Permite que admins (voluntários/cuidadores) façam upload de fotos e vídeos, e que cada criança acesse seu álbum pessoal através de um token único — sem precisar de cadastro ou senha.

**Conformidade:** LGPD (Lei Geral de Proteção de Dados Pessoais, Brasil).

---

## ⚠️ Regra Crítica — Dois Ambientes de Arquivos

O projeto existe em **dois locais** no mesmo computador e ambos precisam ser mantidos sincronizados:

| Ambiente | Caminho | Uso |
|---|---|---|
| **WSL (Ubuntu)** | `\\wsl.localhost\Ubuntu\home\gerson\AlbumSeguro\` | Fonte primária — editar aqui |
| **Windows** | `C:\Projetos\AlbumSeguro\` | Android Studio lê daqui |

**Regra obrigatória:** sempre que editar arquivos Android (`.kt`), copiar para o Windows logo depois:

```powershell
Copy-Item "\\wsl.localhost\Ubuntu\home\gerson\AlbumSeguro\android\app\src\main\java\com\familiaaco\ui\screens\NomeDoArquivo.kt" `
          -Destination "C:\Projetos\AlbumSeguro\android\app\src\main\java\com\familiaaco\ui\screens\NomeDoArquivo.kt" `
          -Force
```

Arquivos de backend (`.js`) só existem no WSL e não precisam ser copiados.

---

## Estrutura do Projeto

```
AlbumSeguro/
├── backend/                   # Node.js + Express REST API
│   ├── server.js              # Entry point
│   └── src/
│       ├── app.js             # Express app config, rotas, middlewares
│       ├── models/            # Mongoose schemas
│       │   ├── User.js        # Admins e super_admins
│       │   ├── Child.js       # Perfil da criança
│       │   ├── Media.js       # Fotos e vídeos
│       │   └── Log.js         # Auditoria
│       ├── routes/            # Rotas da API
│       │   ├── auth.routes.js
│       │   ├── children.routes.js
│       │   ├── media.routes.js
│       │   ├── admin.routes.js
│       │   ├── logs.routes.js
│       │   ├── album.routes.js    # Acesso público via token
│       │   └── public.routes.js
│       ├── middleware/
│       │   ├── auth.js        # authMiddleware, adminMiddleware, superAdminMiddleware
│       │   └── errorHandler.js
│       ├── services/
│       │   └── storageService.js  # Firebase Storage (upload/delete)
│       └── utils/
│           └── helpers.js     # gerarTokenAcesso, etc.
│
├── android/                   # App Android (Kotlin + Jetpack Compose)
│   └── app/src/main/java/com/familiaaco/
│       ├── MainActivity.kt
│       ├── data/
│       │   ├── local/
│       │   │   └── TokenManager.kt        # EncryptedSharedPreferences
│       │   └── models/
│       │       └── DTOs.kt                # Todos os data classes
│       ├── network/
│       │   ├── ApiService.kt              # Interface Retrofit
│       │   ├── ApiClient.kt               # OkHttpClient + Retrofit builder
│       │   └── AuthInterceptor.kt         # Bearer token + refresh automático
│       ├── repository/                    # Chamadas à API
│       │   ├── AuthRepository.kt
│       │   ├── ChildrenRepository.kt
│       │   ├── MediaRepository.kt
│       │   ├── AdminRepository.kt
│       │   ├── ProfileRepository.kt
│       │   └── LogsRepository.kt
│       ├── viewmodel/                     # MVVM ViewModels
│       │   ├── LoginViewModel.kt
│       │   ├── ChildrenViewModel.kt
│       │   ├── MediaViewModel.kt
│       │   ├── ProfileViewModel.kt
│       │   └── LogsViewModel.kt
│       └── ui/
│           ├── navigation/
│           │   └── NavGraph.kt            # Todas as rotas de navegação
│           ├── screens/
│           │   ├── LoginScreen.kt
│           │   ├── AdminDashboardScreen.kt
│           │   ├── ChildrenListScreen.kt
│           │   ├── CreateChildScreen.kt
│           │   ├── ChildDetailScreen.kt   # Maior tela — galeria + token + filtros
│           │   ├── MediaUploadScreen.kt
│           │   ├── AdminListScreen.kt
│           │   ├── ProfileScreen.kt
│           │   ├── LogsScreen.kt
│           │   ├── VideoPlayerScreen.kt   # ExoPlayer
│           │   ├── ChildTokenInputScreen.kt
│           │   ├── ChildAlbumScreen.kt    # Fluxo da criança
│           │   └── QRScannerScreen.kt
│           └── theme/
│
├── web/                       # Frontend web (React/TSX) — álbum público
├── docs/                      # Documentação técnica
├── AGENTS.md                  # Este arquivo
├── CLAUDE.md                  # Guia específico para Claude Code
└── plano.md                   # Plano de desenvolvimento detalhado
```

---

## Backend

### Stack
- **Runtime:** Node.js 20, Express 4
- **Banco:** MongoDB via Mongoose
- **Auth:** JWT — access token (1h) + refresh token (30d) com bcryptjs
- **Storage:** Firebase Storage via `firebase-admin`
- **Upload:** `multer` (memoryStorage)
- **Outros:** `helmet`, `cors`, `express-rate-limit` (100 req/15min), `compression`, `morgan`, `qrcode`, `nodemailer`

### Rotas da API (`/api/`)

| Método | Rota | Auth | Descrição |
|---|---|---|---|
| POST | `/auth/login` | — | Login, retorna `token` + `refreshToken` |
| POST | `/auth/refresh` | — | Renova access token via refreshToken |
| POST | `/auth/registrar` | superAdmin | Cria novo admin |
| GET | `/auth/perfil` | auth | Retorna dados do usuário logado |
| PUT | `/auth/perfil` | auth | Atualiza nome e telefone |
| PUT | `/auth/senha` | auth | Altera senha |
| POST | `/auth/logout` | auth | Logout |
| GET | `/children/` | admin | Lista crianças |
| POST | `/children/` | admin | Cria criança |
| GET | `/children/:id` | admin | Obtém criança por ID |
| PUT | `/children/:id` | admin | Edita criança |
| DELETE | `/children/:id` | admin | Deleta criança |
| POST | `/children/:id/foto` | admin | Upload foto de perfil (multipart) |
| POST | `/children/:id/gerar-token` | admin | Gera token de acesso com `diasValidade` |
| GET | `/media/:criancaId` | admin | Lista mídia (`?tipo=foto\|video&ordem=asc\|desc`) |
| POST | `/media/:criancaId/upload` | admin | Upload de mídia (multipart) |
| PUT | `/media/:midiaId` | admin | Edita descrição da mídia |
| DELETE | `/media/:midiaId` | admin | Deleta mídia |
| GET | `/admin/` | superAdmin | Lista admins |
| PUT | `/admin/:id` | superAdmin | Edita admin |
| DELETE | `/admin/:id` | superAdmin | Desativa admin |
| GET | `/logs/` | superAdmin | Lista logs de auditoria |
| GET | `/album/:token` | — | Acesso público ao álbum (valida expiração) |

### Níveis de Autenticação (middleware chain)
1. `authMiddleware` — verifica JWT, popula `req.user`
2. `adminMiddleware` — role `admin` ou `super_admin`
3. `superAdminMiddleware` — role `super_admin` apenas
4. `requirePermission(perm)` — verifica `req.user.permissoes[]`

### Modelos Mongoose

**User:** `nome`, `email`, `senha` (hash bcrypt), `telefone`, `role` (`admin`|`super_admin`), `permissoes[]`, `ativo`, `dataUltimoAcesso`

**Child:** `nome`, `dataNascimento`, `fotoPerfil`, `descricao`, `tokenAcesso` (único/sparse), `tokenExpiracao` (Date), `ativo`

**Media:** `criancaId` (ref Child), `url`, `tipo` (`foto`|`video`), `descricao`, `dataMomento`, `tamanho`, `privacidade` (`apenas_crianca`|`admins_e_crianca`)

**Log:** `usuarioId`, `acao` (enum), `status` (`sucesso`|`erro`), `detalhes`, `ipAddress`, `createdAt`

---

## Android

### Stack
- **Linguagem:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Padrão:** MVVM (`Screen → ViewModel → Repository → ApiService`)
- **Rede:** Retrofit 2 + OkHttp + Gson
- **Vídeo:** Media3 ExoPlayer
- **Imagens:** Coil (`AsyncImage`)
- **Armazenamento local:** `EncryptedSharedPreferences` via `TokenManager`
- **minSdk:** 26 (Android 8.0)
- **Package:** `com.familiaaco`

### Navegação (NavGraph)

| Rota | Tela | Fluxo |
|---|---|---|
| `login` | `LoginScreen` | Admin |
| `admin_dashboard` | `AdminDashboardScreen` | Admin |
| `children_list` | `ChildrenListScreen` | Admin |
| `create_child` | `CreateChildScreen` | Admin |
| `child_detail/{childId}` | `ChildDetailScreen` | Admin |
| `media_upload/{criancaId}` | `MediaUploadScreen` | Admin |
| `admin_list` | `AdminListScreen` | Admin |
| `profile` | `ProfileScreen` | Admin |
| `logs` | `LogsScreen` | Admin (superAdmin) |
| `video_player/{url}` | `VideoPlayerScreen` | Admin |
| `child_token` | `ChildTokenInputScreen` | Criança |
| `child_album/{token}` | `ChildAlbumScreen` | Criança |
| `qr_scanner` | `QRScannerScreen` | Criança |

### TokenManager (`data/local/TokenManager.kt`)
Métodos disponíveis:
- `saveToken(token)` / `getToken()` — access token JWT
- `saveRefreshToken(token)` / `getRefreshToken()` — refresh token
- `saveUserRole(role)` / `getUserRole()` — role do usuário
- `clearAll()` — logout completo

### AuthInterceptor
- Anexa `Authorization: Bearer <token>` em toda requisição
- Em resposta `401`, tenta renovar automaticamente via `POST /auth/refresh`
- Se refresh OK, salva novo token e repete a requisição original
- Se refresh falhar, retorna 401 para o caller

### ApiClient
- `BASE_URL` = `http://192.168.1.188:3000/api/` (IP do servidor na rede Wi-Fi)
- Para emulador usar `http://10.0.2.2:3000/api/`

### Auto-Login
- `LoginScreen` verifica `TokenManager.getToken()` ao iniciar
- Se token existe → navega direto para `admin_dashboard`
- Logout chama `AuthRepository.logout()` que limpa o `TokenManager` antes de navegar para `login`

### Convenções Android
- Nomes de campos em português espelhando o backend (`nome`, `dataNascimento`, `tokenAcesso`, etc.)
- URLs de vídeo são codificadas com `URLEncoder.encode(..., "UTF-8")` antes de passar como argumento de navegação
- ViewModels são criados com factory inline usando `key = "nome_$id"` para escopo por entidade
- `@OptIn` de APIs experimentais (`ExperimentalMaterial3Api`, `ExperimentalFoundationApi`) sempre no nível da função `@Composable`, nunca inline em lambdas

---

## Variáveis de Ambiente (backend/.env)

```
PORT=3000
MONGODB_URI=mongodb://...
JWT_SECRET=chave-secreta
JWT_REFRESH_SECRET=chave-refresh-diferente
FIREBASE_CREDENTIALS={"type":"service_account",...}
FIREBASE_STORAGE_BUCKET=seu-bucket.appspot.com
ALLOWED_ORIGINS=http://localhost:3000
```

Copiar de `backend/.env.example` e preencher antes de rodar.

---

## Comandos do Backend

```bash
# Na pasta backend/
npm install       # instalar dependências
npm run dev       # iniciar com nodemon (hot reload)
npm start         # produção
npm test          # testes Jest
npm run lint      # ESLint
```

Verificar sintaxe sem rodar:
```bash
find src/routes src/models src/middleware src/services -name '*.js' | xargs -I{} node --check {}
```

---

## Convenções Gerais

- Respostas de erro sempre com `{ "error": "mensagem" }`
- `User.toJSON()` remove o campo `senha` automaticamente
- `tokenAcesso` da criança tem índice sparse único — `null` é permitido
- Logs de auditoria são criados em toda ação sensível (login, upload, deleção, acesso ao álbum)
- Soft-delete para admins (campo `ativo: false`), hard-delete para crianças e mídia
- Firebase Storage: URLs no formato `https://firebasestorage.googleapis.com/v0/b/{bucket}/o/{encoded_path}?alt=media`

---

## Superpowers — Skills do Agente

O projeto usa o framework [Superpowers](https://github.com/obra/superpowers) instalado em `~/.config/superpowers/`.

### Skills disponíveis

| Skill | Arquivo | Quando usar |
|---|---|---|
| `using-superpowers` | `~/.config/superpowers/skills/using-superpowers/SKILL.md` | Início de qualquer conversa — estabelece como usar skills |
| `brainstorming` | `~/.config/superpowers/skills/brainstorming/SKILL.md` | Antes de entrar em modo de planejamento |
| `writing-plans` | `~/.config/superpowers/skills/writing-plans/SKILL.md` | Ao criar planos de implementação |
| `executing-plans` | `~/.config/superpowers/skills/executing-plans/SKILL.md` | Ao executar planos em lote |
| `test-driven-development` | `~/.config/superpowers/skills/test-driven-development/SKILL.md` | Para ciclos RED-GREEN-REFACTOR |
| `systematic-debugging` | `~/.config/superpowers/skills/systematic-debugging/SKILL.md` | Para depuração sistemática de bugs |
| `verification-before-completion` | `~/.config/superpowers/skills/verification-before-completion/SKILL.md` | Antes de declarar uma tarefa concluída |
| `subagent-driven-development` | `~/.config/superpowers/skills/subagent-driven-development/SKILL.md` | Para desenvolvimento com subagentes |
| `requesting-code-review` | `~/.config/superpowers/skills/requesting-code-review/SKILL.md` | Antes de solicitar code review |
| `receiving-code-review` | `~/.config/superpowers/skills/receiving-code-review/SKILL.md` | Ao responder feedback de code review |
| `dispatching-parallel-agents` | `~/.config/superpowers/skills/dispatching-parallel-agents/SKILL.md` | Para fluxos paralelos de subagentes |
| `finishing-a-development-branch` | `~/.config/superpowers/skills/finishing-a-development-branch/SKILL.md` | Para merge/PR de branches |
| `using-git-worktrees` | `~/.config/superpowers/skills/using-git-worktrees/SKILL.md` | Para desenvolvimento paralelo com worktrees |
| `writing-skills` | `~/.config/superpowers/skills/writing-skills/SKILL.md` | Para criar novas skills |

### Regra de uso

Antes de responder qualquer mensagem, verifique se alguma skill se aplica (mesmo com 1% de chance). Se sim, leia o arquivo `SKILL.md` correspondente e siga-o rigorosamente.

**Prioridade das instruções:**
1. Instruções explícitas do usuário (este AGENTS.md, mensagens diretas)
2. Skills do Superpowers
3. Comportamento padrão do sistema

### Atualizar

```bash
cd ~/.config/superpowers && git pull
```
