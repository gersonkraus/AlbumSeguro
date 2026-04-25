const express = require('express');
const Log = require('../models/Log');
const { authMiddleware, superAdminMiddleware } = require('../middleware/auth');

const router = express.Router();

// LISTAR logs
router.get('/', authMiddleware, superAdminMiddleware, async (req, res) => {
  try {
    const { limite = 100, pagina = 1, acao, usuarioId } = req.query;

    const filtros = {};
    if (acao) filtros.acao = acao;
    if (usuarioId) filtros.usuarioId = usuarioId;

    const logs = await Log.find(filtros)
      .populate('usuarioId', 'nome email')
      .sort({ dataCriacao: -1 })
      .limit(limite * 1)
      .skip((pagina - 1) * limite);

    const total = await Log.countDocuments(filtros);

    res.json({
      logs,
      paginacao: {
        total,
        pagina: pagina * 1,
        limitePorPagina: limite * 1,
        totalPaginas: Math.ceil(total / limite),
      },
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
