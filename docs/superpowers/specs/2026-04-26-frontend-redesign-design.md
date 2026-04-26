# Frontend Redesign — AlbumSeguro Web

**Data:** 2026-04-26  
**Status:** Aprovado  
**Escopo:** `web/` (React + TypeScript + Tailwind)

---

## Objetivo

Redesenhar o frontend web do AlbumSeguro para transmitir acolhimento, afeto e alegria às crianças que acessam seus álbuns. O redesign combina refatoração estrutural (componentes menores e mais coesos) com uma identidade visual calorosa e interações mobile-first.

---

## Arquitetura de Componentes

### Estrutura de arquivos

```
web/src/
├── pages/
│   ├── AlbumPage.tsx          # Orquestrador: fetch + estado global
│   └── NotFoundPage.tsx       # Paleta atualizada, mensagem mais acolhedora
├── components/
│   ├── AlbumHeader.tsx        # Avatar + nome + subtítulo + contadores
│   ├── GalleryGrid.tsx        # Grid de mídia + estado vazio personalizado
│   ├── MediaCard.tsx          # Card individual (foto ou vídeo)
│   └── Lightbox.tsx           # Modal fullscreen + swipe + navegação + slideshow
└── hooks/
    └── useSwipe.ts             # Gestos touch: onSwipeLeft / onSwipeRight
```

### Responsabilidades

- **`AlbumPage`** — único responsável pelo fetch de dados (`/public/album/:token`), gestão de estados de loading/error/empty, e passagem de `crianca` + `midias` para os filhos via props.
- **`AlbumHeader`** — recebe `crianca` (nome, fotoPerfil), `photoCount: number` e `videoCount: number` (calculados em `AlbumPage`). Stateless.
- **`GalleryGrid`** — recebe `midias[]`, gerencia qual índice está selecionado, abre/fecha o `Lightbox`.
- **`MediaCard`** — recebe `midia` + `onClick`. Renderiza foto ou vídeo com overlay de descrição+data.
- **`Lightbox`** — recebe `midias[]`, `currentIndex`, callbacks `onClose`/`onNext`/`onPrev`. Usa `useSwipe` internamente.
- **`useSwipe`** — hook puro. Recebe `{ onSwipeLeft, onSwipeRight, threshold?: number }`. Retorna `{ ref }` para anexar ao elemento alvo. Threshold padrão: 50px. Sem dependências externas.

---

## Identidade Visual

### Paleta de cores (substitui totalmente o `tailwind.config.js` atual)

| Token Tailwind | Valor hex | Uso principal |
|---|---|---|
| `primary-500` | `#ff8c42` | Botões CTA, avatar border, badges ativos |
| `primary-400` | `#ffaa6e` | Hover de botões |
| `primary-300` | `#ffcca0` | Estados intermediários |
| `primary-100` | `#ffe8d0` | Chips, badges |
| `primary-50` | `#fff3e8` | Fundos de seções, cards leves |
| `accent-500` | `#6bcb77` | Badge de vídeo, indicadores de sucesso |
| `accent-100` | `#d4f5d8` | Fundo do badge de vídeo |
| `surface` | `#fffbf7` | Fundo geral (creme quente) |
| `surface-variant` | `#fdf3eb` | Fundo alternativo suave |
| `on-surface` | `#2d1a0e` | Texto principal (marrom escuro) |
| `on-surface-variant` | `#7a5c45` | Texto secundário |
| `outline` | `#f0d9c8` | Bordas e divisores |
| `error` | `#d93025` | Estados de erro (mantido) |

### Tipografia

- **Família:** `Nunito` (Google Fonts) — arredondada, amigável, legível para crianças
- **Fallback:** `system-ui, -apple-system, sans-serif`
- **Import:** tag `<link>` no `index.html` com pesos 400, 600, 700, 800
- **Substituição:** remove `Roboto` e `Roboto Mono` do config

### Sombras

```js
'elevation-1': '0 2px 8px rgba(255,140,66,0.12), 0 1px 3px rgba(45,26,14,0.08)',
'elevation-2': '0 4px 16px rgba(255,140,66,0.16), 0 2px 6px rgba(45,26,14,0.10)',
'elevation-3': '0 8px 24px rgba(255,140,66,0.18), 0 4px 8px rgba(45,26,14,0.12)',
```

### Border-radius

- Cards da galeria: `rounded-2xl` (16px)
- Botões de ação: `rounded-full`
- Chips/badges: `rounded-full`
- Avatar: `rounded-full`

---

## Componentes — Especificação Detalhada

### AlbumHeader

