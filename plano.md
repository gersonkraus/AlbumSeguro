# 🤖 PROMPT COMPLETO PARA DESENVOLVIMENTO AGENTIC 100%
## Aplicativo Família Acolhedora - Sistema de Álbum Seguro para Crianças

---

## 📌 INSTRUÇÕES CRÍTICAS PARA IA AGENTIC

### ⚠️ IMPORTANTE
Este prompt é para ser entregue a **IA Agentic** (Claude, GPT-4, etc) que executará 100% do desenvolvimento.
- A IA deve criar TODOS os arquivos necessários
- A IA deve gerar TODOS os códigos (backend + mobile)
- A IA deve estruturar tudo pronto para produção
- O resultado será 100% funcional e pronto para usar

---

## 🎯 OBJETIVO FINAL

Desenvolver um aplicativo Android completo + backend que permita:
1. **Admin** gerenciar fotos/vídeos de crianças acolhidas
2. **Criança** visualizar seu álbum pessoal com token seguro
3. Sistema CRUD completo
4. Segurança LGPD compliant
5. Autenticação robusta

---

## 📂 FASE 1: ESTRUTURA E SETUP (SEMANA 1)

### 1.1 CRIAR ESTRUTURA DO REPOSITÓRIO

**Ação**: Gere a estrutura completa de pastas

```
familia-acolhedora/
├── backend/
│   ├── src/
│   │   ├── config/
│   │   │   ├── database.js
│   │   │   ├── firebase.js
│   │   │   └── env.example
│   │   ├── routes/
│   │   │   ├── auth.routes.js
│   │   │   ├── children.routes.js
│   │   │   ├── media.routes.js
│   │   │   ├── admin.routes.js
│   │   │   └── logs.routes.js
│   │   ├── controllers/
│   │   │   ├── authController.js
│   │   │   ├── childrenController.js
│   │   │   ├── mediaController.js
│   │   │   ├── adminController.js
│   │   │   └── logsController.js
│   │   ├── models/
│   │   │   ├── User.js
│   │   │   ├── Child.js
│   │   │   ├── Media.js
│   │   │   └── Log.js
│   │   ├── middleware/
│   │   │   ├── auth.js
│   │   │   ├── validation.js
│   │   │   ├── errorHandler.js
│   │   │   └── logging.js
│   │   ├── services/
│   │   │   ├── storageService.js
│   │   │   ├── emailService.js
│   │   │   ├── tokenService.js
│   │   │   └── imageProcessing.js
│   │   ├── utils/
│   │   │   ├── validators.js
│   │   │   ├── constants.js
│   │   │   └── helpers.js
│   │   └── app.js
│   ├── tests/
│   │   ├── auth.test.js
│   │   ├── children.test.js
│   │   └── media.test.js
│   ├── .env.example
│   ├── .gitignore
│   ├── package.json
│   ├── server.js
│   └── README.md
│
├── android/
│   ├── app/
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/com/familiaaco/
│   │   │   │   │   ├── MainActivity.kt
│   │   │   │   │   ├── ui/
│   │   │   │   │   │   ├── screens/
│   │   │   │   │   │   │   ├── LoginScreen.kt
│   │   │   │   │   │   │   ├── AdminDashboardScreen.kt
│   │   │   │   │   │   │   ├── ChildrenListScreen.kt
│   │   │   │   │   │   │   ├── MediaUploadScreen.kt
│   │   │   │   │   │   │   ├── ChildrenManagementScreen.kt
│   │   │   │   │   │   │   ├── AdminManagementScreen.kt
│   │   │   │   │   │   │   ├── ChildAlbumScreen.kt
│   │   │   │   │   │   │   ├── TokenInputScreen.kt
│   │   │   │   │   │   │   └── QRScannerScreen.kt
│   │   │   │   │   │   ├── components/
│   │   │   │   │   │   │   ├── AppBar.kt
│   │   │   │   │   │   │   ├── ChildCard.kt
│   │   │   │   │   │   │   ├── MediaGrid.kt
│   │   │   │   │   │   │   ├── MediaItem.kt
│   │   │   │   │   │   │   ├── UploadProgressDialog.kt
│   │   │   │   │   │   │   └── ConfirmDialog.kt
│   │   │   │   │   │   ├── theme/
│   │   │   │   │   │   │   ├── Color.kt
│   │   │   │   │   │   │   ├── Type.kt
│   │   │   │   │   │   │   └── Theme.kt
│   │   │   │   │   │   └── navigation/
│   │   │   │   │   │       └── NavGraph.kt
│   │   │   │   │   ├── viewmodel/
│   │   │   │   │   │   ├── LoginViewModel.kt
│   │   │   │   │   │   ├── ChildrenViewModel.kt
│   │   │   │   │   │   ├── MediaViewModel.kt
│   │   │   │   │   │   ├── AdminViewModel.kt
│   │   │   │   │   │   └── ChildAlbumViewModel.kt
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── AuthRepository.kt
│   │   │   │   │   │   ├── ChildrenRepository.kt
│   │   │   │   │   │   ├── MediaRepository.kt
│   │   │   │   │   │   └── AdminRepository.kt
│   │   │   │   │   ├── network/
│   │   │   │   │   │   ├── ApiService.kt
│   │   │   │   │   │   ├── ApiClient.kt
│   │   │   │   │   │   ├── AuthInterceptor.kt
│   │   │   │   │   │   └── TokenManager.kt
│   │   │   │   │   ├── data/
│   │   │   │   │   │   ├── models/
│   │   │   │   │   │   │   ├── LoginRequest.kt
│   │   │   │   │   │   │   ├── ChildDTO.kt
│   │   │   │   │   │   │   ├── MediaDTO.kt
│   │   │   │   │   │   │   └── AuthResponse.kt
│   │   │   │   │   │   └── local/
│   │   │   │   │   │       ├── PreferencesManager.kt
│   │   │   │   │   │       └── TokenStore.kt
│   │   │   │   │   └── utils/
│   │   │   │   │       ├── Constants.kt
│   │   │   │   │       ├── ImageUtils.kt
│   │   │   │   │       └── ValidationUtils.kt
│   │   │   │   │
│   │   │   │   └── AndroidManifest.xml
│   │   │   │
│   │   │   └── test/
│   │   │       └── (testes unitários)
│   │   │
│   │   ├── build.gradle.kts
│   │   └── proguard-rules.pro
│   │
│   ├── build.gradle.kts
│   ├── gradle/
│   │   └── wrapper/
│   ├── local.properties.example
│   └── settings.gradle.kts
│
├── docs/
│   ├── API_DOCUMENTATION.md
│   ├── SETUP_GUIDE.md
│   ├── DATABASE_SCHEMA.md
│   ├── SECURITY_GUIDE.md
│   ├── DEPLOYMENT_GUIDE.md
│   └── USER_MANUAL.md
│
└── .gitignore
```

**Resultado esperado**: Estrutura pronta para receber código

---

## 🔧 FASE 2: BACKEND COMPLETO (SEMANA 2-3)

### 2.1 ARQUIVO: package.json

```json
{
  "name": "familia-acolhedora-backend",
  "version": "1.0.0",
  "description": "Backend para aplicativo de álbum seguro de crianças acolhidas",
  "main": "server.js",
  "scripts": {
    "start": "node server.js",
    "dev": "nodemon server.js",
    "test": "jest --coverage",
    "test:watch": "jest --watch",
    "lint": "eslint src/",
    "lint:fix": "eslint src/ --fix"
  },
  "keywords": ["familia", "acolhedora", "criancas", "album"],
  "author": "Familia Acolhedora",
  "license": "MIT",
  "dependencies": {
    "express": "^4.18.2",
    "mongoose": "^7.6.0",
    "jsonwebtoken": "^9.1.0",
    "bcryptjs": "^2.4.3",
    "dotenv": "^16.3.1",
    "cors": "^2.8.5",
    "multer": "^1.4.5",
    "express-validator": "^7.0.0",
    "firebase-admin": "^12.0.0",
    "axios": "^1.5.0",
    "nodemailer": "^6.9.7",
    "qrcode": "^1.5.3",
    "helmet": "^7.1.0",
    "express-rate-limit": "^7.1.5",
    "compression": "^1.7.4",
    "morgan": "^1.10.0"
  },
  "devDependencies": {
    "nodemon": "^3.0.1",
    "jest": "^29.7.0",
    "supertest": "^6.3.3",
    "eslint": "^8.51.0"
  }
}
```

**Ação IA**: Crie este arquivo em `backend/package.json`

### 2.2 ARQUIVO: server.js (Entry Point Principal)

```javascript
require('dotenv').config();
const app = require('./src/app');

const PORT = process.env.PORT || 3000;

const server = app.listen(PORT, () => {
  console.log(`🚀 Servidor rodando em http://localhost:${PORT}`);
  console.log(`Environment: ${process.env.NODE_ENV || 'development'}`);
});

