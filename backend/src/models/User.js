const mongoose = require('mongoose');
const bcryptjs = require('bcryptjs');

const userSchema = new mongoose.Schema({
  nome: {
    type: String,
    required: [true, 'Nome é obrigatório'],
    trim: true,
    minlength: 3,
  },
  email: {
    type: String,
    required: [true, 'Email é obrigatório'],
    unique: true,
    lowercase: true,
    match: [/^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w{2,3})+$/, 'Email inválido'],
  },
  senha: {
    type: String,
    required: [true, 'Senha é obrigatória'],
    minlength: 6,
    select: false,
  },
  telefone: {
    type: String,
    match: [/^(\+55)?[1-9]{2}9?[6-9]\d{3}-?\d{4}$/, 'Telefone inválido'],
  },
  role: {
    type: String,
    enum: ['admin', 'super_admin'],
    default: 'admin',
  },
  permissoes: {
    type: [String],
    default: ['criar_crianca', 'gerenciar_midia'],
  },
  ativo: {
    type: Boolean,
    default: true,
  },
  verificadoEmail: {
    type: Boolean,
    default: false,
  },
  dataCriacao: {
    type: Date,
    default: Date.now,
  },
  dataUltimoAcesso: Date,
  tokenRecuperacaoSenha: String,
  expiracaoTokenRecuperacao: Date,
}, { timestamps: true });

// Hash senha antes de salvar
userSchema.pre('save', async function(next) {
  if (!this.isModified('senha')) return next();

  try {
    const salt = await bcryptjs.genSalt(10);
    this.senha = await bcryptjs.hash(this.senha, salt);
    next();
  } catch (error) {
    next(error);
  }
});

// Método para comparar senhas
userSchema.methods.compararSenha = async function(senhaInformada) {
  return await bcryptjs.compare(senhaInformada, this.senha);
};

// Remover senha da saída JSON
userSchema.methods.toJSON = function() {
  const { senha, ...usuario } = this.toObject();
  return usuario;
};

module.exports = mongoose.model('User', userSchema);