- Avatar: `w-12 h-12`, borda `2px solid primary-300`, fallback com inicial em fundo `primary-100`
- Título fixo: `"Álbum Seguro"` em `font-bold`
- Subtítulo: `"Seu álbum especial ✨"` abaixo do nome da criança, em `text-xs text-on-surface-variant`
- Fundo do header: gradiente `from-[#fffbf7] to-white` (imperceptível, só suaviza)
- Contadores: chips com ícone — fotos em `primary-100/primary-500`, vídeos em `accent-100/accent-500`

### GalleryGrid

- Grid: `grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5`, gap `3`
- Animação de entrada: cada card com `animation: fadeInUp`, delay `index * 40ms`, máximo de 20 cards animados (resto aparece instantâneo)
- Estado vazio: ícone grande + `"Nenhuma memória ainda, {nome}."` + subtítulo `"Em breve suas fotos aparecerão aqui 💛"`

### MediaCard

- Aspect ratio: `aspect-square` (mantido)
- Border-radius: `rounded-2xl`
- Overlay no hover: gradiente `from-black/60 via-transparent to-transparent`, mostra `descricao` + data formatada (`dd 'de' MMMM 'de' yyyy` via `date-fns`)
- Badge de vídeo: círculo `bg-primary-500` com ícone play branco (substitui fundo cinza atual)
- Indicador de duração: se `midia.duracao` disponível, exibe `"0:45"` no canto inferior direito

### Lightbox

- Fundo: `bg-[#0f0a07]` (preto quente)
- Swipe: usa `useSwipe({ onSwipeLeft: nextMedia, onSwipeRight: prevMedia, threshold: 50 })` — aplicado apenas quando a mídia atual é `tipo: 'foto'`; vídeos desabilitam o swipe para não conflitar com os controles nativos do `<video>`
- Transição entre mídias: `transform translateX` animado 200ms (`-100%` → `0` → `+100%`) — apenas em fotos; troca de vídeo é instantânea
- Botões de navegação: `w-12 h-12` (maiores que os atuais `w-10 h-10`), fundo `white/15 hover:white/25`
- Dots de progresso: cor ativa `bg-primary-400` (laranja), inativa `bg-white/25`
- Botão slideshow ativo: fundo `bg-primary-500` (laranja, substitui verde atual)
- Top bar: exibe `{currentIndex+1} / {total}` + nome da mídia se disponível

### useSwipe

```ts
// Assinatura
function useSwipe(options: {
  onSwipeLeft?: () => void;
  onSwipeRight?: () => void;
  threshold?: number; // default: 50
}): { ref: React.RefCallback<HTMLElement> }
```

- Registra `touchstart` e `touchend` no elemento via `ref`
- Calcula delta X; se `|deltaX| > threshold`, chama o callback correspondente
- Ignora swipes verticais (delta Y > delta X)
- Faz cleanup dos listeners no unmount

### NotFoundPage

- Mesmo layout atual, paleta quente aplicada
- Mensagem: `"Esta página não existe"` + subtítulo `"O link pode estar quebrado ou o álbum foi removido."`
- Botão mantém estilo `rounded-full primary-500`

---

## Animações

Adicionar ao `tailwind.config.js`:

```js
keyframes: {
  fadeInUp: {
    '0%': { opacity: '0', transform: 'translateY(12px)' },
    '100%': { opacity: '1', transform: 'translateY(0)' },
  },
  // (manter fadeIn e slideUp existentes)
},
animation: {
  'fade-in-up': 'fadeInUp 0.3s ease-out both',
  // (manter existentes)
},
```

---

## Tratamento de Erros

- Telas de loading, error e empty: sem mudança estrutural, apenas paleta quente aplicada
- Botão "Tentar novamente": `rounded-full primary-500` com ícone `FiRefreshCw`
- Mensagem de erro token expirado: se `error` contém `"expirado"` ou `"expired"`, exibe mensagem especial `"Seu link de acesso expirou. Peça um novo para seu responsável. 💛"`

---

## Dependências

Sem novas dependências de runtime. Apenas:
- Fonte `Nunito` via Google Fonts CDN (tag no `index.html`) — zero bundle size
- `date-fns` já está instalado (`^3.0.0`) — usar para formatar datas no overlay

---

## O que NÃO muda

- Lógica de fetch (`apiClient.ts`) — intocada
- Roteamento (`App.tsx`) — intocado
- Navegação por teclado (Esc, ArrowLeft, ArrowRight, Space) — mantida
- Slideshow automático (3s) — mantido
- `preload="metadata"` nos vídeos — mantido
