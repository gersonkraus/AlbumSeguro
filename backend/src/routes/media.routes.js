const express = require('express');
const multer = require('multer');
const Media = require('../models/Media');
const Child = require('../models/Child');
const { authMiddleware, adminMiddleware } = require('../middleware/auth');
const { storageService } = require('../services/storageService');
const Log = require('../models/Log');

const router = express.Router();

// Configurar multer
const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: 50 * 1024 * 1024 }, // 50MB
  fileFilter: (req, file, cb) => {
    const tiposPermitidos = /jpeg|jpg|png|gif|webp|heic|heif|mp4|mov|avi|mkv|quicktime|octet-stream/;
    const permitido = tiposPermitidos.test(file.mimetype);
    if (permitido) {
      return cb(null, true);
    } else {
      return cb(new Error(`Tipo de arquivo não permitido: ${file.mimetype}`));
    }
  },
});

// FAZER UPLOAD de mídia
router.post('/:criancaId/upload', authMiddleware, adminMiddleware, upload.single('file'), async (req, res) => {
  try {
    const { criancaId } = req.params;
    const { descricao, dataMomento } = req.body;

    const crianca = await Child.findById(criancaId);
    if (!crianca) {
      return res.status(404).json({ error: 'Criança não encontrada' });
    }

    const urlDownload = await storageService.uploadArquivo(
      req.file,
      `criancas/${criancaId}/${Date.now()}_${req.file.originalname}`
    );

    const novaMidia = new Media({
      criancaId,
      tipo: req.file.mimetype.includes('video') ? 'video' : 'foto',
      url: urlDownload,
      descricao,
      dataMomento: dataMomento || new Date(),
      cadastroPor: req.user._id,
      tamanho: req.file.size,
    });

    await novaMidia.save();

    if (!crianca.historicoPessoas.includes(req.user._id)) {
      crianca.historicoPessoas.push(req.user._id);
      await crianca.save();
    }

    await Log.create({
      usuarioId: req.user._id,
      acao: 'UPLOAD_MIDIA',
      recursoId: novaMidia._id,
      status: 'sucesso',
      ipAddress: req.ip,
    });

    res.status(201).json({
      message: 'Arquivo enviado com sucesso',
      midia: novaMidia,
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// LISTAR mídia de uma criança
router.get('/:criancaId', authMiddleware, async (req, res) => {
  try {
    const { criancaId } = req.params;
    const { tipo, ordem = 'desc', dataInicio, dataFim } = req.query;

    const crianca = await Child.findById(criancaId);
    if (!crianca) {
      return res.status(404).json({ error: 'Criança não encontrada' });
    }

    const filtro = { criancaId };
    if (tipo && tipo !== 'todos') filtro.tipo = tipo;
    if (dataInicio || dataFim) {
      filtro.dataMomento = {};
      if (dataInicio) filtro.dataMomento.$gte = new Date(dataInicio);
      if (dataFim) filtro.dataMomento.$lte = new Date(dataFim);
    }

    const sortMap = { asc: { dataMomento: 1 }, desc: { dataMomento: -1 }, tamanho: { tamanho: -1 } };
    const sort = sortMap[ordem] || { dataMomento: -1 };

    const midias = await Media.find(filtro)
      .populate('cadastroPor', 'nome')
      .sort(sort);

    res.json({ midias });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// EDITAR mídia (descrição)
router.put('/:midiaId', authMiddleware, adminMiddleware, async (req, res) => {
  try {
    const midia = await Media.findByIdAndUpdate(
      req.params.midiaId,
      { descricao: req.body.descricao },
      { new: true }
    );
    if (!midia) return res.status(404).json({ error: 'Mídia não encontrada' });
    res.json({ message: 'Mídia atualizada', midia });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// DELETAR mídia
router.delete('/:midiaId', authMiddleware, adminMiddleware, async (req, res) => {
  try {
    const midia = await Media.findById(req.params.midiaId);

    if (!midia) {
      return res.status(404).json({ error: 'Mídia não encontrada' });
    }

    await storageService.deletarArquivo(midia.url);
    await Media.findByIdAndDelete(req.params.midiaId);

    await Log.create({
      usuarioId: req.user._id,
      acao: 'DELETAR_MIDIA',
      recursoId: req.params.midiaId,
      status: 'sucesso',
      ipAddress: req.ip,
    });

    res.json({ message: 'Mídia deletada com sucesso' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
