# Changelog - Salve Mais (Backend)


## [Unreleased]

### Adicionado

- Configuração de CI/CD para deploy automático na VPS:
  - Workflow do GitHub Actions para deploy automatizado
  - Documentação detalhada do processo de configuração
  - Script de deploy para ambiente de produção

### Melhorado

- Tratamento amigável para erros de violação de chave estrangeira:
  - Nova exceção `EntityInUseException` para tratar tentativas de excluir registros vinculados
  - Handler específico para `DataIntegrityViolationException` com mensagens amigáveis
  - Extração automática do nome da tabela e constraint da mensagem de erro
  - Mapeamento de nomes técnicos para nomes amigáveis nas mensagens de erro
  - Implementação em serviços de `Categoria` e `Conta`

---

## [1.9.0] - 2025-08-23

### Adicionado

- Sistema completo de Reserva de Emergência:
  - Nova entidade `ReservaEmergencia` para armazenar dados da reserva
  - Migration V13 para criação da tabela `reserva_emergencia`
  - Endpoints para criação, atualização e consulta de reservas
  - Sistema de cálculo automático de objetivo baseado nas despesas mensais
  - Funcionalidade para contribuições para a reserva de emergência
  - Integração com contas para gerenciamento de saldo
- Sistema de tipagem de contas:
  - Migration V14 para adicionar campo `tipo` à tabela `conta`
  - Enum `TipoConta` com tipos CORRENTE, POUPANCA, INVESTIMENTO e RESERVA_EMERGENCIA
  - Adição de campos `taxa_rendimento` e `descricao` para contas
  - Serviço `RendimentoService` para cálculo de rendimentos em contas específicas

### Melhorado

- Integração entre módulos para facilitar transações entre contas
- Informações detalhadas em contas para melhor gestão financeira

---

## [1.8.0] - 2025-08-09

### Adicionado

- Sistema completo de anexos de comprovantes para contas fixas:
  - Entidade `Anexo` para armazenamento de metadados de arquivos
  - Migration V12 para criação da tabela `anexo` no banco de dados
  - Integração com AWS S3 para armazenamento de arquivos
  - Implementação específica para ambiente de desenvolvimento usando banco de dados
  - Endpoints para upload, download e gerenciamento de anexos
  - Interface `AnexoServiceInterface` para desacoplamento da implementação
  - Estratégia de organização baseada em tenant para facilitar administração

### Melhorado

- Otimização das relações JPA para evitar loops infinitos:
  - Adicionado `@JsonManagedReference` e `@JsonBackReference` nas relações bidirecionais
  - Ajuste das estratégias de carregamento (fetch) para melhorar desempenho
  - Configuração de carregamento LAZY para coleções e EAGER para entidades pai

### Corrigido

- Correção do problema de carregamento infinito na serialização JSON de entidades relacionadas
- Ajuste da organização de arquivos no S3 para usar o tenant como identificador principal

---
## [1.7.0] - 2025-07-22

### Adicionado

- Sistema completo de notificações de contas e faturas atrasadas/proximas do vencimento:
  - NotificacaoDTO, enums de prioridade e tipo
  - NotificacaoService com métodos para contas/faturas atrasadas e próximas do vencimento
  - NotificacaoController com endpoints REST para notificações e resumo
- Integração de datas `criadoEm` e `ultimoAcesso` na entidade Usuario
- Endpoints e lógica para atualização automática de ultimoAcesso no login

### Corrigido

- Ajustes de mapeamento JPA para campos snake_case/camelCase
- Correções de integração entre frontend e backend para dados reais do usuário

---
## [Unreleased]

### Adicionado

- Sistema aprimorado de pagamento de faturas com vinculação à conta bancária:
  - Novo endpoint PATCH `/api/faturas/{faturaId}/pagar/{contaId}` para pagamento com conta específica
  - Novo endpoint GET `/api/faturas/pendentes` para listar faturas não pagas
  - Novo endpoint GET `/api/faturas/conta/{contaId}` para listar faturas por conta de pagamento
  - Adicionado campo `conta_pagamento_id` na tabela `fatura` (Migration V6)
  - Método `marcarComoPaga(Long faturaId, Long contaId)` no `FaturaService` com validação de saldo
  - Métodos auxiliares no `FaturaService`: `listarNaoPagas()`, `listarPorConta()`, `calcularTotalFaturasPendentes()`
  - Métodos no `ContaService`: `findOrFail()`, `debitar()`, `creditar()` para gerenciamento de saldos
  - Novos métodos no `FaturaRepository`: `findByContaPagamentoId()`, `findByPagoFalse()`, `findByCartaoCreditoIdAndPagoFalse()`
- Melhoria no sistema de armazenamento de anexos de comprovantes:
  - Estrutura no S3 alterada para usar o tenant como pasta principal ao invés do ID da conta
  - Facilitação da organização e acesso dos anexos por tenant

### Melhorado

- Sistema transacional de pagamento de faturas:
  - Validação de saldo suficiente na conta antes do pagamento
  - Débito automático do valor da fatura na conta selecionada
  - Registro da conta utilizada e data de pagamento
  - Prevenção de exclusão de faturas já pagas
- DTO `FaturaResponseDTO` atualizado com novos campos:
  - `dataPagamento`: Data em que a fatura foi paga
  - `contaPagamentoId`: ID da conta utilizada no pagamento
  - `nomeContaPagamento`: Nome do titular da conta de pagamento

### Corrigido

- Método legado `marcarComoPaga(Long faturaId)` mantido para compatibilidade, mas marcado como deprecated
- Validações aprimoradas para evitar pagamentos duplicados

## [1.6.0] - 2025-07-17

### Adicionado

- Sistema completo de gerenciamento de faturas manuais:
  - Endpoint POST `/api/faturas/manual` para criação de faturas sem compras
  - Endpoint PATCH `/api/faturas/{id}/pagar` para marcar fatura como paga
  - Endpoint DELETE `/api/faturas/{id}` para exclusão de faturas
  - Endpoint GET `/api/faturas/{id}` para busca individual
  - DTO `FaturaManualDTO` para criação manual de faturas
  - DTO `FaturaResponseDTO` para respostas padronizadas
  - Validações para criação e manipulação de faturas

### Melhorado

- Sistema de relatórios mensais completamente reestruturado:
  - Relatórios agora mostram gastos de cartões através de faturas (em vez de compras individuais)
  - Removida seção "Outras Despesas" para simplificar a visualização
  - Filtro de contas fixas corrigido para mostrar apenas contas que vencem no mês especificado
  - Adicionado método `findByDataVencimentoBetween` no `FaturaRepository`
  - Adicionado método `findByVencimentoBetween` no `ContaFixaRepository`
  - Melhorada precisão dos cálculos financeiros mensais

### Corrigido

- Corrigido problema onde contas fixas de meses anteriores apareciam em relatórios de meses futuros
- Resolvido erro de injeção de dependência `UnsatisfiedDependencyException` no `FaturaController`
- Corrigida consulta de relatório mensal para usar apenas dados do período especificado

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
