# Changelog - Gestor Financeiro (Backend)

## [Unreleased]

## [1.5.0] - 2025-07-06

### Adicionado

- Sistema padronizado de tratamento de exceções da API:
  - `ApiExceptionHandler` para lidar com todas as exceções da aplicação
  - Classes de exceção personalizadas: `BadRequestException`, `ForbiddenException`, `ResourceNotFoundException`
  - Padronização de respostas de erro com formato Problem Details
  - Tratamento específico para violações de validação (campos inválidos)
- Implementação completa de endpoints para dashboard:
  - GET `/api/dashboard/summary` para resumo financeiro
  - GET `/api/dashboard/expenses-by-category` para gráfico de despesas por categoria
  - GET `/api/dashboard/monthly-trend` para tendência de gastos por mês
  - Criação do DashboardService com métodos para calcular dados agregados
  - Criação de DTOs para respostas (DashboardSummaryDTO, CategoryExpenseDTO, MonthlyExpenseDTO)
- Implementação de endpoints CRUD para entidades principais:
  - Categorias: PUT, DELETE, GET por ID
  - Proventos: PUT, DELETE com ajuste de saldo da conta
  - Cartões de crédito: DELETE, GET por ID
  - Contas: PUT para atualização de dados (saldo e titular)
  - Contas fixas: GET por ID, PUT, DELETE
  - Compras: GET por ID, PUT, DELETE
- Serviço de cadastro e confirmação de tenant:
  - Criação do TenantService com métodos para cadastro, verificação e confirmação
  - Endpoints públicos para gerenciamento de tenant (`/api/tenants/cadastro`, `/api/tenants/verificar`, `/api/tenants/confirmar`, `/api/tenants/verificar-dominio`)
  - Envio de e-mail de confirmação de tenant
  - Tabelas e migrations para suporte a ativação de tenant

### Melhorado

- Métodos de repositório aprimorados com `findByDataBetween` para ProventoRepository e CompraRepository
- Tratamento de erro para categorias não encontradas
- Implementação de métodos de serviço para todas as entidades
- Configuração CORS para suportar o header X-Private-Tenant
- Configuração de segurança para permitir rotas públicas de tenant e usuário
- TenantController refatorado para usar TenantService ao invés de acessar diretamente o repository

### Corrigido

- Correção de mapeamento JPA/Hibernate:
  - Ajustado o nome da coluna de relacionamento CartaoCredito em Compra para `cartao_credito_id`
  - Garantido que todas as entidades usam nomes de tabela e coluna compatíveis com o banco (snake_case)
- Correção de erro ao criar cartão de crédito (tabela/cartão/tabela de relacionamento)
- Ajuste de multi-tenancy e integração front-end/back-end para envio do tenant em todas as requisições
- Correção de bean validation para tenant (campos name e domain)

## [1.4.0] - 2025-07-05

### Adicionado

- Suporte completo a multi-tenancy (multi locatários) com:
  - Entidade, DTO, Mapper e Repository para Tenant
  - Controller REST para Tenant
  - Filtro, contexto e configuração para troca dinâmica de schema
  - Serviço de migração Flyway por tenant
  - Migration SQL para tabela de tenants

### Melhorado

- Correção das propriedades do Maven no pom.xml para build correto
- Ajuste do profile de teste para importar corretamente o application-test.properties
- Atualização do pom.xml para definir versões de plugins e dependências

## [1.3.0] - 2025-07-03

### Melhorado

- Script de deploy atualizado para utilizar Docker Compose com o arquivo `docker-compose.prod.yml` para ambiente de produção

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
