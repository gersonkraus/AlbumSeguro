# Frontend Redesign — AlbumSeguro Web — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Redesenhar o frontend web do AlbumSeguro com paleta calorosa, componentes separados e swipe touch no lightbox.

**Architecture:** O `AlbumPage.tsx` (410 linhas) é dividido em 4 componentes (`AlbumHeader`, `GalleryGrid`, `MediaCard`, `Lightbox`) + 1 hook (`useSwipe`). A paleta de cores e tipografia são substituídas no `tailwind.config.js` e `index.html`. Nenhuma dependência nova de runtime é adicionada.

**Tech Stack:** React 18, TypeScript, Tailwind CSS 3, Vite, date-fns 3, react-icons

---

## Mapa de arquivos

| Ação | Arquivo |
|---|---|
| Modificar | `web/index.html` |
| Modificar | `web/tailwind.config.js` |
| Modificar | `web/src/pages/AlbumPage.tsx` |
| Modificar | `web/src/pages/NotFoundPage.tsx` |
| Criar | `web/src/hooks/useSwipe.ts` |
| Criar | `web/src/components/AlbumHeader.tsx` |
| Criar | `web/src/components/MediaCard.tsx` |
| Criar | `web/src/components/GalleryGrid.tsx` |
| Criar | `web/src/components/Lightbox.tsx` |

---

## Task 1: Tipografia e paleta de cores

**Files:**
- Modify: `web/index.html`
- Modify: `web/tailwind.config.js`

- [ ] **Step 1: Adicionar fonte Nunito no index.html**

Substituir o conteúdo de `web/index.html` por:

```html
<!doctype html>
<html lang="pt-BR">
  <head>
    <meta charset="UTF-8" />
    <link rel="icon" type="image/svg+xml" href="/vite.svg" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link rel="preconnect" href="https://fonts.googleapis.com" />
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
    <link href="https://fonts.googleapis.com/css2?family=Nunito:wght@400;600;700;800&display=swap" rel="stylesheet" />
    <title>Álbum Seguro</title>
  </head>
  <body>
    <div id="root"></div>
    <script type="module" src="/src/main.tsx"></script>
  </body>
</html>
```

- [ ] **Step 2: Substituir paleta no tailwind.config.js**

Substituir o conteúdo de `web/tailwind.config.js` por:

```js
/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50:  '#fff3e8',
          100: '#ffe8d0',
          200: '#ffd0a8',
          300: '#ffcca0',
          400: '#ffaa6e',
          500: '#ff8c42',
          600: '#e67530',
          700: '#c45e20',
          800: '#9c4a18',
          900: '#7a3a12',
        },
        accent: {
          100: '#d4f5d8',
          500: '#6bcb77',
          600: '#4db85a',
        },
        surface: '#fffbf7',
        'surface-variant': '#fdf3eb',
        'on-surface': '#2d1a0e',
        'on-surface-variant': '#7a5c45',
        outline: '#f0d9c8',
        error: '#d93025',
      },
      fontFamily: {
        sans: ['Nunito', 'system-ui', '-apple-system', 'sans-serif'],
      },
      boxShadow: {
        'elevation-1': '0 2px 8px rgba(255,140,66,0.12), 0 1px 3px rgba(45,26,14,0.08)',
        'elevation-2': '0 4px 16px rgba(255,140,66,0.16), 0 2px 6px rgba(45,26,14,0.10)',
        'elevation-3': '0 8px 24px rgba(255,140,66,0.18), 0 4px 8px rgba(45,26,14,0.12)',
      },
      borderRadius: {
        '2xl': '16px',
      },
      animation: {
        'fade-in': 'fadeIn 0.2s ease-in-out',
        'fade-in-up': 'fadeInUp 0.3s ease-out both',
        'slide-up': 'slideUp 0.3s ease-out',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        fadeInUp: {
          '0%': { opacity: '0', transform: 'translateY(12px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        slideUp: {
          '0%': { transform: 'translateY(16px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
      },
    },
  },
  plugins: [],
}
```

- [ ] **Step 3: Verificar que o Vite compila sem erros**

```bash
cd /home/gerson/AlbumSeguro/web && npm run build 2>&1 | tail -20
```

Esperado: `✓ built in` sem erros de TypeScript ou Tailwind.

- [ ] **Step 4: Commit**

