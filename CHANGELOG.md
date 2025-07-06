# Changelog - Gestor Financeiro (Backend)

## [Unreleased]

- Adicionado endpoint PUT /api/categorias/{id} para atualização de categorias
- Adicionado endpoint DELETE /api/categorias/{id} para exclusão de categorias
- Adicionado endpoint GET /api/categorias/id/{id} para busca de categoria por ID
- Atualizado DTO de categoria para incluir ID
- Implementados métodos no CategoriaService para atualização e exclusão
- Adicionado tratamento de erro para categorias não encontradas
- Adicionado endpoint PUT /api/proventos/{id} para atualização de proventos
- Implementado método atualizarProvento no ProventoService
- Adicionado endpoint DELETE /api/cartao-credito/{id} para exclusão de cartões
- Adicionado endpoint GET /api/cartao-credito/{id} para busca individual de cartões
- Adicionado endpoint PUT /api/conta/{id} para atualizar dados da conta (saldo e titular)
- Implementado método update no ContaService para atualização de contas
- Adicionado endpoint GET /api/contas/fixas/{id} para busca individual de contas fixas
- Adicionado endpoint PUT /api/contas/fixas/{id} para atualização de contas fixas
- Adicionado endpoint DELETE /api/contas/fixas/{id} para exclusão de contas fixas
- Adicionado endpoint GET /api/compras/{id} para busca individual de compras
- Adicionado endpoint PUT /api/compras/{id} para atualização de compras
- Adicionado endpoint DELETE /api/compras/{id} para exclusão de compras
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
- Implementada funcionalidade de edição de provento (endpoint PUT, service, integração com DTO)

## [1.4.0] - 2025-07-05

### Adicionado

- Suporte completo a multi-tenancy (multi locatários) com:
  - Entidade, DTO, Mapper e Repository para Tenant
  - Controller REST para Tenant
  - Filtro, contexto e configuração para troca dinâmica de schema
  - Serviço de migração Flyway por tenant
  - Migration SQL para tabela de tenants
- Correção das propriedades do Maven no pom.xml para build correto
- Ajuste do profile de teste para importar corretamente o application-test.properties

### Alterado

- Atualização do pom.xml para definir versões de plugins e dependências

## [1.3.0] - 2025-07-03

### Alterado

- Script de deploy atualizado para utilizar Docker Compose com o arquivo `docker-compose.prod.yml` para ambiente de produção.

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

## [Unreleased]

- Correção de mapeamento JPA/Hibernate:
  - Ajustado o nome da coluna de relacionamento CartaoCredito em Compra para `cartao_credito_id`.
  - Garantido que todas as entidades usam nomes de tabela e coluna compatíveis com o banco (snake_case).
- Correção de erro ao criar cartão de crédito (tabela/cartão/tabela de relacionamento).
- Ajuste de multi-tenancy e integração front-end/back-end para envio do tenant em todas as requisições.
