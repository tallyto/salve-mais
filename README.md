# Gestor Financeiro

Aplicação Spring Boot para gestão financeira, com suporte a Docker e PostgreSQL.

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
