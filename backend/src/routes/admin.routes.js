const express = require('express');
const User = require('../models/User');
const AppConfig = require('../models/AppConfig');
const { authMiddleware, superAdminMiddleware } = require('../middleware/auth');
const Log = require('../models/Log');

const router = express.Router();

const getOrCreateConfig = async () => {
  let config = await AppConfig.findOne();
  if (!config) {
    config = await AppConfig.create({ childAlbumBaseUrl: '' });
  }
  return config;
};

// LISTAR admins
router.get('/', authMiddleware, superAdminMiddleware, async (req, res) => {
  try {
    const admins = await User.find({ role: { $in: ['admin', 'super_admin'] } })
      .select('-senha')
      .sort({ dataCriacao: -1 });

    res.json({ admins });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// LER configuração global do app
router.get('/config', authMiddleware, superAdminMiddleware, async (req, res) => {
  try {
    const config = await getOrCreateConfig();
    res.json({ config });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// EDITAR configuração global do app
router.put('/config', authMiddleware, superAdminMiddleware, async (req, res) => {
  try {
    const { childAlbumBaseUrl } = req.body;

    if (typeof childAlbumBaseUrl !== 'string') {
      return res.status(400).json({ error: 'childAlbumBaseUrl deve ser uma string' });
    }

    const url = childAlbumBaseUrl.trim();
    if (url.length > 0) {
      try {
        const parsed = new URL(url);
        if (!['http:', 'https:'].includes(parsed.protocol)) {
          return res.status(400).json({ error: 'URL deve começar com http:// ou https://' });
        }
      } catch (_) {
        return res.status(400).json({ error: 'URL inválida' });
      }
    }

    const config = await getOrCreateConfig();
    config.childAlbumBaseUrl = url;
    await config.save();

    await Log.create({
      usuarioId: req.user._id,
      acao: 'EDITAR_CONFIG_APP',
      recursoId: config._id,
      status: 'sucesso',
      detalhes: `childAlbumBaseUrl: ${url || '(vazio)'}`,
      ipAddress: req.ip,
    });

    res.json({ message: 'Configuração atualizada com sucesso', config });
  } catch (error) {
    await Log.create({
      usuarioId: req.user?._id,
      acao: 'EDITAR_CONFIG_APP',
      status: 'erro',
      detalhes: error.message,
      ipAddress: req.ip,
    });
    res.status(500).json({ error: error.message });
  }
});

// EDITAR admin (permissões / ativo)
router.put('/:id', authMiddleware, superAdminMiddleware, async (req, res) => {
  try {
    const { permissoes, ativo } = req.body;

    const admin = await User.findByIdAndUpdate(
      req.params.id,
      { permissoes, ativo },
      { new: true, runValidators: true }
    );

    if (!admin) {
      return res.status(404).json({ error: 'Admin não encontrado' });
    }

    await Log.create({
      usuarioId: req.user._id,
      acao: 'EDITAR_ADMIN',
      recursoId: admin._id,
      status: 'sucesso',
      ipAddress: req.ip,
    });

    res.json({
      message: 'Admin atualizado com sucesso',
      admin: admin.toJSON(),
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// DESATIVAR admin
router.delete('/:id', authMiddleware, superAdminMiddleware, async (req, res) => {
  try {
    const admin = await User.findByIdAndUpdate(
      req.params.id,
      { ativo: false },
      { new: true }
    );

    if (!admin) {
      return res.status(404).json({ error: 'Admin não encontrado' });
    }

    await Log.create({
      usuarioId: req.user._id,
      acao: 'EDITAR_ADMIN',
      recursoId: admin._id,
      status: 'sucesso',
      ipAddress: req.ip,
    });

    res.json({ message: 'Admin desativado com sucesso' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
