const mongoose = require('mongoose');

const connectDatabase = async () => {
  // Skip if already connected (e.g. during tests)
  if (mongoose.connection.readyState !== 0) return;

  try {
    const mongoURI = process.env.MONGODB_URI || 'mongodb://localhost:27017/familia-acolhedora';

    await mongoose.connect(mongoURI, {
      useNewUrlParser: true,
      useUnifiedTopology: true,
    });

    console.log('✅ MongoDB conectado com sucesso');
  } catch (error) {
    console.error('❌ Erro ao conectar MongoDB:', error.message);
    if (process.env.NODE_ENV !== 'test') {
      process.exit(1);
    }
  }
};

module.exports = { connectDatabase };
