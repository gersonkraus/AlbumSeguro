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
        <GalleryGrid midias={midias} nomeCrianca={crianca.nome} />
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
