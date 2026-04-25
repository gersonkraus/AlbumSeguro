# Checklist de Testes

## Backend (Jest)

Executar todos os testes:
```bash
cd backend && npm test
```

Testes existentes (21 no total):
- **auth.test.js** — register, login, profile, 401 sem token
- **children.test.js** — CRUD, geração de token, acesso público ao álbum
- **media.test.js** — listar mídia, delete, 404 para criança desconhecida

## Testes Manuais — Autenticação

- [ ] `POST /api/auth/registrar` cria admin e retorna sem senha
- [ ] `POST /api/auth/login` retorna JWT
- [ ] `GET /api/auth/perfil` com token válido retorna usuário
- [ ] `GET /api/auth/perfil` sem token retorna 401
- [ ] `POST /api/auth/logout` registra log

## Testes Manuais — Crianças

- [ ] Criar criança com nome e data de nascimento
- [ ] Listar crianças (somente ativas)
- [ ] Editar criança
- [ ] Deletar criança (soft delete — não aparece na listagem)
- [ ] Gerar token: resultado é 32 caracteres hex maiúsculos

## Testes Manuais — Mídia

- [ ] Upload de foto (JPEG/PNG, até 50MB)
- [ ] Upload de vídeo (MP4/MOV/AVI)
- [ ] Listar mídias de uma criança
- [ ] Deletar mídia
- [ ] Tipo inválido rejeitado

## Testes Manuais — Álbum Público

- [ ] `GET /api/album/:token` sem Authorization retorna álbum
- [ ] Token com menos de 32 chars retorna 400
- [ ] Token inexistente retorna 404
- [ ] ACESSAR_ALBUM registrado no log

## Testes Android (emulador)

- [ ] Tela de login aparece ao abrir o app
- [ ] Login com credenciais válidas navega para Dashboard
- [ ] Login com senha errada exibe erro
- [ ] Criar criança via formulário
- [ ] Gerar token na tela de detalhe da criança
- [ ] Acessar álbum digitando o token
- [ ] Scanner QR abre câmera e pede permissão
- [ ] Album exibe grid de mídias

## Segurança

- [ ] HTTPS configurado em produção
- [ ] Rate limiting ativo (100 req/15min)
- [ ] JWT expirado retorna 401
- [ ] Senha não aparece em nenhuma resposta da API
- [ ] Soft-delete preserva histórico no banco
