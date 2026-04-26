const request = require('supertest');
const app = require('../src/app');
const User = require('../src/models/User');

afterEach(async () => {
  await User.deleteMany({});
});

describe('POST /api/auth/registrar', () => {
  it('should create a new admin', async () => {
    const res = await request(app)
      .post('/api/auth/registrar')
      .send({ nome: 'Admin Teste', email: 'admin@test.com', senha: 'senha123' });
    expect(res.status).toBe(201);
    expect(res.body.usuario.email).toBe('admin@test.com');
    expect(res.body.usuario.senha).toBeUndefined();
  });

  it('should reject duplicate email', async () => {
    await request(app)
      .post('/api/auth/registrar')
      .send({ nome: 'Admin Teste', email: 'dup@test.com', senha: 'senha123' });
    const res = await request(app)
      .post('/api/auth/registrar')
      .send({ nome: 'Admin Dois', email: 'dup@test.com', senha: 'senha123' });
    expect(res.status).toBe(400);
    expect(res.body.error).toBeDefined();
  });
});

describe('POST /api/auth/login', () => {
  beforeEach(async () => {
    await request(app)
      .post('/api/auth/registrar')
      .send({ nome: 'Admin Login', email: 'login@test.com', senha: 'senha123' });
  });

  it('should return JWT on valid credentials', async () => {
    const res = await request(app)
      .post('/api/auth/login')
      .send({ email: 'login@test.com', senha: 'senha123' });
    expect(res.status).toBe(200);
    expect(res.body.token).toBeDefined();
    expect(res.body.usuario.email).toBe('login@test.com');
  });

  it('should reject wrong password', async () => {
    const res = await request(app)
      .post('/api/auth/login')
      .send({ email: 'login@test.com', senha: 'errada' });
    expect(res.status).toBe(400);
  });
});

describe('GET /api/auth/perfil', () => {
  let token;
  beforeEach(async () => {
    await request(app)
      .post('/api/auth/registrar')
      .send({ nome: 'Admin Perfil', email: 'perfil@test.com', senha: 'senha123' });
    const res = await request(app)
      .post('/api/auth/login')
      .send({ email: 'perfil@test.com', senha: 'senha123' });
    token = res.body.token;
  });

  it('should return profile for valid token', async () => {
    const res = await request(app)
      .get('/api/auth/perfil')
      .set('Authorization', `Bearer ${token}`);
    expect(res.status).toBe(200);
    expect(res.body.usuario.email).toBe('perfil@test.com');
  });

  it('should return 401 without token', async () => {
    const res = await request(app).get('/api/auth/perfil');
    expect(res.status).toBe(401);
  });
});
