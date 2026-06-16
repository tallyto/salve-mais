# Arquitetura — Salve Mais (Backend)

> Guia de orientação para quem (humano ou agente) for continuar o desenvolvimento.
> Para o estado atual do roadmap de produtização e o que falta fazer, ver `NEXT_SESSION.md`.
> Para o frontend, ver `ARQUITETURA.md` no repo `salve-mais-ui`.

## Visão geral

Salve Mais é um plataforma financeira pessoal **multi-tenant** (cada empresa/usuário-raiz
é um "tenant" com seu próprio schema de banco). Stack: **Spring Boot 3 + Java 21 +
PostgreSQL + Flyway + Hibernate multi-tenant (schema-per-tenant) + JWT + Stripe**.

Está em processo de produtização para SaaS — ver seção "Produtização SaaS" abaixo.

## Compilar e rodar

```bash
# IMPORTANTE: sempre compilar/rodar com Java 21 — Java 26 quebra o plugin de compilação
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./mvnw compile -q
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./mvnw spring-boot:run

docker-compose up   # sobe Postgres + Mailhog (+ Adminer) para o ambiente local
```

Perfil `dev` (`application-dev.properties`) aponta para o Postgres local
(`gestor`/`gestor@admin`, banco `gestor`, porta 5432) e usa Mailhog para e-mails.
Existe um `DevDataSeeder` (`@Profile("dev")`) que provisiona um tenant + usuário de
teste (`teste@salvemais.com.br` / `teste123`, domínio `dev.salvemais.local`) na
subida da aplicação, evitando o fluxo completo de cadastro/confirmação por e-mail.

## Estrutura de pacotes

```
br.com.salvemais
├── api/
│   ├── controllers/     — REST controllers (camada de entrada HTTP)
│   ├── dto/             — Data Transfer Objects expostos pela API
│   └── mappers/         — conversão entidade <-> DTO (MapStruct)
├── config/
│   ├── security/        — SecurityConfig, AuditorAwareImpl
│   ├── stripe/          — StripeProperties, StripeGateway
│   ├── database/        — config de datasource/migrations multi-tenant
│   ├── JwtAuthenticationFilter, TenantFilter, SubscriptionGuardFilter
│   ├── CurrentTenantIdentifierResolverImpl, MultiTenantConnectionProviderImpl
│   └── HibernateConfig, SchedulingConfig
├── context/
│   └── TenantContext    — ThreadLocal (InheritableThreadLocal) com o tenant atual
├── core/
│   ├── application/services/   — regras de negócio (services)
│   ├── domain/
│   │   ├── entities/           — entidades JPA
│   │   ├── enums/              — SubscriptionStatus, TipoConta, TipoTransacao...
│   │   └── exceptions/         — exceções de domínio + ApiExceptionHandler (RFC7807 Problem)
│   ├── database/               — FlywayMigrationService, DevDataSeeder
│   └── infra/repositories/     — Spring Data JPA repositories
├── mappers/             — mappers gerais (legado; novos vão em api/mappers)
└── util/
```

A separação `api` (entrada) / `core.application` (regras de negócio) /
`core.domain` (modelo) / `core.infra` (persistência) segue um espírito de
arquitetura em camadas/hexagonal — ao adicionar uma feature nova, normalmente
você cria: entidade (se necessário) → repository → service → DTO/mapper → controller.

## Multi-tenancy (schema-per-tenant)

Esse é o ponto mais delicado da base de código — qualquer feature nova deve
respeitar esse fluxo:

1. `TenantContext` (`context/TenantContext.java`) é um `ThreadLocal` que guarda
   o domínio do tenant atual da requisição (e também grava no MDC para logs).
2. `JwtAuthenticationFilter` extrai o `tenantDomain` das claims do JWT, chama
   `TenantContext.setCurrentTenant(tenantDomain)` e roda
   `FlywayMigrationService.migrateTenantSchema(...)` (garante que o schema do
   tenant está com as migrations em dia) antes de autenticar a requisição.
3. `CurrentTenantIdentifierResolverImpl` (Hibernate) lê `TenantContext` para
   decidir em qual schema do Postgres a sessão Hibernate vai operar —
   `MultiTenantConnectionProviderImpl` troca o schema da conexão JDBC (`SET search_path`).
4. Sempre limpar o contexto ao final (`TenantContext.clear()`) — olhe os blocos
   `try/finally` existentes como referência (ex.: `DevDataSeeder`, `JwtAuthenticationFilter`).
