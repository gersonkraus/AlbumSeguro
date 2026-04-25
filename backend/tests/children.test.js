const request = require('supertest');
const app = require('../src/app');
const User = require('../src/models/User');
const Child = require('../src/models/Child');

let adminToken;

beforeAll(async () => {
  await request(app).post('/api/auth/registrar')
    .send({ nome: 'Admin', email: 'admin@children-test.com', senha: 'senha123' });
  const res = await request(app).post('/api/auth/login')
    .send({ email: 'admin@children-test.com', senha: 'senha123' });
  adminToken = res.body.token;
});

afterEach(async () => {
  await Child.deleteMany({});
});

describe('POST /api/children', () => {
  it('should create a child', async () => {
    const res = await request(app).post('/api/children')
      .set('Authorization', `Bearer ${adminToken}`)
      .send({ nome: 'Criança Teste', dataNascimento: '2015-06-15' });
    expect(res.status).toBe(201);
    expect(res.body.crianca.nome).toBe('Criança Teste');
    expect(res.body.crianca.tokenAcesso).toBeNull();
  });

  it('should reject unauthenticated request', async () => {
    const res = await request(app).post('/api/children')
      .send({ nome: 'Criança', dataNascimento: '2015-06-15' });
    expect(res.status).toBe(401);
  });
});

describe('GET /api/children', () => {
  beforeEach(async () => {
    await request(app).post('/api/children')
      .set('Authorization', `Bearer ${adminToken}`)
      .send({ nome: 'Criança A', dataNascimento: '2015-01-01' });
  });

  it('should list active children', async () => {
    const res = await request(app).get('/api/children')
      .set('Authorization', `Bearer ${adminToken}`);
    expect(res.status).toBe(200);
    expect(res.body.criancas.length).toBeGreaterThan(0);
  });
});

describe('PUT /api/children/:id', () => {
  it('should update a child', async () => {
    const create = await request(app).post('/api/children')
      .set('Authorization', `Bearer ${adminToken}`)
      .send({ nome: 'Criança Edit', dataNascimento: '2015-01-01' });
    const id = create.body.crianca._id;
    const res = await request(app).put(`/api/children/${id}`)
      .set('Authorization', `Bearer ${adminToken}`)
      .send({ nome: 'Novo Nome', dataNascimento: '2015-01-01' });
    expect(res.status).toBe(200);
    expect(res.body.crianca.nome).toBe('Novo Nome');
  });
});

describe('DELETE /api/children/:id', () => {
  it('should soft-delete a child', async () => {
    const create = await request(app).post('/api/children')
      .set('Authorization', `Bearer ${adminToken}`)
      .send({ nome: 'Criança Delete', dataNascimento: '2016-01-01' });
    const id = create.body.crianca._id;
    const res = await request(app).delete(`/api/children/${id}`)
      .set('Authorization', `Bearer ${adminToken}`);
    expect(res.status).toBe(200);
    // Should not appear in list (ativo: false)
    const list = await request(app).get('/api/children')
      .set('Authorization', `Bearer ${adminToken}`);
    expect(list.body.criancas.find(c => c._id === id)).toBeUndefined();
  });
});

describe('POST /api/children/:id/gerar-token', () => {
  it('should generate a 32-char uppercase hex token', async () => {
    const create = await request(app).post('/api/children')
      .set('Authorization', `Bearer ${adminToken}`)
      .send({ nome: 'Criança Token', dataNascimento: '2016-03-10' });
    const id = create.body.crianca._id;
    const res = await request(app).post(`/api/children/${id}/gerar-token`)
      .set('Authorization', `Bearer ${adminToken}`);
    expect(res.status).toBe(200);
    expect(res.body.token).toMatch(/^[A-F0-9]{32}$/);
  });
});

describe('GET /api/album/:token (public endpoint)', () => {
  let childToken;

  beforeEach(async () => {
    // Clean up and create fresh child with token for each test
    await Child.deleteMany({});
    const create = await request(app).post('/api/children')
      .set('Authorization', `Bearer ${adminToken}`)
      .send({ nome: 'Criança Album', dataNascimento: '2017-05-20' });
    const id = create.body.crianca._id;
    const tokenRes = await request(app).post(`/api/children/${id}/gerar-token`)
      .set('Authorization', `Bearer ${adminToken}`);
    childToken = tokenRes.body.token;
  });

  it('should return child album for valid token (no auth required)', async () => {
    const res = await request(app).get(`/api/album/${childToken}`);
    expect(res.status).toBe(200);
    expect(res.body.crianca.nome).toBe('Criança Album');
    expect(Array.isArray(res.body.midias)).toBe(true);
  });

  it('should return 404 for unknown token', async () => {
    const res = await request(app).get('/api/album/AAAABBBBCCCCDDDDAAAABBBBCCCCDDDD');
    expect(res.status).toBe(404);
  });

  it('should return 400 for invalid token length', async () => {
    const res = await request(app).get('/api/album/SHORT');
    expect(res.status).toBe(400);
  });

  it('should not require Authorization header', async () => {
    const res = await request(app)
      .get(`/api/album/${childToken}`)
      // deliberately no Authorization header
    expect(res.status).toBe(200);
  });
});
