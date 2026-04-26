const express = require('express');
const Child = require('../models/Child');
const Media = require('../models/Media');
const Log = require('../models/Log');

const router = express.Router();

// PUBLIC endpoint — no auth required
// GET /api/album/:token
router.get('/:token', async (req, res) => {
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
      return res.status(404).json({ error: 'Álbum não encontrado' });
    }

    if (crianca.tokenExpiracao && new Date() > crianca.tokenExpiracao) {
      return res.status(403).json({ error: 'Link de acesso expirado. Solicite um novo link ao responsável.' });
    }

    const midias = await Media.find({
      criancaId: crianca._id,
      privacidade: { $in: ['apenas_crianca', 'admins_e_crianca'] },
    })
      .select('-cadastroPor')
      .sort({ dataMomento: -1 });

    await Log.create({
      usuarioId: null,
      acao: 'ACESSAR_ALBUM',
      recursoId: crianca._id,
      status: 'sucesso',
      ipAddress: req.ip,
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
