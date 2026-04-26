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
  nomeCrianca: string;
}

const GalleryGrid: React.FC<GalleryGridProps> = ({ midias, nomeCrianca }) => {
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
          Nenhuma memória ainda, {nomeCrianca}.
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