5. Há também um schema `public`/`DEFAULT_TENANT` para entidades globais —
   `Tenant`, `UsuarioGlobal`, `Plano` vivem em `public`; o restante (Conta,
   Transacao, Categoria, Cartao, Fatura, etc.) vive no schema do tenant.

`UsuarioGlobal` (schema `public`, usado para login/autenticação) é distinto de
`Usuario` (schema do tenant, usuário de fato dentro da aplicação) — ao mexer em
autenticação/cadastro, cuidado para não confundir as duas entidades.

## Segurança / Autenticação

- JWT (`JwtService`) com claims de e-mail + domínio do tenant.
- `SecurityConfig` registra a cadeia de filtros: `JwtAuthenticationFilter` →
  (autenticação) → `SubscriptionGuardFilter` (bloqueio por status de assinatura).
- `ApiExceptionHandler` centraliza o tratamento de exceções no formato
  RFC 7807 (`Problem` + `ProblemType`), incluindo `PaymentRequiredException` (402).

## Produtização SaaS (contexto importante)

O projeto está sendo transformado em SaaS self-service com cobrança recorrente
via Stripe — replicando o padrão já usado no projeto irmão **sgtur**
(`/home/tallyto/projetos/sgtur-back`, `sgtur-front`, `sgtur-page`, `sgtur-admin`).
Sempre que houver dúvida de "como fazer X de forma produtizada", vale consultar
como o sgtur resolveu.

Conceitos centrais:
- `SubscriptionStatus`: `TRIAL → ATIVO → INADIMPLENTE → CANCELADO`
- `Plano` (entidade): catálogo de planos com limites (`maxUsuarios`,
  `maxTransacoesMes`, `maxStorageGb`, `precoMensal`, `stripePriceId`)
- `PlanLimitService`: valida limites do plano antes de operações que consomem cota
- `SubscriptionGuardFilter`: bloqueia tenants `INADIMPLENTE`/`CANCELADO` com 402
  antes de chegar nos controllers (caminhos públicos: `/api/auth/**`,
  `/api/tenants/*` (cadastro/verificar/confirmar/verificar-dominio),
  `/api/usuarios`, `/api/webhook/stripe`)
- `BillingService` + `StripeGateway`: checkout, cancelamento, processamento de
  webhooks (`checkout.session.completed`, `invoice.payment_succeeded`,
  `invoice.payment_failed`, `customer.subscription.deleted`)
- `SubscriptionScheduler`: cron 02:00 expira trials vencidos e envia e-mail

Roteiro completo de fases (1-6) e o que falta: ver tabela de roadmap no `README.md`
e o detalhamento em `NEXT_SESSION.md`.

### Variáveis de ambiente Stripe (produção)
```
STRIPE_SECRET_KEY=sk_live_...
STRIPE_WEBHOOK_SECRET=whsec_...
STRIPE_SUCCESS_URL=https://www.salvemais.com.br/#/billing/sucesso
STRIPE_CANCEL_URL=https://www.salvemais.com.br/#/billing/cancelado
```
Os `stripePriceId` de cada plano são cadastrados manualmente na tabela
`public.planos` (não há sincronização automática — ver `NEXT_SESSION.md`).

## Banco de dados / Migrations

- Flyway, migrations em `src/main/resources/db/migration` (atualmente até `V32`).
- Cada tenant tem seu próprio schema — `FlywayMigrationService` aplica as
  migrations no schema do tenant sob demanda (lazy, na autenticação).
- Entidades globais (Tenant, UsuarioGlobal, Plano) ficam no schema `public`.
- `spring.jpa.hibernate.ddl-auto=validate` em produção — mudanças de schema
  **sempre** via migration, nunca via `ddl-auto=update`.

## Convenções e workflow observados neste projeto

- **Sempre Java 21** para compilar (`JAVA_HOME=/usr/lib/jvm/java-21-openjdk`).
- Ao terminar uma fase/feature relevante: commit + entrada no `CHANGELOG.md` +
  bump de versão no `pom.xml` + atualizar a tabela de roadmap no `README.md`.
- Versão atual do backend: ver `pom.xml` (na sessão de 2026-06 estava em 1.21.x).
- Use `NEXT_SESSION.md` como ponto de partida ao retomar — ele documenta em qual
  fase o projeto está e qual é a próxima, evitando retrabalho.
