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
