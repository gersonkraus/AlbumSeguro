import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import AlbumPage from './pages/AlbumPage';
import NotFoundPage from './pages/NotFoundPage';

const App: React.FC = () => {
  return (
    <BrowserRouter>
      <Routes>
        {/* Rota principal do álbum por token */}
        <Route path="/album/:token" element={<AlbumPage />} />
        
        {/* Redirecionar raiz para página de instruções */}
        <Route path="/" element={<Navigate to="/album/" replace />} />
        
        {/* Página não encontrada */}
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </BrowserRouter>
  );
};

export default App;
