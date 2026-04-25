const errorHandler = (err, req, res, next) => {
  res.status(err.status || 500).json({ error: err.message || 'Erro interno do servidor' });
};
module.exports = { errorHandler };
