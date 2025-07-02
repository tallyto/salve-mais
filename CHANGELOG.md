# Changelog - Gestor Financeiro (Backend)

## [Unreleased]

- Implementação de autenticação JWT (login, geração e validação de token)
- Cadastro de usuário com endpoint dedicado
- Proteção de endpoints com Spring Security
- Criação do controller de autenticação e usuário
- Integração com banco de dados para persistência de usuários
- Estruturação inicial do projeto Spring Boot
- Logout (remoção do token e redirecionamento)
- Expiração automática do token (logout automático)
- Feedback visual (snackbar/toast para sucesso/erro)
- Permissões e papéis de usuário (admin, comum)
- Recuperação de senha
- Exibir nome do usuário logado
- Tela de perfil do usuário (editar dados, trocar senha)
- Lembrar-me (persistência do login)
- Tema escuro/claro
- Refresh Token (renovação automática do JWT)
- Documentação da API (Swagger/OpenAPI)
- Tela de boas-vindas/resumo financeiro
- Validação de formulário avançada (mensagens específicas por campo)
- Testes automatizados para autenticação

## [1.2.0] - 2025-07-01

### Adicionado

- Redefinição de senha completa (persistência, validação, expiração)
- Migration para tabela de tokens

### Corrigido

- Erro de transação ao remover token

## [1.1.0] - 2025-06-30

### Adicionado

- Recuperação de senha (envio de e-mail)
- Integração com Mailhog

## [1.0.0] - 2025-06-28

### Adicionado

- Cadastro, login, JWT, guard, logout, feedback visual
- Roadmap e changelog iniciais