```bash
cd /home/gerson/AlbumSeguro/web && git -C .. add index.html tailwind.config.js && git -C .. commit -m "style: paleta quente + fonte Nunito (redesign frontend)"
```

---

## Task 2: Hook useSwipe

**Files:**
- Create: `web/src/hooks/useSwipe.ts`

- [ ] **Step 1: Criar o hook**

Criar `web/src/hooks/useSwipe.ts`:

```ts
import { useCallback, useRef } from 'react';

interface UseSwipeOptions {
  onSwipeLeft?: () => void;
  onSwipeRight?: () => void;
  threshold?: number;
}

export function useSwipe({ onSwipeLeft, onSwipeRight, threshold = 50 }: UseSwipeOptions) {
  const touchStartX = useRef<number | null>(null);
  const touchStartY = useRef<number | null>(null);

  const ref = useCallback((el: HTMLElement | null) => {
    if (!el) return;

    const handleTouchStart = (e: TouchEvent) => {
      touchStartX.current = e.touches[0].clientX;
      touchStartY.current = e.touches[0].clientY;
    };

    const handleTouchEnd = (e: TouchEvent) => {
      if (touchStartX.current === null || touchStartY.current === null) return;

      const deltaX = e.changedTouches[0].clientX - touchStartX.current;
      const deltaY = e.changedTouches[0].clientY - touchStartY.current;

      // Ignorar swipes predominantemente verticais
      if (Math.abs(deltaY) > Math.abs(deltaX)) return;

      if (deltaX < -threshold) onSwipeLeft?.();
      else if (deltaX > threshold) onSwipeRight?.();

      touchStartX.current = null;
      touchStartY.current = null;
    };

    el.addEventListener('touchstart', handleTouchStart, { passive: true });
    el.addEventListener('touchend', handleTouchEnd, { passive: true });

    return () => {
      el.removeEventListener('touchstart', handleTouchStart);
      el.removeEventListener('touchend', handleTouchEnd);
    };
  }, [onSwipeLeft, onSwipeRight, threshold]);

  return { ref };
}
```

- [ ] **Step 2: Verificar tipagem**

```bash
cd /home/gerson/AlbumSeguro/web && npx tsc --noEmit 2>&1 | head -20
```

Esperado: sem erros.

- [ ] **Step 3: Commit**

```bash
git -C /home/gerson/AlbumSeguro add web/src/hooks/useSwipe.ts && git -C /home/gerson/AlbumSeguro commit -m "feat: hook useSwipe para gestos touch no lightbox"
```

---

## Task 3: Componente AlbumHeader

**Files:**
- Create: `web/src/components/AlbumHeader.tsx`

- [ ] **Step 1: Criar o componente**

Criar `web/src/components/AlbumHeader.tsx`:

```tsx
import React from 'react';
import { FiImage, FiVideo } from 'react-icons/fi';

interface CriancaDTO {
  _id: string;
  nome: string;
  fotoPerfil?: string;
  descricao?: string;
}

interface AlbumHeaderProps {
  crianca: CriancaDTO;
  photoCount: number;
  videoCount: number;
}

const AlbumHeader: React.FC<AlbumHeaderProps> = ({ crianca, photoCount, videoCount }) => {
  return (
    <header className="sticky top-0 z-40 bg-gradient-to-b from-[#fffbf7] to-white border-b border-outline shadow-elevation-1">
      <div className="max-w-6xl mx-auto px-4 sm:px-6">
        <div className="flex items-center justify-between h-16">
          <div className="flex items-center gap-3">
            {crianca.fotoPerfil ? (
              <img
                src={crianca.fotoPerfil}
                alt={crianca.nome}
                className="w-12 h-12 rounded-full object-cover ring-2 ring-primary-300"
              />
            ) : (
              <div className="w-12 h-12 rounded-full bg-primary-100 ring-2 ring-primary-300 flex items-center justify-center text-primary-500 font-bold text-lg">
                {crianca.nome.charAt(0).toUpperCase()}
              </div>
            )}
            <div>
              <h1 className="text-base font-bold text-on-surface leading-tight">Álbum Seguro</h1>
              <p className="text-xs text-on-surface-variant leading-tight">
                {crianca.nome} · <span className="text-primary-500">Seu álbum especial ✨</span>
              </p>
            </div>
          </div>

          <div className="hidden sm:flex items-center gap-2">
            {photoCount > 0 && (
              <span className="inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-semibold bg-primary-100 text-primary-500 border border-primary-200">
                <FiImage className="w-3 h-3" />
                {photoCount}
              </span>
            )}
            {videoCount > 0 && (
              <span className="inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-semibold bg-accent-100 text-accent-600 border border-accent-100">
                <FiVideo className="w-3 h-3" />
                {videoCount}
              </span>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};

export default AlbumHeader;
```

