const express = require('express');
const User = require('../models/User');
const { authMiddleware, superAdminMiddleware } = require('../middleware/auth');
const Log = require('../models/Log');

const router = express.Router();

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
