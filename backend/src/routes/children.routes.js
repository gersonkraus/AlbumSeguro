const express = require('express');
const multer = require('multer');
const { body, validationResult } = require('express-validator');
const Child = require('../models/Child');
const { authMiddleware, adminMiddleware } = require('../middleware/auth');
const { gerarTokenAcesso } = require('../utils/helpers');
const Log = require('../models/Log');
const { storageService } = require('../services/storageService');
const uploadFoto = multer({ storage: multer.memoryStorage(), limits: { fileSize: 10 * 1024 * 1024 } });

const router = express.Router();

// CRIAR criança
router.post('/', authMiddleware, adminMiddleware, [
  body('nome').trim().isLength({ min: 2 }),
  body('dataNascimento').isISO8601(),
], async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }

    const { nome, dataNascimento, descricao } = req.body;

    const novaCrianca = new Child({
      nome,
      dataNascimento,
      descricao,
      criadoPor: req.user._id,
      historicoPessoas: [req.user._id],
    });

    await novaCrianca.save();

    await Log.create({
      usuarioId: req.user._id,
      acao: 'CRIAR_CRIANCA',
      recursoId: novaCrianca._id,
      status: 'sucesso',
      ipAddress: req.ip,
    });

    res.status(201).json({
      message: 'Criança criada com sucesso',
      crianca: novaCrianca,
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// LISTAR crianças
router.get('/', authMiddleware, adminMiddleware, async (req, res) => {
  try {
    const criancas = await Child.find({ ativo: true })
      .populate('criadoPor', 'nome email')
      .sort({ dataCriacao: -1 });

    res.json({ criancas });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// OBTER criança por ID
router.get('/:id', authMiddleware, adminMiddleware, async (req, res) => {
  try {
    const crianca = await Child.findById(req.params.id)
      .populate('criadoPor', 'nome email')
      .populate('historicoPessoas', 'nome email');

    if (!crianca) {
      return res.status(404).json({ error: 'Criança não encontrada' });
    }

    res.json({ crianca });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// EDITAR criança
router.put('/:id', authMiddleware, adminMiddleware, async (req, res) => {
  try {
    const { nome, descricao, dataNascimento } = req.body;

    const crianca = await Child.findByIdAndUpdate(
      req.params.id,
      { nome, descricao, dataNascimento },
      { new: true, runValidators: true }
    );

    if (!crianca) {
      return res.status(404).json({ error: 'Criança não encontrada' });
    }

    await Log.create({
      usuarioId: req.user._id,
      acao: 'EDITAR_CRIANCA',
      recursoId: crianca._id,
      status: 'sucesso',
      ipAddress: req.ip,
    });

    res.json({
      message: 'Criança atualizada com sucesso',
      crianca,
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// DELETAR criança (soft delete)
router.delete('/:id', authMiddleware, adminMiddleware, async (req, res) => {
  try {
    const crianca = await Child.findByIdAndUpdate(
      req.params.id,
      { ativo: false },
      { new: true }
    );

    if (!crianca) {
      return res.status(404).json({ error: 'Criança não encontrada' });
    }

    await Log.create({
      usuarioId: req.user._id,
      acao: 'DELETAR_CRIANCA',
      recursoId: crianca._id,
      status: 'sucesso',
      ipAddress: req.ip,
    });

    res.json({ message: 'Criança deletada com sucesso' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// FOTO DE PERFIL DA CRIANÇA
router.post('/:id/foto', authMiddleware, adminMiddleware, uploadFoto.single('foto'), async (req, res) => {
  try {
    if (!req.file) return res.status(400).json({ error: 'Nenhuma foto enviada' });
    const crianca = await Child.findById(req.params.id);
    if (!crianca) return res.status(404).json({ error: 'Criança não encontrada' });

    const url = await storageService.uploadArquivo(
      req.file,
      `perfis/${req.params.id}/foto_${Date.now()}`
    );
    crianca.fotoPerfil = url;
    await crianca.save();

    res.json({ message: 'Foto atualizada', fotoPerfil: url });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// GERAR TOKEN DE ACESSO
router.post('/:id/gerar-token', authMiddleware, adminMiddleware, async (req, res) => {
  try {
    const crianca = await Child.findById(req.params.id);

    if (!crianca) {
      return res.status(404).json({ error: 'Criança não encontrada' });
    }

    const diasValidade = parseInt(req.body.diasValidade) || 30;
    crianca.tokenAcesso = gerarTokenAcesso();
    crianca.tokenExpiracao = new Date(Date.now() + diasValidade * 24 * 60 * 60 * 1000);
    await crianca.save();

    await Log.create({
      usuarioId: req.user._id,
      acao: 'GERAR_TOKEN',
      recursoId: crianca._id,
      status: 'sucesso',
      ipAddress: req.ip,
    });

    res.json({
      message: 'Token gerado com sucesso',
      token: crianca.tokenAcesso,
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