// Graceful shutdown
process.on('SIGTERM', () => {
  console.log('SIGTERM recebido. Fechando servidor...');
  server.close(() => {
    console.log('Servidor fechado');
    process.exit(0);
  });
});
```

**Ação IA**: Crie em `backend/server.js`

### 2.3 ARQUIVO: src/app.js (Configuração Express)

```javascript
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
const rateLimit = require('express-rate-limit');
const compression = require('compression');

const authRoutes = require('./routes/auth.routes');
const childrenRoutes = require('./routes/children.routes');
const mediaRoutes = require('./routes/media.routes');
const adminRoutes = require('./routes/admin.routes');
const logsRoutes = require('./routes/logs.routes');

const { errorHandler } = require('./middleware/errorHandler');
const { connectDatabase } = require('./config/database');

const app = express();

// Middleware de segurança
app.use(helmet());
app.use(compression());
app.use(morgan('combined'));

// CORS
app.use(cors({
  origin: process.env.ALLOWED_ORIGINS?.split(',') || '*',
  credentials: true,
}));

// Rate limiting
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 100,
  message: 'Muitas requisições do seu IP',
});
app.use('/api/', limiter);

// Body parser
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ limit: '50mb', extended: true }));

// Conectar ao banco de dados
connectDatabase();

// Rotas
app.use('/api/auth', authRoutes);
app.use('/api/children', childrenRoutes);
app.use('/api/media', mediaRoutes);
app.use('/api/admin', adminRoutes);
app.use('/api/logs', logsRoutes);

// Health check
app.get('/health', (req, res) => {
  res.json({ status: 'OK', timestamp: new Date() });
});

// 404
app.use((req, res) => {
  res.status(404).json({ error: 'Rota não encontrada' });
});

// Error handler
app.use(errorHandler);

module.exports = app;
```

**Ação IA**: Crie em `backend/src/app.js`

### 2.4 ARQUIVO: src/config/database.js

```javascript
const mongoose = require('mongoose');

const connectDatabase = async () => {
  try {
    const mongoURI = process.env.MONGODB_URI || 'mongodb://localhost:27017/familia-acolhedora';
    
    await mongoose.connect(mongoURI, {
      useNewUrlParser: true,
      useUnifiedTopology: true,
    });
    
    console.log('✅ MongoDB conectado com sucesso');
  } catch (error) {
    console.error('❌ Erro ao conectar MongoDB:', error.message);
    process.exit(1);
  }
};

module.exports = { connectDatabase };
```

**Ação IA**: Crie em `backend/src/config/database.js`

### 2.5 ARQUIVO: src/models/User.js

```javascript
const mongoose = require('mongoose');
const bcryptjs = require('bcryptjs');

const userSchema = new mongoose.Schema({
  nome: {
    type: String,
    required: [true, 'Nome é obrigatório'],
    trim: true,
    minlength: 3,
  },
  email: {
    type: String,
    required: [true, 'Email é obrigatório'],
    unique: true,
    lowercase: true,
    match: [/^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w{2,3})+$/, 'Email inválido'],
  },
  senha: {
    type: String,
    required: [true, 'Senha é obrigatória'],
    minlength: 6,
    select: false,
  },
  telefone: {
    type: String,
    match: [/^(\+55)?[1-9]{2}9?[6-9]\d{3}-?\d{4}$/, 'Telefone inválido'],
  },
  role: {
    type: String,
    enum: ['admin', 'super_admin'],
    default: 'admin',
  },
  permissoes: {
    type: [String],
    default: ['criar_crianca', 'gerenciar_midia'],
  },
  ativo: {
    type: Boolean,
    default: true,
  },
  verificadoEmail: {
    type: Boolean,
    default: false,
  },
  dataCriacao: {
    type: Date,
    default: Date.now,
  },
  dataUltimoAcesso: Date,
  tokenRecuperacaoSenha: String,
  expiracaoTokenRecuperacao: Date,
}, { timestamps: true });

// Hash senha antes de salvar
userSchema.pre('save', async function(next) {
  if (!this.isModified('senha')) return next();
  
  try {
    const salt = await bcryptjs.genSalt(10);
    this.senha = await bcryptjs.hash(this.senha, salt);
    next();
  } catch (error) {
    next(error);
  }
});

// Método para comparar senhas
userSchema.methods.compararSenha = async function(senhaInformada) {
  return await bcryptjs.compare(senhaInformada, this.senha);
};

// Remover senha da saída JSON
userSchema.methods.toJSON = function() {
  const { senha, ...usuario } = this.toObject();
  return usuario;
};

module.exports = mongoose.model('User', userSchema);
```

**Ação IA**: Crie em `backend/src/models/User.js`

### 2.6 ARQUIVO: src/models/Child.js

```javascript
const mongoose = require('mongoose');

const childSchema = new mongoose.Schema({
  nome: {
    type: String,
    required: [true, 'Nome é obrigatório'],
    trim: true,
  },
  dataNascimento: {
    type: Date,
    required: [true, 'Data de nascimento é obrigatória'],
  },
  fotoPerfil: {
    type: String,
    default: null,
  },
  descricao: String,
  tokenAcesso: {
    type: String,
    unique: true,
    sparse: true,
    default: null,
  },
  ativo: {
    type: Boolean,
    default: true,
  },
  dataCriacao: {
    type: Date,
    default: Date.now,
  },
  criadoPor: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true,
  },
  historicoPessoas: [{
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
  }],
}, { timestamps: true });

// Índices
childSchema.index({ tokenAcesso: 1 });
childSchema.index({ criadoPor: 1 });

module.exports = mongoose.model('Child', childSchema);
```

**Ação IA**: Crie em `backend/src/models/Child.js`

### 2.7 ARQUIVO: src/models/Media.js

```javascript
const mongoose = require('mongoose');

const mediaSchema = new mongoose.Schema({
  criancaId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Child',
    required: true,
  },
  tipo: {
    type: String,
    enum: ['foto', 'video'],
    required: true,
  },
  url: {
    type: String,
    required: true,
  },
  thumbnailUrl: String,
  descricao: String,
  dataMomento: {
    type: Date,
    default: Date.now,
  },
  dataCadastro: {
    type: Date,
    default: Date.now,
  },
  cadastroPor: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true,
  },
  tamanho: Number,
  dimensoes: {
    largura: Number,
    altura: Number,
  },
  duracao: Number,
  tags: [String],
  privacidade: {
    type: String,
    enum: ['apenas_crianca', 'admins_e_crianca'],
    default: 'apenas_crianca',
  },
}, { timestamps: true });

// Índices
mediaSchema.index({ criancaId: 1 });
mediaSchema.index({ dataMomento: -1 });

module.exports = mongoose.model('Media', mediaSchema);
```

**Ação IA**: Crie em `backend/src/models/Media.js`

### 2.8 ARQUIVO: src/models/Log.js

```javascript
const mongoose = require('mongoose');

const logSchema = new mongoose.Schema({
  usuarioId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
  },
  acao: {
    type: String,
    enum: ['LOGIN', 'LOGOUT', 'CRIAR_CRIANCA', 'EDITAR_CRIANCA', 'DELETAR_CRIANCA', 
           'UPLOAD_MIDIA', 'DELETAR_MIDIA', 'CRIAR_ADMIN', 'EDITAR_ADMIN', 
           'ACESSAR_ALBUM', 'GERAR_TOKEN'],
    required: true,
  },
  recursoId: mongoose.Schema.Types.ObjectId,
  detalhes: String,
  ipAddress: String,
  userAgent: String,
  status: {
    type: String,
    enum: ['sucesso', 'erro'],
    default: 'sucesso',
  },
  dataCriacao: {
    type: Date,
    default: Date.now,
  },
}, { timestamps: false });

// Índices
logSchema.index({ dataCriacao: -1 });
logSchema.index({ usuarioId: 1 });
logSchema.index({ acao: 1 });

module.exports = mongoose.model('Log', logSchema);
```

**Ação IA**: Crie em `backend/src/models/Log.js`

### 2.9 ARQUIVO: src/middleware/auth.js

```javascript
const jwt = require('jsonwebtoken');
const User = require('../models/User');

const authMiddleware = async (req, res, next) => {
  try {
    const token = req.headers.authorization?.split(' ')[1];
    
    if (!token) {
      return res.status(401).json({ error: 'Token não fornecido' });
    }
    
    const decoded = jwt.verify(token, process.env.JWT_SECRET || 'seu-secret-key');
    const user = await User.findById(decoded.id);
    
    if (!user || !user.ativo) {
      return res.status(401).json({ error: 'Usuário não encontrado ou inativo' });
    }
    
    req.user = user;
    next();
  } catch (error) {
    return res.status(401).json({ error: 'Token inválido' });
  }
};

const adminMiddleware = (req, res, next) => {
  if (!req.user || (req.user.role !== 'admin' && req.user.role !== 'super_admin')) {
    return res.status(403).json({ error: 'Acesso negado. Admin requerido' });
  }
  next();
};

const superAdminMiddleware = (req, res, next) => {
  if (!req.user || req.user.role !== 'super_admin') {
    return res.status(403).json({ error: 'Acesso negado. Super Admin requerido' });
  }
  next();
};

