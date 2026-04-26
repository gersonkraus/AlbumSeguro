const mongoose = require('mongoose');

const childSchema = new mongoose.Schema({
  nome: {
    type: String,
    required: [true, 'Nome é obrigatório'],
    trim: true,
  },
  dataNascimento: {
    type: Date,
    required: [true, 'Data de nascimento é obrigatória'],
  },
  fotoPerfil: {
    type: String,
    default: null,
  },
  descricao: String,
  tokenAcesso: {
    type: String,
    unique: true,
    sparse: true,
    default: null,
  },
  tokenExpiracao: {
    type: Date,
    default: null,
  },
  ativo: {
    type: Boolean,
    default: true,
  },
  dataCriacao: {
    type: Date,
    default: Date.now,
  },
  criadoPor: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true,
  },
  historicoPessoas: [{
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
  }],
}, { timestamps: true });

// Índices
childSchema.index({ tokenAcesso: 1 });
childSchema.index({ criadoPor: 1 });

module.exports = mongoose.model('Child', childSchema);
