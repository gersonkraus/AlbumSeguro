# Álbum Seguro - Website

Interface web para visualização de álbuns de fotos e vídeos de crianças via token de acesso.

## 🚀 Stack Tecnológico

- **React 18** - Framework UI
- **TypeScript** - Type safety
- **Vite** - Build tool rápida
- **Tailwind CSS** - Estilização
- **React Router** - Navegação
- **Axios** - HTTP client
- **React Icons** - Ícones

## 📁 Estrutura

```
web/
├── src/
│   ├── api/
│   │   └── apiClient.ts       # Cliente HTTP
│   ├── pages/
│   │   ├── AlbumPage.tsx      # Página do álbum
│   │   └── NotFoundPage.tsx   # Página 404
│   ├── styles/
│   │   └── globals.css        # Estilos globais + Tailwind
│   ├── App.tsx                # Rotas
│   └── main.tsx               # Entry point
├── public/
│   └── favicon.svg            # Favicon
├── package.json
├── tsconfig.json
├── tailwind.config.js
└── vite.config.ts
```

## 🔧 Instalação

```bash
cd web
npm install
```

## 📝 Configuração

Copie o arquivo `.env.example` para `.env`:

```bash
cp .env.example .env
```

Edite o arquivo `.env` com a URL da sua API:

```env
VITE_API_URL=http://localhost:3000/api
```

## 🚀 Desenvolvimento

```bash
npm run dev
```

O servidor de desenvolvimento inicia em `http://localhost:3001`

## 📦 Build

```bash
npm run build
```

Os arquivos de produção são gerados na pasta `dist/`.

## 🔗 Rotas

- `/album/:token` - Visualiza o álbum de uma criança via token

## 🛡️ Segurança

- ✅ **Sem login necessário** (apenas token)
- ✅ **Interface colorida e amigável**
- ✅ **Galeria responsiva** (mobile/desktop)
- ✅ **Fullscreen de fotos**
- ✅ **Player de vídeo**
- ✅ **Slideshow automático**
- ✅ **Proteção contra download** (user-select: none, no-drag)

## 🌐 Deploy

### Vercel (recomendado)

```bash
npm install -g vercel
vercel login
vercel deploy --prod
```

### Outras opções

- Netlify
- Firebase Hosting
- GitHub Pages

## 🔑 Token de Acesso

A página pública é acessada via token único de 32 caracteres:

```
https://seusite.com/album/A1B2C3D4E5F6...
```

O token é gerado no app Android quando uma criança é criada.

## 📱 API Endpoints

### GET `/api/public/album/:token`

Retorna o álbum de uma criança (público, sem autenticação).

**Response:**
```json
{
  "crianca": {
    "_id": "...",
    "nome": "João",
    "fotoPerfil": "..."
  },
  "midias": [
    {
      "_id": "...",
      "tipo": "foto",
      "url": "...",
      "descricao": "...",
      "dataMomento": "..."
    }
  ]
}
```

## 🎨 Cores

| Cor | Hex | Uso |
|-----|-----|-----|
| Primary 500 | #3b82f6 | Botões principais |
| Primary 600 | #2563eb | Hover states |
| Kids Pink | #ff9eb5 | Destaques |
| Kids Purple | #c9b1ff | Backgrounds |

## 📄 Licença

Privado - Álbum Seguro
