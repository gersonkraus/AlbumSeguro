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
    if (!firebaseInitialized) {
      throw new Error('Firebase não configurado: verifique FIREBASE_CREDENTIALS e FIREBASE_STORAGE_BUCKET no .env');
    }
    try {
      const storageBucket = admin.storage().bucket();
      const file = storageBucket.file(caminho);

      await file.save(arquivo.buffer, {
        metadata: {
          contentType: arquivo.mimetype,
        },
        public: true,
      });

      const bucketName = process.env.FIREBASE_STORAGE_BUCKET;
      const encodedPath = encodeURIComponent(caminho);
      const url = `https://firebasestorage.googleapis.com/v0/b/${bucketName}/o/${encodedPath}?alt=media`;

      return url;
    } catch (error) {
      console.error('Erro ao fazer upload:', error);
      throw new Error(`Erro Firebase: ${error.message}`);
    }
  },

  deletarArquivo: async (url) => {
    initFirebase();
    try {
      // Firebase Storage URLs: .../o/ENCODED_PATH?alt=media
      const urlObj = new URL(url);
      const encodedPath = urlObj.pathname.split('/o/')[1];
      if (!encodedPath) return;
      const caminho = decodeURIComponent(encodedPath.split('?')[0]);
      const bucket = admin.storage().bucket();
      const file = bucket.file(caminho);
      await file.delete();
    } catch (error) {
      console.error('Erro ao deletar arquivo:', error);
    }
  },
};

module.exports = { storageService };