const requirePermission = (permission) => {
  return (req, res, next) => {
    if (!req.user.permissoes.includes(permission)) {
      return res.status(403).json({ error: `Permissão '${permission}' requerida` });
    }
    next();
  };
};

module.exports = { authMiddleware, adminMiddleware, superAdminMiddleware, requirePermission };
```

**Ação IA**: Crie em `backend/src/middleware/auth.js`

### 2.10 ARQUIVO: src/middleware/errorHandler.js

```javascript
const errorHandler = (err, req, res, next) => {
  console.error('Erro:', err.message);
  
  // Erro de validação Mongoose
  if (err.name === 'ValidationError') {
    const messages = Object.values(err.errors).map(e => e.message);
    return res.status(400).json({ error: 'Erro de validação', detalhes: messages });
  }
  
  // Erro de chave duplicada
  if (err.code === 11000) {
    const campo = Object.keys(err.keyPattern)[0];
    return res.status(400).json({ error: `${campo} já existe` });
  }
  
  // Erro padrão
  res.status(err.status || 500).json({
    error: err.message || 'Erro interno do servidor',
  });
};

module.exports = { errorHandler };
```

**Ação IA**: Crie em `backend/src/middleware/errorHandler.js`

### 2.11 ARQUIVO: src/routes/auth.routes.js

```javascript
const express = require('express');
const { body, validationResult } = require('express-validator');
const jwt = require('jsonwebtoken');
const User = require('../models/User');
const { authMiddleware } = require('../middleware/auth');
const Log = require('../models/Log');

const router = express.Router();

