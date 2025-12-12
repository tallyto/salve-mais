# Changelog - Salve Mais (Backend)


## [Unreleased]

## [1.17.0] - 2025-12-11

### Adicionado

- **Melhorias em Compras Parceladas**:
  - Endpoint `GET /api/compras-parceladas` agora aceita parâmetros de filtro:
    - `cartaoId` - Filtra por cartão específico
    - `categoriaId` - Filtra por categoria específica
    - `apenasPendentes` - Filtra apenas compras com parcelas pendentes
  - Ordenação automática por data mais recente (dataCompra desc)
  - Queries otimizadas no repository:
    - `findAllByOrderByDataCompraDesc()` - Lista ordenada por data
    - `findComprasComParcelasPendentes()` - Busca compras com parcelas não pagas
    - `findComprasComParcelasPendentesPorCartao()` - Filtro combinado cartão + pendentes
    - `findComprasComParcelasPendentesPorCategoria()` - Filtro combinado categoria + pendentes
  - Service `listarComprasComFiltros()` aplica filtros no banco de dados para melhor performance

### Melhorado

- **Performance**:
  - Filtros de compras parceladas processados no banco de dados
  - Redução de tráfego de rede com paginação eficiente
  - Queries otimizadas com JPA

## [1.16.0] - 2025-12-07

### Adicionado

- **Módulo de Planejamento Financeiro**:
  - Entidade `Meta` para gerenciamento de metas financeiras:
    - Campos: nome, descrição, valorAlvo, valorAtual, dataAlvo, categoria
    - Ícone e cor personalizáveis
    - Cálculo automático de percentual concluído e dias restantes
    - Status: EM_ANDAMENTO, CONCLUIDA, PAUSADA, CANCELADA
  
  - Entidade `PlanoCompra` para planejamento de compras:
    - Tipos de compra: A_VISTA, PARCELADO_SEM_JUROS, PARCELADO_COM_JUROS, FINANCIAMENTO
    - Campo `valorEconomizado` para controle de progresso
    - Cálculo automático de parcelas usando fórmula Price
    - Cálculo de valor final, juros total e percentual economizado
    - Sistema de prioridades e status
  
  - Entidade `PlanoAposentadoria` para planejamento de aposentadoria:
    - Dados pessoais: idade atual, idade de aposentadoria, expectativa de vida
    - Dados financeiros: patrimônio atual, contribuição mensal, renda desejada
    - Parâmetros: taxa de retorno anual, inflação estimada
    - Cálculos de projeções e viabilidade
  
  - Controllers REST completos:
    - `MetaController` - CRUD + atualização de progresso
    - `PlanoCompraController` - CRUD com filtros por status
    - `PlanoAposentadoriaController` - CRUD (singleton por usuário)
  
  - Services com lógica de negócio:
    - `MetaService` - Gerenciamento e cálculos de metas
    - `PlanoCompraService` - Cálculos financeiros de compras
    - `PlanoAposentadoriaService` - Gestão de plano de aposentadoria
  
  - Repositories com queries otimizadas:
    - `MetaRepository` - Ordenação por data alvo e status
    - `PlanoCompraRepository` - Ordenação por prioridade e data
    - `PlanoAposentadoriaRepository` - Busca por usuário

- **Migrations de Banco de Dados**:
  - V25: Criação de tabelas `metas`, `planos_compra`, `plano_aposentadoria`
  - V26: Adição de coluna `valor_economizado` em `planos_compra`

- **DTOs com Validações**:
  - `MetaDTO` - Validações de campos obrigatórios e limites
  - `MetaAtualizarProgressoDTO` - Para atualizações incrementais
  - `PlanoCompraDTO` - Validações de valores e percentuais
  - `PlanoAposentadoriaDTO` - Validações de idades e valores

### Removido

- **Multi-tenancy Simplificado**:
  - Removida entidade `FundoEmergencia` (funcionalidade descontinuada)
  - Removido campo `tenantId` das entidades de planejamento financeiro
  - Simplificação da arquitetura para melhor manutenção

### Melhorado

- **Cálculos Financeiros**:
  - Método `calcularParcela()` em PlanoCompra usando fórmula Price
  - Método `calcularPercentualEconomizado()` para acompanhamento de progresso
  - Método `calcularValorFinal()` considerando entrada e parcelas
  - Método `calcularJurosTotal()` para transparência financeira

### Corrigido

- Alinhamento de nomenclatura de campos entre frontend e backend
- Validações de dados em todos os DTOs de planejamento

## [1.15.0] - 2025-12-07

### Adicionado

- **Funcionalidade de Compras em Débito**:
  - Nova entidade `CompraDebito` para gerenciar compras que debitam imediatamente da conta
  - Controller REST `CompraDebitoController` com endpoints completos:
    - `POST /api/compras/debito` - Criar compra e debitar automaticamente
    - `GET /api/compras/debito` - Listar compras com paginação e filtros por mês/ano
    - `GET /api/compras/debito/{id}` - Buscar compra por ID
    - `PUT /api/compras/debito/{id}` - Atualizar compra (restrições aplicadas)
    - `DELETE /api/compras/debito/{id}` - Excluir compra
    - `GET /api/compras/debito/categoria/{categoriaId}` - Listar por categoria
    - `GET /api/compras/debito/total` - Calcular total por período
  - Service `CompraDebitoService` com lógica de negócio:
    - Validação de saldo antes de debitar
    - Débito automático da conta vinculada
    - Criação automática de transação ao registrar compra
    - Restrição de edição (não permite alterar valor, conta ou data após criação)
  - Repository `CompraDebitoRepository` com queries customizadas:
    - Busca por categoria
    - Busca por período de datas
    - Busca por mês e ano com paginação
    - Cálculo de total por período
  - DTO `CompraDebitoDTO` com validações Bean Validation
  - Migration `V24__create_table_compra_debito.sql` para PostgreSQL:
    - Tabela `compra_debito` com chaves estrangeiras para categoria e conta
    - Coluna `compra_debito_id` em `anexo` para suporte a comprovantes

