const express = require('express');
const multer = require('multer');
const os = require('os');
const path = require('path');
const fs = require('fs');
const ffmpegInstaller = require('@ffmpeg-installer/ffmpeg');
const ffmpeg = require('fluent-ffmpeg');
const Media = require('../models/Media');
const Child = require('../models/Child');
const { authMiddleware, adminMiddleware } = require('../middleware/auth');
const { storageService } = require('../services/storageService');
const Log = require('../models/Log');

ffmpeg.setFfmpegPath(ffmpegInstaller.path);

async function gerarThumbnailComFfmpeg(buffer, criancaId) {
  const tmpVideo = path.join(os.tmpdir(), `vid_${Date.now()}.mp4`);
  const tmpThumb = path.join(os.tmpdir(), `thumb_${Date.now()}.jpg`);
  try {
    fs.writeFileSync(tmpVideo, buffer);
    await new Promise((resolve, reject) => {
      ffmpeg(tmpVideo)
        .screenshots({
          timestamps: ['00:00:01'],
          filename: path.basename(tmpThumb),
          folder: path.dirname(tmpThumb),
        })
        .on('end', resolve)
        .on('error', reject);
    });
    const thumbBuffer = fs.readFileSync(tmpThumb);
    return await storageService.uploadArquivo(
      { buffer: thumbBuffer, mimetype: 'image/jpeg', originalname: 'thumbnail.jpg' },
      `criancas/${criancaId}/thumbs/${Date.now()}_thumb.jpg`
    );
  } catch (_) {
    return null;
  } finally {
    try { fs.unlinkSync(tmpVideo); } catch (_) {}
    try { fs.unlinkSync(tmpThumb); } catch (_) {}
  }
}

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
router.post('/:criancaId/upload', authMiddleware, adminMiddleware, upload.fields([
  { name: 'file', maxCount: 1 },
  { name: 'thumbnail', maxCount: 1 },
]), async (req, res) => {
  try {
    const { criancaId } = req.params;
    const { descricao, dataMomento } = req.body;

    const videoFile = req.files['file']?.[0];
    const thumbnailFile = req.files['thumbnail']?.[0];

    if (!videoFile) {
      return res.status(400).json({ error: 'Arquivo não enviado' });
    }

    const crianca = await Child.findById(criancaId);
    if (!crianca) {
      return res.status(404).json({ error: 'Criança não encontrada' });
    }

    const urlDownload = await storageService.uploadArquivo(
      videoFile,
      `criancas/${criancaId}/${Date.now()}_${videoFile.originalname}`
    );

    const mimeType = (videoFile.mimetype || '').toLowerCase();
    const nomeArquivo = (videoFile.originalname || '').toLowerCase();
    const isVideoMime = mimeType.startsWith('video/');
    const isVideoByExt = /\.(mp4|mov|avi|mkv|webm|m4v)$/i.test(nomeArquivo);
    const isVideo = isVideoMime || isVideoByExt;

    let thumbnailUrl = null;
    if (isVideo) {
      if (thumbnailFile) {
        thumbnailUrl = await storageService.uploadArquivo(
          thumbnailFile,
          `criancas/${criancaId}/thumbs/${Date.now()}_thumb.jpg`
        );
      } else {
        thumbnailUrl = await gerarThumbnailComFfmpeg(videoFile.buffer, criancaId);
      }
    }

    const novaMidia = new Media({
      criancaId,
      tipo: isVideo ? 'video' : 'foto',
      url: urlDownload,
      thumbnailUrl,
      descricao,
      dataMomento: dataMomento || new Date(),
      cadastroPor: req.user._id,
      tamanho: videoFile.size,
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
    const { tipo, ordem = 'desc', dataInicio, dataFim, page = 0, limit = 30 } = req.query;
    const pageNum = Math.max(0, parseInt(page) || 0);
    const limitNum = Math.min(100, Math.max(1, parseInt(limit) || 30));

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

    const total = await Media.countDocuments(filtro);
    const midias = await Media.find(filtro)
      .populate('cadastroPor', 'nome')
      .sort(sort)
      .skip(pageNum * limitNum)
      .limit(limitNum);

    res.json({ midias, total, hasMore: (pageNum + 1) * limitNum < total });
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