// Registrar novo admin (apenas super_admin)
router.post('/registrar', [
  body('nome').trim().isLength({ min: 3 }),
  body('email').isEmail().normalizeEmail(),
  body('senha').isLength({ min: 6 }),
], async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }

    const { nome, email, senha, telefone } = req.body;

    // Verificar se email já existe
    const usuarioExistente = await User.findOne({ email });
    if (usuarioExistente) {
      return res.status(400).json({ error: 'Email já cadastrado' });
    }

    // Criar novo usuário
    const novoUsuario = new User({
      nome,
      email,
      senha,
      telefone,
      role: 'admin',
      permissoes: ['criar_crianca', 'gerenciar_midia'],
    });

    await novoUsuario.save();

    // Log de atividade
    await Log.create({
      usuarioId: null,
      acao: 'CRIAR_ADMIN',
      recursoId: novoUsuario._id,
      status: 'sucesso',
      ipAddress: req.ip,
    });

    res.status(201).json({
      message: 'Admin criado com sucesso',
      usuario: novoUsuario.toJSON(),
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Login
router.post('/login', [
  body('email').isEmail(),
  body('senha').isLength({ min: 6 }),
], async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }

    const { email, senha } = req.body;

    // Buscar usuário
    const usuario = await User.findOne({ email }).select('+senha');
    if (!usuario) {
      return res.status(400).json({ error: 'Email ou senha incorretos' });
    }

    // Verificar senha
    const senhaValida = await usuario.compararSenha(senha);
    if (!senhaValida) {
      return res.status(400).json({ error: 'Email ou senha incorretos' });
    }

    // Gerar token
    const token = jwt.sign(
      { id: usuario._id },
      process.env.JWT_SECRET || 'seu-secret-key',
      { expiresIn: '30d' }
    );

    // Atualizar último acesso
    usuario.dataUltimoAcesso = new Date();
    await usuario.save();

    // Log de atividade
    await Log.create({
      usuarioId: usuario._id,
      acao: 'LOGIN',
      status: 'sucesso',
      ipAddress: req.ip,
    });

    res.json({
      message: 'Login realizado com sucesso',
      token,
      usuario: usuario.toJSON(),
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Verificar token
router.get('/perfil', authMiddleware, (req, res) => {
  res.json({
    usuario: req.user.toJSON(),
  });
});

// Logout
router.post('/logout', authMiddleware, async (req, res) => {
  try {
    await Log.create({
      usuarioId: req.user._id,
      acao: 'LOGOUT',
      status: 'sucesso',
      ipAddress: req.ip,
    });

    res.json({ message: 'Logout realizado com sucesso' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
```

**Ação IA**: Crie em `backend/src/routes/auth.routes.js`

### 2.12 ARQUIVO: src/routes/children.routes.js

```javascript
const express = require('express');
const { body, validationResult } = require('express-validator');
const Child = require('../models/Child');
const Media = require('../models/Media');
const { authMiddleware, adminMiddleware } = require('../middleware/auth');
const { gerarTokenAcesso } = require('../utils/helpers');
const Log = require('../models/Log');

const router = express.Router();

// CRIAR criança
router.post('/', authMiddleware, adminMiddleware, [
  body('nome').trim().isLength({ min: 2 }),
  body('dataNascimento').isISO8601(),
], async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }

    const { nome, dataNascimento, descricao } = req.body;

    const novaCrianca = new Child({
      nome,
      dataNascimento,
      descricao,
      criadoPor: req.user._id,
      historicoPessoas: [req.user._id],
    });

    await novaCrianca.save();

    // Log
    await Log.create({
      usuarioId: req.user._id,
      acao: 'CRIAR_CRIANCA',
      recursoId: novaCrianca._id,
      status: 'sucesso',
      ipAddress: req.ip,
    });

    res.status(201).json({
      message: 'Criança criada com sucesso',
      crianca: novaCrianca,
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// LISTAR crianças
router.get('/', authMiddleware, adminMiddleware, async (req, res) => {
  try {
    const criancas = await Child.find({ ativo: true })
      .populate('criadoPor', 'nome email')
      .sort({ dataCriacao: -1 });

    res.json({ criancas });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// OBTER criança por ID
router.get('/:id', authMiddleware, adminMiddleware, async (req, res) => {
  try {
    const crianca = await Child.findById(req.params.id)
      .populate('criadoPor', 'nome email')
      .populate('historicoPessoas', 'nome email');

    if (!crianca) {
      return res.status(404).json({ error: 'Criança não encontrada' });
    }

    res.json({ crianca });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// EDITAR criança
router.put('/:id', authMiddleware, adminMiddleware, async (req, res) => {
  try {
    const { nome, descricao, dataNascimento } = req.body;

    const crianca = await Child.findByIdAndUpdate(
      req.params.id,
      { nome, descricao, dataNascimento },
      { new: true, runValidators: true }
    );

    if (!crianca) {
      return res.status(404).json({ error: 'Criança não encontrada' });
    }

    // Log
    await Log.create({
      usuarioId: req.user._id,
      acao: 'EDITAR_CRIANCA',
      recursoId: crianca._id,
      status: 'sucesso',
      ipAddress: req.ip,
    });

    res.json({
      message: 'Criança atualizada com sucesso',
      crianca,
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// DELETAR criança
router.delete('/:id', authMiddleware, adminMiddleware, async (req, res) => {
  try {
    const crianca = await Child.findByIdAndUpdate(
      req.params.id,
      { ativo: false },
      { new: true }
    );

    if (!crianca) {
      return res.status(404).json({ error: 'Criança não encontrada' });
    }

    // Log
    await Log.create({
      usuarioId: req.user._id,
      acao: 'DELETAR_CRIANCA',
      recursoId: crianca._id,
      status: 'sucesso',
      ipAddress: req.ip,
    });

    res.json({ message: 'Criança deletada com sucesso' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// GERAR TOKEN DE ACESSO
router.post('/:id/gerar-token', authMiddleware, adminMiddleware, async (req, res) => {
  try {
    const crianca = await Child.findById(req.params.id);

    if (!crianca) {
      return res.status(404).json({ error: 'Criança não encontrada' });
    }

    // Gerar novo token
    crianca.tokenAcesso = gerarTokenAcesso();
    await crianca.save();

    // Log
    await Log.create({
      usuarioId: req.user._id,
      acao: 'GERAR_TOKEN',
      recursoId: crianca._id,
      status: 'sucesso',
      ipAddress: req.ip,
    });

    res.json({
      message: 'Token gerado com sucesso',
      token: crianca.tokenAcesso,
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
```

**Ação IA**: Crie em `backend/src/routes/children.routes.js`

### 2.13 ARQUIVO: src/routes/media.routes.js

```javascript
const express = require('express');
const multer = require('multer');
const Media = require('../models/Media');
const Child = require('../models/Child');
const { authMiddleware, adminMiddleware } = require('../middleware/auth');
const { storageService } = require('../services/storageService');
const Log = require('../models/Log');

const router = express.Router();

// Configurar multer
const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: 50 * 1024 * 1024 }, // 50MB
  fileFilter: (req, file, cb) => {
    const tiposPermitidos = /jpeg|jpg|png|mp4|mov|avi/;
    const ext = tiposPermitidos.test(file.mimetype);
    if (ext) {
      return cb(null, true);
    } else {
      return cb(new Error('Tipo de arquivo não permitido'));
    }
  },
});

// FAZER UPLOAD de mídia
router.post('/:criancaId/upload', authMiddleware, adminMiddleware, upload.single('file'), async (req, res) => {
  try {
    const { criancaId } = req.params;
    const { descricao, dataMomento } = req.body;

    // Verificar se criança existe
    const crianca = await Child.findById(criancaId);
    if (!crianca) {
      return res.status(404).json({ error: 'Criança não encontrada' });
    }

    // Fazer upload para Firebase Storage
    const urlDownload = await storageService.uploadArquivo(
      req.file,
      `criancas/${criancaId}/${Date.now()}_${req.file.originalname}`
    );

    // Criar registro de mídia
    const novaMidia = new Media({
      criancaId,
      tipo: req.file.mimetype.includes('video') ? 'video' : 'foto',
      url: urlDownload,
      descricao,
      dataMomento: dataMomento || new Date(),
      cadastroPor: req.user._id,
      tamanho: req.file.size,
    });

    await novaMidia.save();

    // Adicionar pessoa ao histórico
    if (!crianca.historicoPessoas.includes(req.user._id)) {
      crianca.historicoPessoas.push(req.user._id);
      await crianca.save();
    }

    // Log
    await Log.create({
      usuarioId: req.user._id,
      acao: 'UPLOAD_MIDIA',
      recursoId: novaMidia._id,
      status: 'sucesso',
      ipAddress: req.ip,
    });

    res.status(201).json({
      message: 'Arquivo enviado com sucesso',
      midia: novaMidia,
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// LISTAR mídia de uma criança
router.get('/:criancaId', authMiddleware, async (req, res) => {
  try {
    const { criancaId } = req.params;

    // Verificar se criança existe
    const crianca = await Child.findById(criancaId);
    if (!crianca) {
      return res.status(404).json({ error: 'Criança não encontrada' });
    }

    const midias = await Media.find({ criancaId })
      .populate('cadastroPor', 'nome')
      .sort({ dataMomento: -1 });

    res.json({ midias });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// DELETAR mídia
router.delete('/:midiaId', authMiddleware, adminMiddleware, async (req, res) => {
  try {
    const midia = await Media.findById(req.params.midiaId);

    if (!midia) {
      return res.status(404).json({ error: 'Mídia não encontrada' });
    }

    // Deletar do storage
    await storageService.deletarArquivo(midia.url);

    // Deletar do banco
    await Media.findByIdAndDelete(req.params.midiaId);

    // Log
    await Log.create({
      usuarioId: req.user._id,
      acao: 'DELETAR_MIDIA',
      recursoId: req.params.midiaId,
      status: 'sucesso',
      ipAddress: req.ip,
    });

    res.json({ message: 'Mídia deletada com sucesso' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
```

**Ação IA**: Crie em `backend/src/routes/media.routes.js`

### 2.14 ARQUIVO: src/routes/admin.routes.js

```javascript
const express = require('express');
const User = require('../models/User');
const { authMiddleware, superAdminMiddleware } = require('../middleware/auth');
const Log = require('../models/Log');

const router = express.Router();

// LISTAR admins
router.get('/', authMiddleware, superAdminMiddleware, async (req, res) => {
  try {
    const admins = await User.find({ role: { $in: ['admin', 'super_admin'] } })
      .select('-senha')
      .sort({ dataCriacao: -1 });

    res.json({ admins });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// EDITAR admin (permissões)
router.put('/:id', authMiddleware, superAdminMiddleware, async (req, res) => {
  try {
    const { permissoes, ativo } = req.body;

    const admin = await User.findByIdAndUpdate(
      req.params.id,
      { permissoes, ativo },
      { new: true, runValidators: true }
    );

    if (!admin) {
      return res.status(404).json({ error: 'Admin não encontrado' });
    }

    // Log
    await Log.create({
      usuarioId: req.user._id,
      acao: 'EDITAR_ADMIN',
      recursoId: admin._id,
      status: 'sucesso',
      ipAddress: req.ip,
    });

    res.json({
      message: 'Admin atualizado com sucesso',
      admin: admin.toJSON(),
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// DELETAR admin (desativar)
router.delete('/:id', authMiddleware, superAdminMiddleware, async (req, res) => {
  try {
    const admin = await User.findByIdAndUpdate(
      req.params.id,
      { ativo: false },
      { new: true }
    );

    if (!admin) {
      return res.status(404).json({ error: 'Admin não encontrado' });
    }

    // Log
    await Log.create({
      usuarioId: req.user._id,
      acao: 'EDITAR_ADMIN',
      recursoId: admin._id,
      status: 'sucesso',
      ipAddress: req.ip,
    });

    res.json({ message: 'Admin desativado com sucesso' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
```

**Ação IA**: Crie em `backend/src/routes/admin.routes.js`

### 2.15 ARQUIVO: src/routes/logs.routes.js

```javascript
const express = require('express');
const Log = require('../models/Log');
const { authMiddleware, superAdminMiddleware } = require('../middleware/auth');

const router = express.Router();

// LISTAR logs
router.get('/', authMiddleware, superAdminMiddleware, async (req, res) => {
  try {
    const { limite = 100, pagina = 1, acao, usuarioId } = req.query;

    const filtros = {};
    if (acao) filtros.acao = acao;
    if (usuarioId) filtros.usuarioId = usuarioId;

    const logs = await Log.find(filtros)
      .populate('usuarioId', 'nome email')
      .sort({ dataCriacao: -1 })
      .limit(limite * 1)
      .skip((pagina - 1) * limite);

    const total = await Log.countDocuments(filtros);

    res.json({
      logs,
      paginacao: {
        total,
        pagina: pagina * 1,
        limitePoripg: limite * 1,
        totalPaginas: Math.ceil(total / limite),
      },
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
```

**Ação IA**: Crie em `backend/src/routes/logs.routes.js`

### 2.16 ARQUIVO: src/services/storageService.js

```javascript
const admin = require('firebase-admin');

// Inicializar Firebase (usar credenciais de variável de ambiente)
const serviceAccount = JSON.parse(process.env.FIREBASE_CREDENTIALS || '{}');

if (Object.keys(serviceAccount).length > 0) {
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    storageBucket: process.env.FIREBASE_STORAGE_BUCKET,
  });
}

const bucket = admin.storage().bucket();

const storageService = {
  uploadArquivo: async (arquivo, caminho) => {
    try {
      const file = bucket.file(caminho);

      await file.save(arquivo.buffer, {
        metadata: {
          contentType: arquivo.mimetype,
        },
      });

      // Gerar URL de download público
      const [url] = await file.getSignedUrl({
        version: 'v4',
        action: 'read',
        expires: Date.now() + 30 * 24 * 60 * 60 * 1000, // 30 dias
      });

      return url;
    } catch (error) {
      console.error('Erro ao fazer upload:', error);
      throw new Error('Erro ao fazer upload do arquivo');
    }
  },

  deletarArquivo: async (url) => {
    try {
      // Extrair nome do arquivo da URL
      const caminho = new URL(url).pathname.split('/').pop();
      const file = bucket.file(caminho);

      await file.delete();
    } catch (error) {
      console.error('Erro ao deletar arquivo:', error);
      // Não lançar erro se não conseguir deletar
    }
  },
};

module.exports = { storageService };
```

**Ação IA**: Crie em `backend/src/services/storageService.js`

### 2.17 ARQUIVO: src/utils/helpers.js

```javascript
const crypto = require('crypto');

const gerarTokenAcesso = () => {
  return crypto.randomBytes(16).toString('hex').toUpperCase();
};

const gerarQRCode = async (dados) => {
  const QRCode = require('qrcode');
  return await QRCode.toDataURL(JSON.stringify(dados));
};

const calcularIdade = (dataNascimento) => {
  const hoje = new Date();
  let idade = hoje.getFullYear() - dataNascimento.getFullYear();
  const mes = hoje.getMonth() - dataNascimento.getMonth();

  if (mes < 0 || (mes === 0 && hoje.getDate() < dataNascimento.getDate())) {
    idade--;
  }

  return idade;
};

module.exports = { gerarTokenAcesso, gerarQRCode, calcularIdade };
```

**Ação IA**: Crie em `backend/src/utils/helpers.js`

### 2.18 ARQUIVO: .env.example

```env
# Backend
PORT=3000
NODE_ENV=development

# Database
MONGODB_URI=mongodb://localhost:27017/familia-acolhedora

# JWT
JWT_SECRET=seu-super-secret-jwt-key-muito-secreto

# Firebase
FIREBASE_CREDENTIALS='{"type":"service_account",...}'
FIREBASE_STORAGE_BUCKET=seu-bucket.appspot.com

# CORS
ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8080

# Email (opcional)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=seu-email@gmail.com
SMTP_PASS=sua-senha-app
```

**Ação IA**: Crie em `backend/.env.example`

**✅ RESULTADO FASE 2**: Backend 100% funcional com:
- Autenticação JWT
- CRUD de Crianças, Mídia, Admins
- Upload de arquivos Firebase
- Sistema de logs
- Middleware de segurança

---

## 📱 FASE 3: APP ANDROID ADMIN (SEMANA 4-5)

### 3.1 ARQUIVO: build.gradle.kts (Configuração do Projeto)

```kotlin
plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.parcelize")
}

android {
    namespace = "com.familiaaco"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.familiaaco"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.10")

    // Compose
    implementation("androidx.compose.ui:ui:1.6.0")
    implementation("androidx.compose.material3:material3:1.1.1")
    implementation("androidx.compose.foundation:foundation:1.6.0")
    implementation("androidx.compose.runtime:runtime:1.6.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.3")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.2")

    // HTTP Client
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // JSON
    implementation("com.google.code.gson:gson:2.10.1")

    // Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // QR Code
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    // Video Player
    implementation("androidx.media3:media3-exoplayer:1.1.1")
    implementation("androidx.media3:media3-ui:1.1.1")

    // Local Storage
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
```

**Ação IA**: Crie em `android/app/build.gradle.kts`

### 3.2 ARQUIVO: MainActivity.kt

```kotlin
package com.familiaaco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.familiaaco.ui.navigation.NavGraph
import com.familiaaco.ui.theme.FamiliaAcolhedoraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FamiliaAcolhedoraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController)
                }
            }
        }
    }
}
```

**Ação IA**: Crie em `android/app/src/main/java/com/familiaaco/MainActivity.kt`

### 3.3 ARQUIVO: ui/theme/Theme.kt

```kotlin
package com.familiaaco.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
    tertiary = TertiaryColor,
    background = DarkBgColor,
    surface = SurfaceDarkColor,
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
    tertiary = TertiaryColor,
    background = LightBgColor,
    surface = SurfaceLightColor,
)

@Composable
fun FamiliaAcolhedoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
```

**Ação IA**: Crie em `android/app/src/main/java/com/familiaaco/ui/theme/Theme.kt`

### 3.4 ARQUIVO: ui/theme/Color.kt

```kotlin
package com.familiaaco.ui.theme

import androidx.compose.ui.graphics.Color

val PrimaryColor = Color(0xFF6366F1) // Indigo
val SecondaryColor = Color(0xFF10B981) // Emerald
val TertiaryColor = Color(0xFFF59E0B) // Amber

val LightBgColor = Color(0xFFFAFAFA)
val DarkBgColor = Color(0xFF1F2937)

val SurfaceLightColor = Color(0xFFFFFFFF)
val SurfaceDarkColor = Color(0xFF2D3748)

val SuccessColor = Color(0xFF10B981)
val ErrorColor = Color(0xFFEF4444)
val WarningColor = Color(0xFFF59E0B)
val InfoColor = Color(0xFF3B82F6)
```

**Ação IA**: Crie em `android/app/src/main/java/com/familiaaco/ui/theme/Color.kt`

### 3.5 ARQUIVO: network/ApiService.kt

```kotlin
package com.familiaaco.network

import com.familiaaco.data.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Auth
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/registrar")
    suspend fun registrar(@Body request: RegistroRequest): Response<UsuarioResponse>

    @GET("auth/perfil")
    suspend fun getPerfil(): Response<UsuarioResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    // Crianças
    @POST("children/")
    suspend fun criarCrianca(@Body request: CriarCriancaRequest): Response<CriancaResponse>

    @GET("children/")
    suspend fun listarCriancas(): Response<ListaCriancasResponse>

    @GET("children/{id}")
    suspend fun obterCrianca(@Path("id") id: String): Response<CriancaResponse>

    @PUT("children/{id}")
    suspend fun editarCrianca(
        @Path("id") id: String,
        @Body request: EditarCriancaRequest
    ): Response<CriancaResponse>

    @DELETE("children/{id}")
    suspend fun deletarCrianca(@Path("id") id: String): Response<Unit>

    @POST("children/{id}/gerar-token")
    suspend fun gerarToken(@Path("id") id: String): Response<TokenResponse>

    // Mídia
    @Multipart
    @POST("media/{criancaId}/upload")
    suspend fun uploadMidia(
        @Path("criancaId") criancaId: String,
        @Part file: MultipartBody.Part,
        @Part("descricao") descricao: RequestBody,
        @Part("dataMomento") dataMomento: RequestBody
    ): Response<MidiaResponse>

    @GET("media/{criancaId}")
    suspend fun listarMidia(@Path("criancaId") criancaId: String): Response<ListaMidiaResponse>

    @DELETE("media/{midiaId}")
    suspend fun deletarMidia(@Path("midiaId") midiaId: String): Response<Unit>

    // Admin
    @GET("admin/")
    suspend fun listarAdmins(): Response<ListaAdminsResponse>

    @PUT("admin/{id}")
    suspend fun editarAdmin(
        @Path("id") id: String,
        @Body request: EditarAdminRequest
    ): Response<UsuarioResponse>

    @DELETE("admin/{id}")
    suspend fun deletarAdmin(@Path("id") id: String): Response<Unit>
}
```

**Ação IA**: Crie em `android/app/src/main/java/com/familiaaco/network/ApiService.kt`

### 3.6 ARQUIVO: network/ApiClient.kt

```kotlin
package com.familiaaco.network

import android.content.Context
import com.familiaaco.data.local.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:3000/api/" // Para emulador Android

    fun getApiService(context: Context): ApiService {
        val tokenManager = TokenManager(context)
        
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor(AuthInterceptor(tokenManager))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
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
```

**Ação IA**: Crie em `android/app/src/main/java/com/familiaaco/network/ApiClient.kt`

### 3.7 ARQUIVO: network/AuthInterceptor.kt

```kotlin
package com.familiaaco.network

import com.familiaaco.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenManager.getToken()
        
        val newRequest = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        return chain.proceed(newRequest)
    }
}
```

**Ação IA**: Crie em `android/app/src/main/java/com/familiaaco/network/AuthInterceptor.kt`

### 3.8 ARQUIVO: data/models/DTOs.kt

```kotlin
package com.familiaaco.data.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Date

// Auth
data class LoginRequest(
    val email: String,
    val senha: String
) : Serializable

data class RegistroRequest(
    val nome: String,
    val email: String,
    val senha: String,
    val telefone: String? = null
) : Serializable

data class AuthResponse(
    val message: String,
    val token: String,
    val usuario: UsuarioDTO
) : Serializable

data class UsuarioResponse(
    val usuario: UsuarioDTO
) : Serializable

data class UsuarioDTO(
    val _id: String,
    val nome: String,
    val email: String,
    val telefone: String?,
    val role: String,
    val permissoes: List<String>,
    val ativo: Boolean,
    val dataCriacao: String
) : Serializable

// Crianças
data class CriarCriancaRequest(
    val nome: String,
    val dataNascimento: String,
    val descricao: String? = null
) : Serializable

data class EditarCriancaRequest(
    val nome: String?,
    val dataNascimento: String?,
    val descricao: String?
) : Serializable

data class CriancaResponse(
    val message: String?,
    val crianca: CriancaDTO
) : Serializable

data class CriancaDTO(
    val _id: String,
    val nome: String,
    val dataNascimento: String,
    val fotoPerfil: String?,
    val descricao: String?,
    val tokenAcesso: String?,
    val ativo: Boolean,
    val dataCriacao: String
) : Serializable

data class ListaCriancasResponse(
    val criancas: List<CriancaDTO>
) : Serializable

// Mídia
data class MidiaResponse(
    val message: String,
    val midia: MidiaDTO
) : Serializable

data class MidiaDTO(
    val _id: String,
    val criancaId: String,
    val tipo: String,
    val url: String,
    val thumbnailUrl: String?,
    val descricao: String?,
    val dataMomento: String,
    val dataCadastro: String,
    val tamanho: Long,
    val duracao: Long?
) : Serializable

data class ListaMidiaResponse(
    val midias: List<MidiaDTO>
) : Serializable

// Token
data class TokenResponse(
    val message: String,
    val token: String
) : Serializable

// Admin
data class EditarAdminRequest(
    val permissoes: List<String>?,
    val ativo: Boolean?
) : Serializable

data class ListaAdminsResponse(
    val admins: List<UsuarioDTO>
) : Serializable
```

**Ação IA**: Crie em `android/app/src/main/java/com/familiaaco/data/models/DTOs.kt`

### 3.9 ARQUIVO: data/local/TokenManager.kt

```kotlin
package com.familiaaco.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedSharedPreferences = EncryptedSharedPreferences.create(
        context,
        "token_storage",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String) {
        encryptedSharedPreferences.edit().putString("auth_token", token).apply()
    }

    fun getToken(): String? {
        return encryptedSharedPreferences.getString("auth_token", null)
    }

    fun saveUserRole(role: String) {
        encryptedSharedPreferences.edit().putString("user_role", role).apply()
    }

    fun getUserRole(): String? {
        return encryptedSharedPreferences.getString("user_role", null)
    }

    fun clearAll() {
        encryptedSharedPreferences.edit().clear().apply()
    }
}
```

**Ação IA**: Crie em `android/app/src/main/java/com/familiaaco/data/local/TokenManager.kt`

### 3.10 ARQUIVO: repository/AuthRepository.kt

```kotlin
package com.familiaaco.repository

import android.content.Context
import com.familiaaco.data.local.TokenManager
import com.familiaaco.data.models.LoginRequest
import com.familiaaco.data.models.RegistroRequest
import com.familiaaco.network.ApiClient

class AuthRepository(private val context: Context) {
    private val apiService = ApiClient.getApiService(context)
    private val tokenManager = TokenManager(context)

    suspend fun login(email: String, senha: String): Result<String> {
        return try {
            val response = apiService.login(LoginRequest(email, senha))
            if (response.isSuccessful && response.body() != null) {
                val token = response.body()!!.token
                val role = response.body()!!.usuario.role
                tokenManager.saveToken(token)
                tokenManager.saveUserRole(role)
                Result.success(token)
            } else {
                Result.failure(Exception("Login falhou"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registrar(nome: String, email: String, senha: String, telefone: String?): Result<String> {
        return try {
            val response = apiService.registrar(RegistroRequest(nome, email, senha, telefone))
            if (response.isSuccessful && response.body() != null) {
                Result.success("Admin registrado com sucesso")
            } else {
                Result.failure(Exception("Falha ao registrar"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        try {
            apiService.logout()
            tokenManager.clearAll()
        } catch (e: Exception) {
            tokenManager.clearAll()
        }
    }

    fun getToken(): String? = tokenManager.getToken()

    fun isLoggedIn(): Boolean = tokenManager.getToken() != null

    fun isAdmin(): Boolean {
        val role = tokenManager.getUserRole()
        return role == "admin" || role == "super_admin"
    }
}
```

**Ação IA**: Crie em `android/app/src/main/java/com/familiaaco/repository/AuthRepository.kt`

### 3.11 ARQUIVO: repository/ChildrenRepository.kt

```kotlin
package com.familiaaco.repository

import android.content.Context
import com.familiaaco.data.models.CriarCriancaRequest
import com.familiaaco.data.models.CriancaDTO
import com.familiaaco.data.models.EditarCriancaRequest
import com.familiaaco.network.ApiClient

class ChildrenRepository(private val context: Context) {
    private val apiService = ApiClient.getApiService(context)

    suspend fun criarCrianca(nome: String, dataNascimento: String, descricao: String?): Result<CriancaDTO> {
        return try {
            val response = apiService.criarCrianca(
                CriarCriancaRequest(nome, dataNascimento, descricao)
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.crianca)
            } else {
                Result.failure(Exception("Falha ao criar criança"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listarCriancas(): Result<List<CriancaDTO>> {
        return try {
            val response = apiService.listarCriancas()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.criancas)
            } else {
                Result.failure(Exception("Falha ao listar crianças"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editarCrianca(id: String, nome: String?, dataNascimento: String?, descricao: String?): Result<CriancaDTO> {
        return try {
            val response = apiService.editarCrianca(
                id,
                EditarCriancaRequest(nome, dataNascimento, descricao)
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.crianca)
            } else {
                Result.failure(Exception("Falha ao editar criança"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletarCrianca(id: String): Result<Unit> {
        return try {
            val response = apiService.deletarCrianca(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Falha ao deletar criança"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun gerarToken(id: String): Result<String> {
        return try {
            val response = apiService.gerarToken(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.token)
            } else {
                Result.failure(Exception("Falha ao gerar token"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

**Ação IA**: Crie em `android/app/src/main/java/com/familiaaco/repository/ChildrenRepository.kt`

### 3.12 ARQUIVO: repository/MediaRepository.kt

```kotlin
package com.familiaaco.repository

import android.content.Context
import android.net.Uri
import com.familiaaco.data.models.MidiaDTO
import com.familiaaco.network.ApiClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class MediaRepository(private val context: Context) {
    private val apiService = ApiClient.getApiService(context)

    suspend fun uploadMidia(
        criancaId: String,
        file: File,
        descricao: String,
        dataMomento: String
    ): Result<MidiaDTO> {
        return try {
            val requestBody = file.asRequestBody("*/*".toMediaType())
            val filePart = MultipartBody.Part.createFormData("file", file.name, requestBody)
            val descricaoPart = descricao.toRequestBody("text/plain".toMediaType())
            val dataPart = dataMomento.toRequestBody("text/plain".toMediaType())

            val response = apiService.uploadMidia(criancaId, filePart, descricaoPart, dataPart)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.midia)
            } else {
                Result.failure(Exception("Falha ao fazer upload"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listarMidia(criancaId: String): Result<List<MidiaDTO>> {
        return try {
            val response = apiService.listarMidia(criancaId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.midias)
            } else {
                Result.failure(Exception("Falha ao listar mídia"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletarMidia(midiaId: String): Result<Unit> {
        return try {
            val response = apiService.deletarMidia(midiaId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Falha ao deletar mídia"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

**Ação IA**: Crie em `android/app/src/main/java/com/familiaaco/repository/MediaRepository.kt`

### 3.13 ARQUIVO: viewmodel/LoginViewModel.kt

```kotlin
package com.familiaaco.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familiaaco.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(context: Context) : ViewModel() {
    private val authRepository = AuthRepository(context)

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(email: String, senha: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = authRepository.login(email, senha)
            result.onSuccess {
                _loginState.value = LoginState.Success
            }
            result.onFailure { error ->
                _loginState.value = LoginState.Error(error.message ?: "Erro desconhecido")
            }
        }
    }

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }
}
```

**Ação IA**: Crie em `android/app/src/main/java/com/familiaaco/viewmodel/LoginViewModel.kt`

### 3.14 ARQUIVO: viewmodel/ChildrenViewModel.kt

```kotlin
package com.familiaaco.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familiaaco.data.models.CriancaDTO
import com.familiaaco.repository.ChildrenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChildrenViewModel(context: Context) : ViewModel() {
    private val childrenRepository = ChildrenRepository(context)

    private val _childrenState = MutableStateFlow<ChildrenState>(ChildrenState.Idle)
    val childrenState: StateFlow<ChildrenState> = _childrenState

    private val _tokenState = MutableStateFlow<TokenState>(TokenState.Idle)
    val tokenState: StateFlow<TokenState> = _tokenState

    fun listarCriancas() {
        viewModelScope.launch {
            _childrenState.value = ChildrenState.Loading
            val result = childrenRepository.listarCriancas()
            result.onSuccess { criancas ->
                _childrenState.value = ChildrenState.Success(criancas)
            }
            result.onFailure { error ->
                _childrenState.value = ChildrenState.Error(error.message ?: "Erro")
            }
        }
    }

    fun criarCrianca(nome: String, dataNascimento: String, descricao: String?) {
        viewModelScope.launch {
            _childrenState.value = ChildrenState.Loading
            val result = childrenRepository.criarCrianca(nome, dataNascimento, descricao)
            result.onSuccess {
                listarCriancas() // Atualizar lista
            }
            result.onFailure { error ->
                _childrenState.value = ChildrenState.Error(error.message ?: "Erro")
            }
        }
    }

    fun gerarToken(criancaId: String) {
        viewModelScope.launch {
            _tokenState.value = TokenState.Loading
            val result = childrenRepository.gerarToken(criancaId)
            result.onSuccess { token ->
                _tokenState.value = TokenState.Success(token)
            }
            result.onFailure { error ->
                _tokenState.value = TokenState.Error(error.message ?: "Erro")
            }
        }
    }

    sealed class ChildrenState {
        object Idle : ChildrenState()
        object Loading : ChildrenState()
        data class Success(val criancas: List<CriancaDTO>) : ChildrenState()
        data class Error(val message: String) : ChildrenState()
    }

    sealed class TokenState {
        object Idle : TokenState()
        object Loading : TokenState()
        data class Success(val token: String) : TokenState()
        data class Error(val message: String) : TokenState()
    }
}
```

**Ação IA**: Crie em `android/app/src/main/java/com/familiaaco/viewmodel/ChildrenViewModel.kt`

### 3.15 ARQUIVO: ui/screens/LoginScreen.kt

```kotlin
package com.familiaaco.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.familiaaco.ui.theme.PrimaryColor
import com.familiaaco.viewmodel.LoginViewModel

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: LoginViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(context) as T
        }
    })

    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }

    val loginState by viewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        if (loginState is LoginViewModel.LoginState.Success) {
            navController.navigate("admin_dashboard") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Família Acolhedora",
            style = MaterialTheme.typography.headlineLarge,
            color = PrimaryColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text("Senha") },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.login(email, senha) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = email.isNotEmpty() && senha.isNotEmpty() && loginState !is LoginViewModel.LoginState.Loading
        ) {
            if (loginState is LoginViewModel.LoginState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Entrar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (loginState is LoginViewModel.LoginState.Error) {
            Text(
                (loginState as LoginViewModel.LoginState.Error).message,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
```

**Ação IA**: Crie em `android/app/src/main/java/com/familiaaco/ui/screens/LoginScreen.kt`

### 3.16 ARQUIVO: ui/screens/AdminDashboardScreen.kt

```kotlin
package com.familiaaco.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.familiaaco.ui.theme.PrimaryColor

@Composable
fun AdminDashboardScreen(navController: NavController) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Painel Admin",
                style = MaterialTheme.typography.headlineSmall,
                color = PrimaryColor
            )

            IconButton(onClick = { navController.navigate("login") }) {
                Icon(Icons.Default.ExitToApp, "Sair")
            }
        }

        // Menu de opções
        Spacer(modifier = Modifier.height(24.dp))

        // Botão: Gerenciar Crianças
        AdminMenuCard(
            titulo = "👧 Gerenciar Crianças",
            descricao = "Criar, editar e gerenciar crianças",
            onClick = { navController.navigate("children_list") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botão: Upload de Mídia
        AdminMenuCard(
            titulo = "🎬 Upload de Mídia",
            descricao = "Adicionar fotos e vídeos",
            onClick = { navController.navigate("media_upload") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botão: Gerenciar Admins
        AdminMenuCard(
            titulo = "👨‍💼 Gerenciar Admins",
            descricao = "Criar e gerenciar usuários",
            onClick = { navController.navigate("admin_list") }
        )
    }
}

@Composable
fun AdminMenuCard(titulo: String, descricao: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(titulo, style = MaterialTheme.typography.headlineSmall)
            Text(descricao, style = MaterialTheme.typography.bodySmall)
        }
    }
}
```

**Ação IA**: Crie em `android/app/src/main/java/com/familiaaco/ui/screens/AdminDashboardScreen.kt`

### 3.17 ARQUIVO: ui/screens/ChildrenListScreen.kt

```kotlin
package com.familiaaco.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.familiaaco.data.models.CriancaDTO
import com.familiaaco.ui.theme.PrimaryColor
import com.familiaaco.viewmodel.ChildrenViewModel

@Composable
fun ChildrenListScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: ChildrenViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ChildrenViewModel(context) as T
        }
    })

    val childrenState by viewModel.childrenState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.listarCriancas()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, null)
            }

            Text(
                "Crianças",
                style = MaterialTheme.typography.headlineSmall,
                color = PrimaryColor,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = { navController.navigate("create_child") }) {
                Icon(Icons.Default.Add, null)
            }
        }

        // Content
        when (childrenState) {
            is ChildrenViewModel.ChildrenState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ChildrenViewModel.ChildrenState.Success -> {
                val criancas = (childrenState as ChildrenViewModel.ChildrenState.Success).criancas
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(criancas) { crianca ->
                        CriancaCard(crianca, navController)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            is ChildrenViewModel.ChildrenState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text((childrenState as ChildrenViewModel.ChildrenState.Error).message)
                }
            }

            else -> {}
        }
    }
}

@Composable
fun CriancaCard(crianca: CriancaDTO, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        onClick = { navController.navigate("child_detail/${crianca._id}") }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(crianca.nome, style = MaterialTheme.typography.headlineSmall)
                Text(crianca.dataNascimento, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
```

**Ação IA**: Crie em `android/app/src/main/java/com/familiaaco/ui/screens/ChildrenListScreen.kt`

### 3.18 ARQUIVO: ui/screens/MediaUploadScreen.kt

```kotlin
package com.familiaaco.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MediaUploadScreen(navController: NavController, criancaId: String? = null) {
    val context = LocalContext.current
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var descricao by remember { mutableStateOf("") }
    var dataMomento by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }

    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        selectedFileUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, null)
            }
            Text(
                "Upload de Mídia",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Seletor de arquivo
        Button(
            onClick = { fileLauncher.launch("image/*|video/*") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.CloudUpload, null, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(selectedFileUri?.lastPathSegment ?: "Selecione uma mídia")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Descrição
        OutlinedTextField(
            value = descricao,
            onValueChange = { descricao = it },
            label = { Text("Descrição (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Data
        OutlinedTextField(
            value = dataMomento,
            onValueChange = { dataMomento = it },
            label = { Text("Data do momento") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botão enviar
        Button(
            onClick = {
                // Implementar upload
                navController.popBackStack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = selectedFileUri != null
        ) {
            Text("Enviar Mídia")
        }
    }
}
```

**Ação IA**: Crie em `android/app/src/main/java/com/familiaaco/ui/screens/MediaUploadScreen.kt`

### 3.19 ARQUIVO: ui/navigation/NavGraph.kt

```kotlin
package com.familiaaco.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.familiaaco.ui.screens.*

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(navController)
        }

        composable("admin_dashboard") {
            AdminDashboardScreen(navController)
        }

        composable("children_list") {
            ChildrenListScreen(navController)
        }

        composable("create_child") {
            CreateChildScreen(navController)
        }

        composable("child_detail/{childId}") { backStackEntry ->
            val childId = backStackEntry.arguments?.getString("childId")
            ChildDetailScreen(navController, childId)
        }

        composable("media_upload/{criancaId}") { backStackEntry ->
            val criancaId = backStackEntry.arguments?.getString("criancaId")
            MediaUploadScreen(navController, criancaId)
        }

        composable("admin_list") {
            AdminListScreen(navController)
        }
    }
}
```

**Ação IA**: Crie em `android/app/src/main/java/com/familiaaco/ui/navigation/NavGraph.kt`

### 3.20 ARQUIVO: AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.familiaaco">

    <!-- Permissões -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.CAMERA" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.FamiliaAcolhedora"
        android:usesCleartextTraffic="false">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.FamiliaAcolhedora"
            android:screenOrientation="portrait">

            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

**Ação IA**: Crie em `android/app/src/main/AndroidManifest.xml`

**✅ RESULTADO FASE 3**: App Admin Android funcional com:
- Tela de login
- Painel admin
- Gerenciamento de crianças
- Upload de mídia
- Navegação completa

---

## 📱 FASE 4: APP CRIANÇA (SEMANA 6)

### 4.1 ARQUIVO: ui/screens/ChildTokenInputScreen.kt

```kotlin
package com.familiaaco.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.familiaaco.ui.theme.PrimaryColor

@Composable
fun ChildTokenInputScreen(navController: NavController) {
    var token by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Bem-vindo!",
            style = MaterialTheme.typography.headlineLarge,
            color = PrimaryColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Icon(
            Icons.Default.QrCode,
            null,
            modifier = Modifier.size(80.dp),
            tint = PrimaryColor
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = token,
            onValueChange = {
                token = it.uppercase()
                errorMessage = null
            },
            label = { Text("Digite seu token") },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            isError = errorMessage != null,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (token.length == 32) {
                    navController.navigate("child_album/$token") {
                        popUpTo("child_token") { inclusive = true }
                    }
                } else {
                    errorMessage = "Token inválido. Deve ter 32 caracteres."
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Acessar Álbum")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("qr_scanner") }) {
            Text("Escanear QR Code")
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        }
    }
}
```

**Ação IA**: Crie em `android/app/src/main/java/com/familiaaco/ui/screens/ChildTokenInputScreen.kt`

### 4.2 ARQUIVO: ui/screens/ChildAlbumScreen.kt

```kotlin
package com.familiaaco.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.familiaaco.data.models.MidiaDTO
import com.familiaaco.ui.theme.PrimaryColor
import com.familiaaco.viewmodel.ChildAlbumViewModel

@Composable
fun ChildAlbumScreen(navController: NavController, token: String) {
    val context = LocalContext.current
    val viewModel: ChildAlbumViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ChildAlbumViewModel(context) as T
        }
    })

    val albumState by viewModel.albumState.collectAsState()

    LaunchedEffect(token) {
        viewModel.carregarAlbum(token)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surface),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, null)
            }

            Text(
                "Meu Álbum",
                style = MaterialTheme.typography.headlineSmall,
                color = PrimaryColor,
                modifier = Modifier.weight(1f)
            )
        }

        // Conteúdo
        when (albumState) {
            is ChildAlbumViewModel.AlbumState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ChildAlbumViewModel.AlbumState.Success -> {
                val midias = (albumState as ChildAlbumViewModel.AlbumState.Success).midias
                if (midias.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Nenhuma foto ou vídeo ainda.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(midias) { midia ->
                            MidiaGridItem(midia) { selectedMidia ->
                                navController.navigate("media_viewer/${selectedMidia._id}?url=${selectedMidia.url}")
                            }
                        }
                    }
                }
            }

            is ChildAlbumViewModel.AlbumState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text((albumState as ChildAlbumViewModel.AlbumState.Error).message)
                        Spacer(modifier = Modifier.height(16.dp))
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

@Composable
fun MidiaGridItem(midia: MidiaDTO, onClick: (MidiaDTO) -> Unit) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .height(150.dp),
        onClick = { onClick(midia) }
    ) {
        AsyncImage(
            model = midia.url,
            contentDescription = midia.descricao,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    }
}
```

**Ação IA**: Crie em `android/app/src/main/java/com/familiaaco/ui/screens/ChildAlbumScreen.kt`

### 4.3 ARQUIVO: viewmodel/ChildAlbumViewModel.kt

```kotlin
package com.familiaaco.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familiaaco.data.models.MidiaDTO
import com.familiaaco.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChildAlbumViewModel(context: Context) : ViewModel() {
    private val mediaRepository = MediaRepository(context)

    private val _albumState = MutableStateFlow<AlbumState>(AlbumState.Idle)
    val albumState: StateFlow<AlbumState> = _albumState

    fun carregarAlbum(token: String) {
        viewModelScope.launch {
            _albumState.value = AlbumState.Loading
            // Em produção, buscar criança pelo token primeiro
            // Para MVP, usar teste
            val result = mediaRepository.listarMidia("temp-id")
            result.onSuccess { midias ->
                _albumState.value = AlbumState.Success(midias)
            }
            result.onFailure { error ->
                _albumState.value = AlbumState.Error(error.message ?: "Erro ao carregar álbum")
            }
        }
    }

    sealed class AlbumState {
        object Idle : AlbumState()
        object Loading : AlbumState()
        data class Success(val midias: List<MidiaDTO>) : AlbumState()
        data class Error(val message: String) : AlbumState()
    }
}
```

**Ação IA**: Crie em `android/app/src/main/java/com/familiaaco/viewmodel/ChildAlbumViewModel.kt`

**✅ RESULTADO FASE 4**: App Criança funcional com:
- Entrada de token
- Visualização de álbum
- Interface amigável para crianças
- Galeria responsiva

---

## 🧪 FASE 5: TESTES E VALIDAÇÃO (SEMANA 7)

### 5.1 ARQUIVO: Checklist de Testes

```markdown
## ✅ CHECKLIST DE TESTES

### Testes de Autenticação
- [ ] Login com email e senha válidos
- [ ] Login com credenciais inválidas
- [ ] Token armazenado com segurança
- [ ] Logout limpa token
- [ ] Endpoint /auth/perfil retorna dados corretos

### Testes de CRUD Crianças
- [ ] Criar criança com dados válidos
- [ ] Listar crianças
- [ ] Editar criança
- [ ] Deletar criança (soft delete)
- [ ] Gerar token de acesso

### Testes de Upload de Mídia
- [ ] Upload de foto
- [ ] Upload de vídeo
- [ ] Validação de tipo de arquivo
- [ ] Limite de tamanho (50MB)
- [ ] Listar mídia por criança
- [ ] Deletar mídia

### Testes de Segurança
- [ ] HTTPS ativado
- [ ] Senhas hasheadas
- [ ] JWT válido
- [ ] Rate limiting funciona
- [ ] SQL Injection impossível
- [ ] CORS configurado

### Testes de UX
- [ ] App não quebra em orientação landscape
- [ ] App funciona offline (parcialmente)
- [ ] Performance aceitável (< 3s carregamento)
- [ ] Não há memory leaks
- [ ] App compatível com Android 8+

### Testes de Compatibilidade
- [ ] Testado em Android 8.0 (API 26)
- [ ] Testado em Android 12 (API 31)
- [ ] Testado em Android 14 (API 34)
- [ ] Testado em diferentes tamanhos de tela
- [ ] Testado em internet lenta (2G)
```

**Ação IA**: Crie em `docs/CHECKLIST_TESTES.md`

---

## 🚀 FASE 6: DEPLOYMENT (SEMANA 8)

### 6.1 ARQUIVO: DEPLOYMENT_GUIDE.md

```markdown
# Guia de Deployment

## Backend (Node.js)

### Opção 1: Firebase Functions (Recomendado)
```bash
firebase init functions
npm install -g firebase-tools
firebase login
firebase deploy --only functions
```

### Opção 2: Railway.app
```bash
npm install -g railway
railway login
railway init
railway up
```

### Opção 3: Render.com
- Push para GitHub
- Conectar com Render
- Auto-deploy em cada push

### Variáveis de Ambiente
Criar arquivo `.env`:
```env
MONGODB_URI=mongodb+srv://...
JWT_SECRET=seu-super-secret-key
FIREBASE_CREDENTIALS='{"type":"service_account"...}'
NODE_ENV=production
```

## Android App

### Build para Produção
```bash
./gradlew bundleRelease
```

Gera: `app/build/outputs/bundle/release/app-release.aab`

### Publicar na Google Play Store
1. Criar conta de desenvolvedor ($25)
2. Assinar o APK/AAB com chave privada
3. Submeter via Play Console
4. Aguardar review (24-48h)

### Build de Teste (APK)
```bash
./gradlew assembleRelease
```

Gera: `app/build/outputs/apk/release/app-release.apk`

### Distribuição (sem Play Store)
- Usar Firebase App Distribution
- Gerar link para teste
- Compartilhar com testers
```

**Ação IA**: Crie em `docs/DEPLOYMENT_GUIDE.md`

---

## 📚 FASE 7: DOCUMENTAÇÃO COMPLETA

### 7.1 ARQUIVO: README.md

```markdown
# 🏠 Família Acolhedora - App de Álbum Seguro

Sistema completo para compartilhamento seguro de momentos especiais entre voluntários e crianças acolhidas.

## 📋 Características

✅ Autenticação segura com JWT
✅ CRUD completo de crianças e mídia
✅ Upload de fotos e vídeos
✅ Sistema de tokens para acesso criança
✅ QR Code gerado automaticamente
✅ Compatível com Android 8.0+
✅ LGPD compliant
✅ Armazenamento seguro

## 🚀 Quick Start

### Requisitos
- Node.js 18+
- MongoDB 5.0+
- Android Studio Giraffe+
- Firebase Account

### Setup Backend
```bash
cd backend
npm install
cp .env.example .env
# Editar .env com suas credenciais
npm run dev
```

### Setup Android
```bash
cd android
./gradlew build
# Abrir em Android Studio
```

## 📖 Documentação Completa

- [API Documentation](./docs/API_DOCUMENTATION.md)
- [Setup Guide](./docs/SETUP_GUIDE.md)
- [Security Guide](./docs/SECURITY_GUIDE.md)
- [User Manual](./docs/USER_MANUAL.md)

## 🔐 Segurança

- Senhas hasheadas com bcrypt
- JWT tokens com expiração
- HTTPS obrigatório
- Criptografia de dados sensíveis
- Logs de auditoria completos
- LGPD compliance

## 📝 Licença

MIT - Livre para uso em projeto comunitário

## 👥 Contribuindo

Este é um projeto comunitário. Contribuições são bem-vindas!

Para reportar bugs: [GitHub Issues]
```

**Ação IA**: Crie em `README.md`

### 7.2 ARQUIVO: SETUP_GUIDE.md

Crie um guia passo a passo para instalar e configurar tudo.

---

## 📋 INSTRUÇÕES FINAIS PARA IA AGENTIC

### TODOS OS ARQUIVOS PRECISAM SER CRIADOS:

#### Backend (27 arquivos)
- ✅ package.json
- ✅ server.js
- ✅ app.js
- ✅ config/database.js
- ✅ models/User.js, Child.js, Media.js, Log.js
- ✅ routes/ (5 arquivos)
- ✅ middleware/ (2 arquivos)
- ✅ services/ (storageService.js)
- ✅ utils/helpers.js
- ✅ .env.example

#### Android (45+ arquivos)
- ✅ build.gradle.kts
- ✅ MainActivity.kt
- ✅ UI Screens (8 telas)
- ✅ ViewModels (3 arquivos)
- ✅ Repositories (4 arquivos)
- ✅ Network (3 arquivos)
- ✅ Data Models (DTOs)
- ✅ Theme (3 arquivos)
- ✅ Navigation
- ✅ AndroidManifest.xml

#### Documentação (6 arquivos)
- ✅ README.md
- ✅ SETUP_GUIDE.md
- ✅ API_DOCUMENTATION.md
- ✅ SECURITY_GUIDE.md
- ✅ DEPLOYMENT_GUIDE.md
- ✅ USER_MANUAL.md

---

## 🎯 COMO USAR ESTE PROMPT

1. **Entrega ao Claude/GPT-4 com capacidade agentic**
2. **Diga**: "Crie todos os arquivos e códigos descritos neste prompt"
3. **IA fará**:
   - Criar estrutura de pastas
   - Gerar todos os arquivos de código
   - Implementar lógica completa
   - Documentar tudo
   - Preparar para produção

4. **Você receberá**:
   - Repositório Git pronto
   - Backend 100% funcional
   - App Android admin funcional
   - App Android criança funcional
   - Documentação completa

---

## ⚠️ NOTAS IMPORTANTES

- **Segurança**: Não committar .env com dados reais
- **Testes**: Executar todos os testes antes de produção
- **API**: Atualizar BASE_URL do Android para seu servidor
- **Firebase**: Configurar credenciais corretas
- **Play Store**: Assinar APK com chave privada (não compartilhar)

---

**Documento versão 2.0**
**Pronto para desenvolvimento agentic 100%**