### Melhorado

- **Dashboard e Relatórios integrados com Compras em Débito**:
  - `DashboardService` atualizado para incluir compras em débito em todos os cálculos:
    - `getSummary()` - Inclui compras débito nas despesas do mês atual e anterior
    - `getExpensesByCategory()` - Agrupa compras débito por categoria no gráfico de pizza
    - `getBudgetRule()` - Inclui compras débito na análise da regra 50/30/20
    - `getMonthlyExpenseTrend()` - Adiciona compras débito na tendência mensal
    - `getMonthlyExpenseTrendByYear()` - Inclui compras débito por mês do ano
    - `getVariationData()` - Compara compras débito entre mês atual e anterior
  - Gráficos e relatórios refletem automaticamente as compras em débito
- Entity `Anexo` atualizada com relacionamento para `CompraDebito`
- Suporte a anexos/comprovantes para compras em débito
- Nomenclatura consistente usando snake_case no banco de dados
- Validação de saldo insuficiente antes de realizar compras

## [1.14.0] - 2025-12-07

### Adicionado

- **Sistema de Notificações por Email**:
  - Novo controller `NotificacaoEmailController` com endpoints REST:
    - `POST /api/notificacoes-email` - Habilitar/atualizar configuração
    - `GET /api/notificacoes-email` - Obter configuração do tenant
    - `DELETE /api/notificacoes-email` - Desabilitar notificações
    - `POST /api/notificacoes-email/testar` - Enviar email de teste
  - Service `NotificacaoEmailSchedulerService` com scheduler automático:
    - Execução a cada hora (cron: `0 0 * * * *`)
    - Timezone configurado para America/Sao_Paulo
    - Tolerância de 30 minutos para envio
    - Suporte a emails de teste com notificações de exemplo
  - Template HTML profissional para emails (`notificacao-diaria.html`):
    - Design moderno com gradientes e badges
    - Informações detalhadas por prioridade (Crítica, Alta, Média)
    - Alerta destacado para notificações críticas
    - Data atual e informação temporal (dias de atraso/vencimento)
    - Footer com dicas e instruções
  - Entity `NotificacaoEmail` para configuração por tenant
  - Migration `V23__create_table_notificacao_email.sql`
  - DTOs: `NotificacaoEmailRequestDTO` e `NotificacaoEmailResponseDTO`

### Melhorado

- Configuração de timezone global do Spring:
  - `spring.jpa.properties.hibernate.jdbc.time_zone=America/Sao_Paulo`
  - `spring.jackson.time-zone=America/Sao_Paulo`
  - `spring.jackson.locale=pt-BR`
- Sistema de multi-tenancy com busca de usuários otimizada
- Tratamento de dependência circular com `@Lazy`
- Logs informativos para debug de envio de emails

### Documentação

- Criado `AI_CODE_REVIEW_GUIDE.md` com guia completo para IA e code review
- Removidos arquivos MD explicativos obsoletos

## [1.11.0] - 2025-11-24

### Adicionado

- **Funcionalidade de exportação Excel do Dashboard**:
  - Novo endpoint `GET /api/dashboard/export/excel` para exportar dados em Excel
  - ExportService completo para geração de arquivos Excel usando Apache POI
  - Arquivo Excel com 6 abas organizadas:
    - Resumo Financeiro (saldo, receitas, despesas, indicadores de saúde)
    - Despesas por Categoria (valores e percentuais)
    - Contas e Saldos (titular, tipo, saldo, descrição)
    - Transações Recentes (data, descrição, valor, categoria, cartão)
    - Compras Parceladas em Aberto (descrição, valor, parcelas, vencimento, status)
    - Tendência Mensal (receitas, despesas e resultado por mês)
  - Formatação profissional com estilos, cores e bordas
  - Suporte a parâmetros de filtro por mês e ano
  - Formatação brasileira para valores monetários (R$ 1.234,56)
  - Auto-ajuste de largura de colunas
  - Dependências Apache POI 5.2.5 para manipulação Excel

## [1.10.0] - 2025-11-20

### Adicionado

- Melhorias no sistema de dashboard e relatórios

## [1.9.0] - 2025-10-05

### Adicionado

- Funcionalidade de edição de compras parceladas:
  - Novo endpoint PUT `/api/compras-parceladas/{id}` para atualizar compras
  - Método `atualizarCompraParcelada()` no CompraParceladaService
  - Método `atualizarValoresParcelas()` para recalcular valores quando necessário
  - Lógica inteligente que detecta mudanças na estrutura de parcelas
  - Regeneração automática de parcelas quando há alteração no número de parcelas
  - Atualização de valores mantendo parcelas existentes quando apenas o valor muda
  - Validações completas para garantir integridade dos dados
  - Tratamento específico de erros para operações de atualização

### Melhorado

- Tratamento de erros no controller com respostas HTTP apropriadas (400, 404, 500)
- Documentação JavaDoc dos novos métodos

## [1.8.0] - 2025-09-15

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

### Removido

- Funcionalidade de estorno de transações:
  - Removidos campos `transacao_original_id`, `estornada` e `transacao_estorno_id` da tabela de transações
  - Removidas constraints e índices relacionados aos campos de estorno
  - Removido o tipo `ESTORNO` do enum `TipoTransacao`
  - Atualizado o DTO de transação para não incluir campos de estorno
  - Migration V18 para remover os campos da tabela

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
