const express = require('express');
const { body, validationResult } = require('express-validator');
const jwt = require('jsonwebtoken');
const User = require('../models/User');
const { authMiddleware } = require('../middleware/auth');
const Log = require('../models/Log');

const router = express.Router();

// Registrar novo admin
router.post('/registrar', [
  body('nome').trim().isLength({ min: 3 }),
  body('email').isEmail().normalizeEmail(),
  body('senha').isLength({ min: 6 }),
], async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }

    const { nome, email, senha, telefone } = req.body;

    const usuarioExistente = await User.findOne({ email });
    if (usuarioExistente) {
      return res.status(400).json({ error: 'Email já cadastrado' });
    }

    const novoUsuario = new User({
      nome,
      email,
      senha,
      telefone,
      role: 'admin',
      permissoes: ['criar_crianca', 'gerenciar_midia'],
    });

    await novoUsuario.save();

    await Log.create({
      usuarioId: null,
      acao: 'CRIAR_ADMIN',
      recursoId: novoUsuario._id,
      status: 'sucesso',
      ipAddress: req.ip,
    });

    res.status(201).json({
      message: 'Admin criado com sucesso',
      usuario: novoUsuario.toJSON(),
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Refresh token
router.post('/refresh', async (req, res) => {
  try {
    const { refreshToken } = req.body;
    if (!refreshToken) return res.status(401).json({ error: 'Refresh token não fornecido' });

    const decoded = jwt.verify(refreshToken, process.env.JWT_REFRESH_SECRET || 'refresh-secret-key');
    const usuario = await User.findById(decoded.id);
    if (!usuario || !usuario.ativo) return res.status(401).json({ error: 'Usuário inválido' });

    const accessToken = jwt.sign(
      { id: usuario._id },
      process.env.JWT_SECRET || 'seu-secret-key',
      { expiresIn: '1h' }
    );

    res.json({ token: accessToken });
  } catch (error) {
    res.status(401).json({ error: 'Refresh token inválido ou expirado' });
  }
});

// Login
router.post('/login', [
  body('email').isEmail(),
  body('senha').isLength({ min: 6 }),
], async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }

    const { email, senha } = req.body;

    const usuario = await User.findOne({ email }).select('+senha');
    if (!usuario) {
      return res.status(400).json({ error: 'Email ou senha incorretos' });
    }

    const senhaValida = await usuario.compararSenha(senha);
    if (!senhaValida) {
      return res.status(400).json({ error: 'Email ou senha incorretos' });
    }

    const token = jwt.sign(
      { id: usuario._id },
      process.env.JWT_SECRET || 'seu-secret-key',
      { expiresIn: '1h' }
    );

    const refreshToken = jwt.sign(
      { id: usuario._id },
      process.env.JWT_REFRESH_SECRET || 'refresh-secret-key',
      { expiresIn: '30d' }
    );

    usuario.dataUltimoAcesso = new Date();
    await usuario.save();

    await Log.create({
      usuarioId: usuario._id,
      acao: 'LOGIN',
      status: 'sucesso',
      ipAddress: req.ip,
    });

    res.json({
      message: 'Login realizado com sucesso',
      token,
      refreshToken,
      usuario: usuario.toJSON(),
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Perfil
router.get('/perfil', authMiddleware, (req, res) => {
  res.json({ usuario: req.user.toJSON() });
});

// Atualizar perfil
router.put('/perfil', authMiddleware, async (req, res) => {
  try {
    const { nome, telefone } = req.body;
    const usuario = await User.findByIdAndUpdate(
      req.user._id,
      { nome, telefone },
      { new: true, runValidators: true }
    );
    res.json({ usuario: usuario.toJSON() });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Alterar senha
router.put('/senha', authMiddleware, async (req, res) => {
  try {
    const { senhaAtual, novaSenha } = req.body;
    if (!senhaAtual || !novaSenha || novaSenha.length < 6) {
      return res.status(400).json({ error: 'Dados inválidos' });
    }
    const usuario = await User.findById(req.user._id).select('+senha');
    const senhaValida = await usuario.compararSenha(senhaAtual);
    if (!senhaValida) return res.status(400).json({ error: 'Senha atual incorreta' });
    usuario.senha = novaSenha;
    await usuario.save();
    res.json({ message: 'Senha alterada com sucesso' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Logout
router.post('/logout', authMiddleware, async (req, res) => {
  try {
    await Log.create({
      usuarioId: req.user._id,
      acao: 'LOGOUT',
      status: 'sucesso',
      ipAddress: req.ip,
    });

    res.json({ message: 'Logout realizado com sucesso' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

module.exports = router;
