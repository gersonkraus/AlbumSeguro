import axios, { AxiosInstance } from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:3000/api';

const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
});

// Interceptor para logs em desenvolvimento
apiClient.interceptors.request.use(
  (config) => {
    if (import.meta.env.DEV) {
      console.log(`[API Request] ${config.method?.toUpperCase()} ${config.url}`);
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (import.meta.env.DEV) {
      console.error('[API Error]', error.response?.data || error.message);
    }
    return Promise.reject(error);
  }
);

// APIs públicas (não precisam de autenticação)
export const publicApi = {
  // Validar token de acesso
  validateToken: async (token: string) => {
    const response = await apiClient.get(`/public/validate-token/${token}`);
    return response.data;
  },

  // Buscar álbum por token
  getAlbum: async (token: string) => {
    const response = await apiClient.get(`/public/album/${token}`);
    return response.data;
  },

  // Buscar álbum (rota alternativa /api/album/:token)
  getAlbumLegacy: async (token: string) => {
    const response = await apiClient.get(`/album/${token}`);
    return response.data;
  },
};

export default apiClient;
