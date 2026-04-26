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
  const containerRef = useRef<HTMLDivElement | null>(null);

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

  const setRef = (el: HTMLDivElement | null) => {
    containerRef.current = el;
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
