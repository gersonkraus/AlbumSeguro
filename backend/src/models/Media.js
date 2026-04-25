const mongoose = require('mongoose');

const mediaSchema = new mongoose.Schema({
  criancaId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Child',
    required: true,
  },
  tipo: {
    type: String,
    enum: ['foto', 'video'],
    required: true,
  },
  url: {
    type: String,
    required: true,
  },
  thumbnailUrl: String,
  descricao: String,
  dataMomento: {
    type: Date,
    default: Date.now,
  },
  dataCadastro: {
    type: Date,
    default: Date.now,
  },
  cadastroPor: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true,
  },
  tamanho: Number,
  dimensoes: {
    largura: Number,
    altura: Number,
  },
  duracao: Number,
  tags: [String],
  privacidade: {
    type: String,
    enum: ['apenas_crianca', 'admins_e_crianca'],
    default: 'apenas_crianca',
  },
}, { timestamps: true });

// Índices
mediaSchema.index({ criancaId: 1 });
mediaSchema.index({ dataMomento: -1 });

module.exports = mongoose.model('Media', mediaSchema);
