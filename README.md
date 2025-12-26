# 💰 Salve Mais - Backend

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=flat&logo=spring-boot&logoColor=white)
![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat&logo=openjdk&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=flat&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white)

**API REST para sistema de gestão financeira multi-tenant com Spring Boot**

Sistema robusto de gestão financeira pessoal com arquitetura multi-tenant, autenticação JWT, isolamento de dados por tenant e recursos avançados de análise financeira.

## 🚀 Principais Funcionalidades

- 🏢 **Multi-tenant**: Isolamento completo de dados por tenant/domínio
- 🔐 **Autenticação JWT**: Tokens seguros com claims de tenant
- 💳 **Gestão Financeira**: Receitas, despesas, cartões, faturas e parcelamentos
- 📊 **Analytics**: Dashboard com métricas e gráficos financeiros
- 📧 **Sistema de Email**: Notificações e recuperação de senha
- 🔄 **Migração de Schema**: Flyway para versionamento de banco
- 📱 **API RESTful**: Endpoints documentados com Swagger/OpenAPI
- ⚡ **Performance**: Connection pooling (HikariCP) e otimizações JPA

## Pré-requisitos

- Docker
- Docker Compose
- PostgreSQL (em produção, o banco deve ser externo)

## Como rodar em desenvolvimento

1. Suba o banco de dados e dependências:

   ```sh
   docker-compose up
   ```

2. Rode a aplicação localmente (via IDE ou Maven):

   ```sh
   ./mvnw spring-boot:run
   ```

## Como rodar em produção

1. Configure as variáveis de ambiente do banco de dados:
   - `SPRING_DATASOURCE_URL`
   - `SPRING_DATASOURCE_USERNAME`
   - `SPRING_DATASOURCE_PASSWORD`

2. Faça o build e execute a aplicação com Docker Compose:

   ```sh
   docker-compose -f docker-compose.prod.yml up --build
   ```

A aplicação estará disponível em <http://localhost:8080>

## Configurações

- O perfil de produção utiliza o arquivo `application-prod.properties`.
- O schema do banco é validado na inicialização (`spring.jpa.hibernate.ddl-auto=validate`).

## Estrutura do Projeto

- `src/main/java` — Código-fonte Java
- `src/main/resources` — Configurações e migrations
- `Dockerfile` — Build da aplicação em container
- `docker-compose.yml` — Ambiente de desenvolvimento
- `docker-compose.prod.yml` — Ambiente de produção

## Licença

MIT
