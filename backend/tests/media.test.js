const request = require('supertest');
const mongoose = require('mongoose');
const app = require('../src/app');
const Child = require('../src/models/Child');
const Media = require('../src/models/Media');

// Mock Firebase storage — no real Firebase in tests
jest.mock('../src/services/storageService', () => ({
  storageService: {
    uploadArquivo: jest.fn().mockResolvedValue('https://storage.example.com/test.jpg'),
    deletarArquivo: jest.fn().mockResolvedValue(undefined),
  },
}));

let adminToken;
let childId;

beforeAll(async () => {
  await request(app).post('/api/auth/registrar')
    .send({ nome: 'Admin', email: 'admin@media-test.com', senha: 'senha123' });
  const res = await request(app).post('/api/auth/login')
    .send({ email: 'admin@media-test.com', senha: 'senha123' });
  adminToken = res.body.token;

  const child = await request(app).post('/api/children')
    .set('Authorization', `Bearer ${adminToken}`)
    .send({ nome: 'Criança Mídia', dataNascimento: '2018-07-04' });
  childId = child.body.crianca._id;
});

afterEach(async () => {
  await Media.deleteMany({});
});

describe('GET /api/media/:criancaId', () => {
  it('should return empty list initially', async () => {
    const res = await request(app).get(`/api/media/${childId}`)
      .set('Authorization', `Bearer ${adminToken}`);
    expect(res.status).toBe(200);
    expect(res.body.midias).toEqual([]);
  });

  it('should return 401 without token', async () => {
    const res = await request(app).get(`/api/media/${childId}`);
    expect(res.status).toBe(401);
  });

  it('should return 404 for unknown child', async () => {
    const fakeId = new mongoose.Types.ObjectId();
    const res = await request(app).get(`/api/media/${fakeId}`)
      .set('Authorization', `Bearer ${adminToken}`);
    expect(res.status).toBe(404);
  });
});

describe('DELETE /api/media/:midiaId', () => {
  it('should delete a media record', async () => {
    const media = await Media.create({
      criancaId: childId,
      tipo: 'foto',
      url: 'https://storage.example.com/foto.jpg',
      cadastroPor: new mongoose.Types.ObjectId(),
    });
    const res = await request(app).delete(`/api/media/${media._id}`)
      .set('Authorization', `Bearer ${adminToken}`);
    expect(res.status).toBe(200);
    expect(res.body.message).toBeDefined();
  });

  it('should return 404 for unknown media', async () => {
    const fakeId = new mongoose.Types.ObjectId();
    const res = await request(app).delete(`/api/media/${fakeId}`)
      .set('Authorization', `Bearer ${adminToken}`);
    expect(res.status).toBe(404);
  });
});