- [ ] **Step 2: Verificar tipagem**

```bash
cd /home/gerson/AlbumSeguro/web && npx tsc --noEmit 2>&1 | head -20
```

Esperado: sem erros.

- [ ] **Step 3: Commit**

```bash
git -C /home/gerson/AlbumSeguro add web/src/components/AlbumHeader.tsx && git -C /home/gerson/AlbumSeguro commit -m "feat: componente AlbumHeader com paleta quente e subtítulo acolhedor"
```

---

## Task 4: Componente MediaCard

**Files:**
- Create: `web/src/components/MediaCard.tsx`

- [ ] **Step 1: Criar o componente**

Criar `web/src/components/MediaCard.tsx`:

```tsx
import React from 'react';
import { FiPlay } from 'react-icons/fi';
import { format } from 'date-fns';
import { ptBR } from 'date-fns/locale';

interface MediaDTO {
  _id: string;
  tipo: 'foto' | 'video';
  url: string;
  thumbnailUrl?: string;
  descricao?: string;
  dataMomento: string;
  duracao?: number;
}

interface MediaCardProps {
  midia: MediaDTO;
  index: number;
  onClick: () => void;
}

function formatDuracao(segundos: number): string {
  const m = Math.floor(segundos / 60);
  const s = segundos % 60;
  return `${m}:${s.toString().padStart(2, '0')}`;
}

const MediaCard: React.FC<MediaCardProps> = ({ midia, index, onClick }) => {
  const animDelay = index < 20 ? `${index * 40}ms` : '0ms';

  const dataFormatada = (() => {
    try {
      return format(new Date(midia.dataMomento), "dd 'de' MMMM 'de' yyyy", { locale: ptBR });
    } catch {
      return '';
    }
  })();

  return (
    <div
      onClick={onClick}
      className="group relative aspect-square rounded-2xl overflow-hidden cursor-pointer bg-primary-50 shadow-elevation-1 hover:shadow-elevation-2 transition-shadow duration-200 animate-fade-in-up"
      style={{ animationDelay: animDelay }}
    >
      {midia.tipo === 'foto' ? (
        <img
          src={midia.url}
          alt={midia.descricao || 'Foto'}
          className="w-full h-full object-cover transition-transform duration-200 group-hover:scale-105"
          loading="lazy"
          draggable={false}
        />
      ) : (
        <div className="w-full h-full bg-on-surface relative">
          <video
            src={midia.url}
            className="w-full h-full object-cover opacity-80"
            preload="metadata"
          />
          <div className="absolute inset-0 flex items-center justify-center bg-black/20 group-hover:bg-black/30 transition-colors">
            <div className="w-12 h-12 rounded-full bg-primary-500 flex items-center justify-center shadow-elevation-2">
              <FiPlay className="w-5 h-5 text-white ml-0.5" />
            </div>
          </div>
          {midia.duracao !== undefined && (
            <span className="absolute bottom-2 right-2 bg-black/60 text-white text-xs px-1.5 py-0.5 rounded font-semibold">
              {formatDuracao(midia.duracao)}
            </span>
          )}
        </div>
      )}

      <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-200">
        <div className="absolute bottom-2 left-2 right-2">
          {midia.descricao && (
            <p className="text-white text-xs line-clamp-2 font-semibold mb-0.5">
              {midia.descricao}
            </p>
          )}
          {dataFormatada && (
            <p className="text-white/70 text-xs">
              {dataFormatada}
            </p>
          )}
        </div>
      </div>
    </div>
  );
};

export default MediaCard;
```

- [ ] **Step 2: Verificar tipagem**

```bash
cd /home/gerson/AlbumSeguro/web && npx tsc --noEmit 2>&1 | head -20
```

Esperado: sem erros.

- [ ] **Step 3: Commit**

