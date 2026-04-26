const express = require('express');
const Child = require('../models/Child');
const Media = require('../models/Media');
const Log = require('../models/Log');

const router = express.Router();

/**
 * Validar token e retornar dados da criança
 * GET /api/public/validate-token/:token
 */
router.get('/validate-token/:token', async (req, res) => {
  try {
    const { token } = req.params;

    if (!token || token.length !== 32) {
      return res.status(400).json({ error: 'Token inválido' });
    }

    const crianca = await Child.findOne({
      tokenAcesso: token.toUpperCase(),
      ativo: true,
    });

    if (!crianca) {
      return res.status(404).json({ error: 'Token inválido ou criança não encontrada' });
    }

    res.json({
      criancaId: crianca._id,
      nome: crianca.nome,
      fotoPerfil: crianca.fotoPerfil,
      descricao: crianca.descricao,
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

/**
 * Listar mídia de uma criança (por token, sem auth)
 * GET /api/public/album/:token
 */
router.get('/album/:token', async (req, res) => {
  try {
    const { token } = req.params;

    if (!token || token.length !== 32) {
      return res.status(400).json({ error: 'Token inválido' });
    }

    // Validar token
    const crianca = await Child.findOne({
      tokenAcesso: token.toUpperCase(),
      ativo: true,
    });

    if (!crianca) {
      return res.status(403).json({ error: 'Token inválido ou criança não encontrada' });
    }

    // Retornar mídia
    const midias = await Media.find({
      criancaId: crianca._id,
      privacidade: { $in: ['apenas_crianca', 'admins_e_crianca'] },
    })
      .select('-cadastroPor')
      .sort({ dataMomento: -1 });

    // Registrar acesso
    await Log.create({
      usuarioId: null,
      acao: 'ACESSAR_ALBUM',
      recursoId: crianca._id,
      status: 'sucesso',
      ipAddress: req.ip,
      userAgent: req.headers['user-agent'],
    });

    res.json({
      crianca: {
        _id: crianca._id,
        nome: crianca.nome,
        fotoPerfil: crianca.fotoPerfil,
        descricao: crianca.descricao,
      },
      midias,
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
