const errorHandler = (err, req, res, next) => {
  console.error('Erro:', err.message);

  // Erro de validação Mongoose
  if (err.name === 'ValidationError') {
    const messages = Object.values(err.errors).map(e => e.message);
    return res.status(400).json({ error: 'Erro de validação', detalhes: messages });
  }

  // Erro de chave duplicada
  if (err.code === 11000) {
    const campo = Object.keys(err.keyPattern)[0];
    return res.status(400).json({ error: `${campo} já existe` });
  }

  // Erro padrão
  res.status(err.status || 500).json({
    error: err.message || 'Erro interno do servidor',
  });
};

module.exports = { errorHandler };
