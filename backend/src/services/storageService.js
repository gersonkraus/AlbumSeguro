const admin = require('firebase-admin');

// Inicializar Firebase (usar credenciais de variável de ambiente)
let firebaseInitialized = false;

const initFirebase = () => {
  if (firebaseInitialized) return;
  const serviceAccount = JSON.parse(process.env.FIREBASE_CREDENTIALS || '{}');
  if (Object.keys(serviceAccount).length > 0) {
    admin.initializeApp({
      credential: admin.credential.cert(serviceAccount),
      storageBucket: process.env.FIREBASE_STORAGE_BUCKET,
    });
    firebaseInitialized = true;
  }
};

const storageService = {
  uploadArquivo: async (arquivo, caminho) => {
    initFirebase();
    try {
      const bucket = admin.storage().bucket();
      const file = bucket.file(caminho);

      await file.save(arquivo.buffer, {
        metadata: {
          contentType: arquivo.mimetype,
        },
      });

      const [url] = await file.getSignedUrl({
        version: 'v4',
        action: 'read',
        expires: Date.now() + 30 * 24 * 60 * 60 * 1000,
      });

      return url;
    } catch (error) {
      console.error('Erro ao fazer upload:', error);
      throw new Error('Erro ao fazer upload do arquivo');
    }
  },

  deletarArquivo: async (url) => {
    initFirebase();
    try {
      const caminho = new URL(url).pathname.split('/').pop();
      const bucket = admin.storage().bucket();
      const file = bucket.file(caminho);
      await file.delete();
    } catch (error) {
      console.error('Erro ao deletar arquivo:', error);
    }
  },
};

module.exports = { storageService };
