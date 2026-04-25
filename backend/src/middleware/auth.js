const jwt = require('jsonwebtoken');
const User = require('../models/User');

const authMiddleware = async (req, res, next) => {
  try {
    const token = req.headers.authorization?.split(' ')[1];

    if (!token) {
      return res.status(401).json({ error: 'Token não fornecido' });
    }

    const decoded = jwt.verify(token, process.env.JWT_SECRET || 'seu-secret-key');
    const user = await User.findById(decoded.id);

    if (!user || !user.ativo) {
      return res.status(401).json({ error: 'Usuário não encontrado ou inativo' });
    }

    req.user = user;
    next();
  } catch (error) {
    return res.status(401).json({ error: 'Token inválido' });
  }
};

const adminMiddleware = (req, res, next) => {
  if (!req.user || (req.user.role !== 'admin' && req.user.role !== 'super_admin')) {
    return res.status(403).json({ error: 'Acesso negado. Admin requerido' });
  }
  next();
};

const superAdminMiddleware = (req, res, next) => {
  if (!req.user || req.user.role !== 'super_admin') {
    return res.status(403).json({ error: 'Acesso negado. Super Admin requerido' });
  }
  next();
};

const requirePermission = (permission) => {
  return (req, res, next) => {
    if (!req.user.permissoes.includes(permission)) {
      return res.status(403).json({ error: `Permissão '${permission}' requerida` });
    }
    next();
  };
};

module.exports = { authMiddleware, adminMiddleware, superAdminMiddleware, requirePermission };
