const express = require('express');
const User = require('../models/User');
const AppConfig = require('../models/AppConfig');
const { authMiddleware, adminMiddleware, superAdminMiddleware } = require('../middleware/auth');
const Log = require('../models/Log');
const AppUpdate = require('../models/AppUpdate');
const multer = require('multer');
const path = require('path');
const fs = require('fs');

const apkStorage = multer.diskStorage({
  destination: (req, file, cb) => {
    const dir = path.join(__dirname, '../../uploads/apk');
    fs.mkdirSync(dir, { recursive: true });
    cb(null, dir);
  },
  filename: (req, file, cb) => {
    cb(null, 'app-nicollas.apk');
  },
});
const uploadApk = multer({
  storage: apkStorage,
  limits: { fileSize: 100 * 1024 * 1024 },
  fileFilter: (req, file, cb) => {
    if (
      file.mimetype === 'application/vnd.android.package-archive' ||
      file.originalname.endsWith('.apk')
    ) {
      cb(null, true);
    } else {
      cb(new Error('Apenas arquivos .apk são permitidos'));
    }
  },
});

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

/**
 * Upload de nova versão do APK do Nicollas
 * POST /api/admin/app-update
 */
router.post(
  '/app-update',
  authMiddleware,
  adminMiddleware,
  uploadApk.single('apk'),
  async (req, res) => {
    try {
      if (!req.file) {
        return res.status(400).json({ error: 'Arquivo APK é obrigatório' });
      }
      const versionCode = parseInt(req.body.versionCode, 10);
      const versionName = req.body.versionName?.trim();
      const notes = req.body.notes?.trim() || '';

      if (!versionCode || versionCode < 1) {
        return res.status(400).json({ error: 'versionCode deve ser um número inteiro positivo' });
      }
      if (!versionName) {
        return res.status(400).json({ error: 'versionName é obrigatório' });
      }

      const appUpdate = await AppUpdate.create({
        versionCode,
        versionName,
        apkFileName: req.file.filename,
        notes,
      });

      await Log.create({
        usuarioId: req.user._id,
        acao: 'UPLOAD_MIDIA',
        recursoId: appUpdate._id,
        status: 'sucesso',
        ipAddress: req.ip,
        userAgent: req.headers['user-agent'],
      });

      res.status(201).json({
        message: 'APK enviado com sucesso',
        versionCode: appUpdate.versionCode,
        versionName: appUpdate.versionName,
      });
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  }
);

module.exports = router;
