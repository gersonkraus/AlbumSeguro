const mongoose = require('mongoose');

const logSchema = new mongoose.Schema({
  usuarioId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
  },
  acao: {
    type: String,
    enum: ['LOGIN', 'LOGOUT', 'CRIAR_CRIANCA', 'EDITAR_CRIANCA', 'DELETAR_CRIANCA',
           'UPLOAD_MIDIA', 'DELETAR_MIDIA', 'CRIAR_ADMIN', 'EDITAR_ADMIN',
           'ACESSAR_ALBUM', 'GERAR_TOKEN', 'EDITAR_CONFIG_APP'],
    required: true,
  },
  recursoId: mongoose.Schema.Types.ObjectId,
  detalhes: String,
  ipAddress: String,
  userAgent: String,
  status: {
    type: String,
    enum: ['sucesso', 'erro'],
    default: 'sucesso',
  },
  dataCriacao: {
    type: Date,
    default: Date.now,
  },
}, { timestamps: false });

// Índices
logSchema.index({ dataCriacao: -1 });
logSchema.index({ usuarioId: 1 });
logSchema.index({ acao: 1 });

module.exports = mongoose.model('Log', logSchema);