```bash
git -C /home/gerson/AlbumSeguro add web/src/components/MediaCard.tsx && git -C /home/gerson/AlbumSeguro commit -m "feat: componente MediaCard com animação stagger, overlay data e badge de vídeo laranja"
```

---

## Task 5: Componente Lightbox

**Files:**
- Create: `web/src/components/Lightbox.tsx`

- [ ] **Step 1: Criar o componente**

Criar `web/src/components/Lightbox.tsx`:

```tsx
import React, { useEffect, useRef, useState } from 'react';
import { FiX, FiChevronLeft, FiChevronRight, FiPlay, FiPause } from 'react-icons/fi';
import { useSwipe } from '../hooks/useSwipe';

interface MediaDTO {
  _id: string;
  tipo: 'foto' | 'video';
  url: string;
  descricao?: string;
  dataMomento: string;
}

interface LightboxProps {
  midias: MediaDTO[];
  currentIndex: number;
  slideshow: boolean;
  onClose: () => void;
  onNext: () => void;
  onPrev: () => void;
  onToggleSlideshow: () => void;
  onJumpTo: (index: number) => void;
}

const Lightbox: React.FC<LightboxProps> = ({
  midias,
  currentIndex,
  slideshow,
  onClose,
  onNext,
  onPrev,
  onToggleSlideshow,
  onJumpTo,
}) => {
  const selectedMedia = midias[currentIndex];
  const isPhoto = selectedMedia.tipo === 'foto';
  const [transitioning, setTransitioning] = useState(false);
  const [slideDir, setSlideDir] = useState<'left' | 'right' | null>(null);
  const containerRef = useRef<HTMLDivElement>(null);

  const triggerTransition = (dir: 'left' | 'right', callback: () => void) => {
    if (!isPhoto) { callback(); return; }
    setSlideDir(dir);
    setTransitioning(true);
    setTimeout(() => {
      callback();
      setTransitioning(false);
      setSlideDir(null);
    }, 200);
  };

  const handleNext = () => triggerTransition('left', onNext);
  const handlePrev = () => triggerTransition('right', onPrev);

  const { ref: swipeRef } = useSwipe(
    isPhoto
      ? { onSwipeLeft: handleNext, onSwipeRight: handlePrev, threshold: 50 }
      : {}
  );

  // Combinar refs
  const setRef = (el: HTMLDivElement | null) => {
    (containerRef as React.MutableRefObject<HTMLDivElement | null>).current = el;
    swipeRef(el);
  };

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      switch (e.key) {
        case 'Escape': onClose(); break;
        case 'ArrowRight': handleNext(); break;
        case 'ArrowLeft': handlePrev(); break;
        case ' ': e.preventDefault(); onToggleSlideshow(); break;
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [onClose, handleNext, handlePrev, onToggleSlideshow]);

  const translateClass = transitioning
    ? slideDir === 'left' ? '-translate-x-8 opacity-0' : 'translate-x-8 opacity-0'
    : 'translate-x-0 opacity-100';

  return (
    <div
      className="fixed inset-0 z-50 bg-[#0f0a07] flex flex-col animate-fade-in"
      onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}
    >
      {/* Top bar */}
      <div className="flex items-center justify-between px-4 py-3 flex-shrink-0">
        <span className="text-white/60 text-sm tabular-nums font-semibold">
          {currentIndex + 1} / {midias.length}
        </span>
        <div className="flex items-center gap-2">
          {midias.length > 1 && (
            <button
              onClick={(e) => { e.stopPropagation(); onToggleSlideshow(); }}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-semibold transition-colors ${
                slideshow
                  ? 'bg-primary-500 text-white'
                  : 'bg-white/10 hover:bg-white/20 text-white'
              }`}
            >
              {slideshow ? <FiPause className="w-3.5 h-3.5" /> : <FiPlay className="w-3.5 h-3.5" />}
              {slideshow ? 'Pausar' : 'Slideshow'}
            </button>
          )}
          <button
            onClick={onClose}
            className="w-10 h-10 rounded-full bg-white/10 hover:bg-white/20 flex items-center justify-center text-white transition-colors"
          >
            <FiX className="w-5 h-5" />
          </button>
        </div>
      </div>

      {/* Media area */}
      <div
        ref={setRef}
        className="flex-1 flex items-center justify-center relative min-h-0 px-4"
      >
        {midias.length > 1 && (
          <>
            <button
              onClick={(e) => { e.stopPropagation(); handlePrev(); }}
              className="absolute left-2 sm:left-4 z-10 w-12 h-12 rounded-full bg-white/15 hover:bg-white/25 flex items-center justify-center text-white transition-colors"
            >
              <FiChevronLeft className="w-6 h-6" />
            </button>
            <button
              onClick={(e) => { e.stopPropagation(); handleNext(); }}
              className="absolute right-2 sm:right-4 z-10 w-12 h-12 rounded-full bg-white/15 hover:bg-white/25 flex items-center justify-center text-white transition-colors"
            >
              <FiChevronRight className="w-6 h-6" />
            </button>
          </>
        )}

        <div className={`transition-all duration-200 max-w-full max-h-full ${translateClass}`}>
          {isPhoto ? (
            <img
              src={selectedMedia.url}
              alt={selectedMedia.descricao || 'Foto'}
              className="max-w-full max-h-full object-contain rounded-xl select-none"
              draggable={false}
            />
          ) : (
            <video
              src={selectedMedia.url}
              controls
              autoPlay
              className="max-w-full max-h-full rounded-xl"
              onClick={(e) => e.stopPropagation()}
            />
          )}
        </div>
      </div>

      {/* Bottom bar */}
      <div className="flex-shrink-0 px-4 py-4">
        {selectedMedia.descricao && (
          <p className="text-white/80 text-sm text-center mb-3 max-w-lg mx-auto leading-relaxed">
            {selectedMedia.descricao}
          </p>
        )}
        {midias.length > 1 && midias.length <= 20 && (
          <div className="flex justify-center gap-1.5">
            {midias.map((_, i) => (
              <button
                key={i}
                onClick={(e) => { e.stopPropagation(); onJumpTo(i); }}
                className={`rounded-full transition-all duration-200 ${
                  i === currentIndex
                    ? 'w-5 h-1.5 bg-primary-400'
                    : 'w-1.5 h-1.5 bg-white/25 hover:bg-white/50'
                }`}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default Lightbox;
```

- [ ] **Step 2: Verificar tipagem**

```bash
cd /home/gerson/AlbumSeguro/web && npx tsc --noEmit 2>&1 | head -20
```

Esperado: sem erros.

- [ ] **Step 3: Commit**

```bash
git -C /home/gerson/AlbumSeguro add web/src/components/Lightbox.tsx && git -C /home/gerson/AlbumSeguro commit -m "feat: componente Lightbox com swipe touch, transição slideX e dots laranja"
```

---

## Task 6: Componente GalleryGrid

**Files:**
- Create: `web/src/components/GalleryGrid.tsx`

- [ ] **Step 1: Criar o componente**

Criar `web/src/components/GalleryGrid.tsx`:

```tsx
import React, { useState, useCallback, useEffect } from 'react';
import { FiImage } from 'react-icons/fi';
import MediaCard from './MediaCard';
import Lightbox from './Lightbox';

interface MediaDTO {
  _id: string;
  tipo: 'foto' | 'video';
  url: string;
  thumbnailUrl?: string;
  descricao?: string;
  dataMomento: string;
  duracao?: number;
}

interface GalleryGridProps {
  midias: MediaDTO[];
  nomecrianças: string;
}

const GalleryGrid: React.FC<GalleryGridProps> = ({ midias, nomecrianças }) => {
  const [selectedIndex, setSelectedIndex] = useState<number | null>(null);
  const [slideshow, setSlideshow] = useState(false);

  const openMedia = (index: number) => {
    setSelectedIndex(index);
    setSlideshow(false);
  };

  const closeMedia = () => {
    setSelectedIndex(null);
    setSlideshow(false);
  };

  const nextMedia = useCallback(() => {
    setSelectedIndex((prev) => prev === null ? null : (prev + 1) % midias.length);
  }, [midias.length]);

  const prevMedia = useCallback(() => {
    setSelectedIndex((prev) => prev === null ? null : (prev - 1 + midias.length) % midias.length);
  }, [midias.length]);

  const jumpTo = (index: number) => setSelectedIndex(index);
  const toggleSlideshow = () => setSlideshow((prev) => !prev);

  useEffect(() => {
    if (!slideshow || selectedIndex === null || !midias.length) return;
    const timer = setInterval(nextMedia, 3000);
    return () => clearInterval(timer);
  }, [slideshow, selectedIndex, midias.length, nextMedia]);

  if (midias.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-24 text-center">
        <div className="w-20 h-20 bg-primary-50 rounded-full flex items-center justify-center mb-5">
          <FiImage className="w-9 h-9 text-primary-300" />
        </div>
        <p className="text-on-surface font-bold mb-1">
          Nenhuma memória ainda, {nomecrianças}.
        </p>
        <p className="text-on-surface-variant text-sm">Em breve suas fotos aparecerão aqui 💛</p>
      </div>
    );
  }

  return (
    <>
      <p className="text-xs font-semibold text-on-surface-variant uppercase tracking-wider mb-4">
        {midias.length} {midias.length === 1 ? 'item' : 'itens'}
      </p>

      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-3">
        {midias.map((midia, index) => (
          <MediaCard
            key={midia._id}
            midia={midia}
            index={index}
            onClick={() => openMedia(index)}
          />
        ))}
      </div>

      {selectedIndex !== null && (
        <Lightbox
          midias={midias}
          currentIndex={selectedIndex}
          slideshow={slideshow}
          onClose={closeMedia}
          onNext={nextMedia}
          onPrev={prevMedia}
          onToggleSlideshow={toggleSlideshow}
          onJumpTo={jumpTo}
        />
      )}
    </>
  );
};

export default GalleryGrid;
```

- [ ] **Step 2: Verificar tipagem**

```bash
cd /home/gerson/AlbumSeguro/web && npx tsc --noEmit 2>&1 | head -20
```

Esperado: sem erros.

- [ ] **Step 3: Commit**

```bash
git -C /home/gerson/AlbumSeguro add web/src/components/GalleryGrid.tsx && git -C /home/gerson/AlbumSeguro commit -m "feat: componente GalleryGrid com estado vazio personalizado e slideshow"
```

---

## Task 7: Refatorar AlbumPage.tsx

**Files:**
- Modify: `web/src/pages/AlbumPage.tsx`

- [ ] **Step 1: Substituir o conteúdo de AlbumPage.tsx**

Substituir o conteúdo de `web/src/pages/AlbumPage.tsx` por:

```tsx
import React, { useEffect, useState, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import { publicApi } from '../api/apiClient';
import { FiAlertCircle, FiRefreshCw } from 'react-icons/fi';
import AlbumHeader from '../components/AlbumHeader';
import GalleryGrid from '../components/GalleryGrid';

interface MediaDTO {
  _id: string;
  tipo: 'foto' | 'video';
  url: string;
  thumbnailUrl?: string;
  descricao?: string;
  dataMomento: string;
  duracao?: number;
}

interface CriancaDTO {
  _id: string;
  nome: string;
  fotoPerfil?: string;
  descricao?: string;
}

interface AlbumData {
  crianca: CriancaDTO;
  midias: MediaDTO[];
}

const AlbumPage: React.FC = () => {
  const { token } = useParams<{ token: string }>();
  const [album, setAlbum] = useState<AlbumData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const carregarAlbum = useCallback(async () => {
    if (!token) {
      setError('Token não fornecido. Verifique o link do álbum.');
      setLoading(false);
      return;
    }
    try {
      setLoading(true);
      setError(null);
      const data = await publicApi.getAlbum(token);
      setAlbum(data);
    } catch (err: any) {
      const msg = err.response?.data?.error || 'Erro ao carregar álbum';
      const isExpired = msg.toLowerCase().includes('expir');
      setError(
        isExpired
          ? 'Seu link de acesso expirou. Peça um novo para seu responsável. 💛'
          : msg
      );
    } finally {
      setLoading(false);
    }
  }, [token]);

  useEffect(() => { carregarAlbum(); }, [carregarAlbum]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-surface">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-4 border-primary-100 border-t-primary-500 mx-auto mb-4"></div>
          <p className="text-on-surface-variant text-sm font-semibold tracking-wide">Carregando álbum...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center p-6 bg-surface">
        <div className="text-center max-w-sm w-full">
          <div className="w-16 h-16 bg-primary-50 rounded-full flex items-center justify-center mx-auto mb-5">
            <FiAlertCircle className="w-8 h-8 text-primary-500" />
          </div>
          <h1 className="text-xl font-bold text-on-surface mb-2">Algo deu errado</h1>
          <p className="text-on-surface-variant text-sm mb-6 leading-relaxed">{error}</p>
          <button
            onClick={carregarAlbum}
            className="inline-flex items-center gap-2 px-6 py-2.5 bg-primary-500 text-white rounded-full text-sm font-semibold hover:bg-primary-400 transition-colors shadow-elevation-1 hover:shadow-elevation-2"
          >
            <FiRefreshCw className="w-4 h-4" />
            Tentar novamente
          </button>
        </div>
      </div>
    );
  }

  if (!album) {
    return (
      <div className="min-h-screen flex items-center justify-center p-6 bg-surface">
        <p className="text-on-surface-variant">Nenhum álbum encontrado.</p>
      </div>
    );
  }

  const { crianca, midias } = album;
  const photoCount = midias.filter((m) => m.tipo === 'foto').length;
  const videoCount = midias.filter((m) => m.tipo === 'video').length;

  return (
    <div className="min-h-screen bg-surface">
      <AlbumHeader crianca={crianca} photoCount={photoCount} videoCount={videoCount} />

      <main className="max-w-6xl mx-auto px-4 sm:px-6 py-6">
        <GalleryGrid midias={midias} nomecrianças={crianca.nome} />
      </main>

      <footer className="border-t border-outline mt-12 bg-surface">
        <div className="max-w-6xl mx-auto px-4 py-5 text-center">
          <p className="text-on-surface-variant text-xs tracking-wide">
            Álbum Seguro · Guardando memórias com carinho 💛
          </p>
        </div>
      </footer>
    </div>
  );
};

export default AlbumPage;
```

- [ ] **Step 2: Verificar tipagem e build**

```bash
cd /home/gerson/AlbumSeguro/web && npx tsc --noEmit 2>&1 | head -30
```

Esperado: sem erros.

```bash
cd /home/gerson/AlbumSeguro/web && npm run build 2>&1 | tail -20
```

Esperado: `✓ built in` sem erros.

- [ ] **Step 3: Commit**

```bash
git -C /home/gerson/AlbumSeguro add web/src/pages/AlbumPage.tsx && git -C /home/gerson/AlbumSeguro commit -m "refactor: AlbumPage dividido em AlbumHeader + GalleryGrid + Lightbox + MediaCard"
```

---

## Task 8: Atualizar NotFoundPage

**Files:**
- Modify: `web/src/pages/NotFoundPage.tsx`

- [ ] **Step 1: Substituir o conteúdo de NotFoundPage.tsx**

Substituir o conteúdo de `web/src/pages/NotFoundPage.tsx` por:

```tsx
import React from 'react';
import { FiAlertCircle } from 'react-icons/fi';

const NotFoundPage: React.FC = () => {
  return (
    <div className="min-h-screen flex items-center justify-center bg-surface p-6">
      <div className="text-center max-w-sm w-full">
        <div className="w-20 h-20 bg-primary-50 rounded-full flex items-center justify-center mx-auto mb-6">
          <FiAlertCircle className="w-9 h-9 text-primary-500" />
        </div>

        <p className="text-5xl font-extrabold text-primary-200 mb-4 tracking-tight">404</p>
        <h1 className="text-xl font-bold text-on-surface mb-3">Esta página não existe</h1>

        <p className="text-on-surface-variant text-sm leading-relaxed">
          O link pode estar quebrado ou o álbum foi removido.
        </p>
      </div>
    </div>
  );
};

export default NotFoundPage;
```

- [ ] **Step 2: Build final e verificação**

```bash
cd /home/gerson/AlbumSeguro/web && npm run build 2>&1 | tail -10
```

Esperado: `✓ built in` sem erros ou avisos críticos.

- [ ] **Step 3: Testar em dev server**

```bash
cd /home/gerson/AlbumSeguro/web && npm run dev
```

Abrir `http://localhost:5173/album/TOKEN_QUALQUER` e verificar:
- Paleta laranja/creme visível
- Fonte Nunito carregada
- Grid com `rounded-2xl`
- Página 404 com nova mensagem

- [ ] **Step 4: Commit final**

```bash
git -C /home/gerson/AlbumSeguro add web/src/pages/NotFoundPage.tsx && git -C /home/gerson/AlbumSeguro commit -m "style: NotFoundPage com paleta quente e mensagem atualizada"
```
