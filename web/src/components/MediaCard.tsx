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
