# Instructions for AI Agents

Este documento fornece instruções para agentes de IA que trabalham neste projeto.

## Informações do Projeto

**Nome:** Gestor Financeiro  
**Linguagem:** Java 17  
**Framework:** Spring Boot 3.2.4  
**Build Tool:** Maven  
**Banco de Dados:** PostgreSQL (via Flyway migrations)  
**Arquitetura:** Multi-tenant com JWT

## Estrutura do Projeto

```
src/main/java/com/tallyto/gestorfinanceiro/
├── api/                    # Controllers e DTOs
│   ├── controllers/        # REST Controllers
│   └── dto/               # Data Transfer Objects
├── core/                  # Lógica de negócio
│   ├── application/       # Services
│   ├── domain/           # Entities e Value Objects
│   └── infrastructure/   # Repositories e configurações
├── mappers/              # MapStruct mappers
└── testsupport/          # Utilitários de teste

src/main/resources/
├── db/migration/         # Flyway migrations (versionadas)
└── templates/           # Templates de email
```

## Regras Importantes

### 1. Versionamento

**CRÍTICO:** Antes de qualquer deploy ou commit de nova funcionalidade:

- **SEMPRE** atualize a versão em `pom.xml` (linha 13)
- Siga versionamento semântico (MAJOR.MINOR.PATCH):
  - **MAJOR** (x.0.0): Mudanças incompatíveis/breaking changes
  - **MINOR** (1.x.0): Novas funcionalidades compatíveis
  - **PATCH** (1.9.x): Correções de bugs

Exemplo:
```xml
<artifactId>gestor-financeiro</artifactId>
<version>1.10.0</version>  <!-- Atualizar aqui -->
```

### 2. Migrations de Banco de Dados

- Migrations ficam em `src/main/resources/db/migration/`
- Nomenclatura: `V{numero}__{descricao}.sql` (ex: `V25__add_new_column.sql`)
- **NUNCA** modifique migrations já aplicadas em produção
- **SEMPRE** crie uma nova migration para alterações

### 3. Testes

- Testes unitários devem estar em `src/test/java/`
- Use `@ControllerSliceTest` para testes de controllers
- Execute testes antes de commits: `./mvnw test`
- Build sem testes: `./mvnw clean package -DskipTests`

### 4. Multi-tenant

- **SEMPRE** considere o contexto de tenant nas queries
- Use `@TenantFilter` nas entities quando necessário
- JWT contém `tenantId` - valide em operações sensíveis
- Cada tenant tem configurações próprias (SMTP, regional, etc.)

### 5. Padrões de Código

#### DTOs
- Usar records para DTOs imutáveis
- Adicionar anotações `@Schema` do Swagger para documentação
- Validações com Bean Validation (`@NotNull`, `@Valid`, etc.)

#### Services
- Um service por entidade/agregado
- Injeção via construtor (não `@Autowired` em fields)
- Lógica de negócio nos services, não nos controllers

#### Controllers
- REST endpoints seguem padrão RESTful
- Usar `ResponseEntity<T>` para respostas
- DTOs para entrada/saída, nunca entities diretamente
- Documentar com `@Operation` do Swagger

#### Mappers
- Usar MapStruct para conversões Entity ↔ DTO
- Definir em `mappers/`
- Usar `@Mapper(componentModel = "spring")`

### 6. Deploy

**Sistema de deploy versionado com rollback disponível**

#### Deploy Normal:
```bash
./deploy.sh
```

O script automaticamente:
- Extrai versão do `pom.xml`
- Cria backup da versão atual
- Valida build antes de derrubar containers
- **Bloqueia deploy de versão duplicada**

#### Rollback:
```bash
# Listar backups
./rollback.sh

# Fazer rollback
./rollback.sh backup_1.9.1_20251120_205500
```

Ver `DEPLOY.md` para detalhes completos.

### 7. Commits

#### Formato de Mensagens (Conventional Commits):

```
<type>(<scope>): <subject>

<body>
```

**Types:**
- `feat`: Nova funcionalidade
- `fix`: Correção de bug
- `refactor`: Refatoração sem mudança funcional
- `test`: Adição/correção de testes
- `docs`: Documentação
- `chore`: Tarefas de manutenção
- `perf`: Melhorias de performance

**Exemplo:**
```
feat(tenant): adiciona configuração de SMTP personalizado

- Permite cada tenant configurar seu próprio servidor SMTP
- Adiciona validação de credenciais
- Atualiza documentação da API
```

### 8. Features do Sistema

#### Principais Funcionalidades:
- Gestão de contas financeiras
- Controle de despesas fixas e recorrentes
- Compras parceladas
- Faturas de cartão
- Orçamento e metas
- Relatórios e dashboards
- Sistema multi-tenant
- Notificações via email

#### Customização por Tenant:
- Marca (logo, favicon, displayName)
- SMTP (email personalizado)
- Regional (timezone, locale, moeda, formato de data)
- Planos (FREE, BASIC, PREMIUM, ENTERPRISE)
- Features habilitadas por plano

### 9. Segurança

- Autenticação via JWT
- Tokens contêm: `userId`, `tenantId`, `email`, `authorities`
- Validar tenant em operações críticas
- Não expor senhas em DTOs (usar `UsuarioResponseDTO`)
- CORS configurado para produção

### 10. Variáveis de Ambiente

Principais variáveis em `.env` (não versionado):

```properties
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=gestor_financeiro
DB_USER=postgres
DB_PASSWORD=senha

# JWT
JWT_SECRET=seu-secret-aqui
JWT_EXPIRATION=86400000

# Email (SMTP padrão)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=email@example.com
SMTP_PASSWORD=senha
```

### 11. Comandos Úteis

```bash
# Build
./mvnw clean package

# Run local
./mvnw spring-boot:run

# Testes
./mvnw test

# Deploy
./deploy.sh

# Rollback
./rollback.sh

# Verificar versão
awk '/<artifactId>gestor-financeiro<\/artifactId>/{getline; print}' pom.xml | grep -oP '(?<=<version>)[^<]+'
```

## Workflow Recomendado para Novas Features

1. **Criar branch** (opcional, mas recomendado)
2. **Atualizar versão** no `pom.xml`
3. **Implementar funcionalidade**
   - Criar/atualizar entities
   - Criar migrations se necessário
   - Criar DTOs
   - Criar/atualizar mappers
   - Criar/atualizar services
   - Criar/atualizar controllers
4. **Adicionar testes**
5. **Validar build**: `./mvnw clean package`
6. **Commit** com mensagem convencional
7. **Push** para repositório
8. **Deploy** automático via GitHub Actions ou manual com `./deploy.sh`

## Troubleshooting

### Build Falha
- Verificar versões Java (deve ser 17)
- Limpar cache: `./mvnw clean`
- Verificar dependências: `./mvnw dependency:tree`

### Testes Falhando
- Verificar mocks em `TenantControllerTest` e similares
- Validar número de parâmetros em DTOs tipo record
- Verificar configuração de test profile

### Deploy Falha
- Verificar se versão foi atualizada
- Verificar se não existe imagem com mesma versão
- Verificar logs do Docker: `docker logs gestor-financeiro-app`

## Documentação Adicional

- **API Docs:** `/swagger-ui.html` (em desenvolvimento)
- **Customização Tenant:** `TENANT_CUSTOMIZATION.md`
- **JWT Implementation:** `TENANT_JWT_IMPLEMENTATION.md`
- **Deploy:** `DEPLOY.md`
- **Changelog:** `CHANGELOG.md`

## Contato

Para dúvidas sobre a arquitetura ou decisões de design, consulte a documentação ou os commits históricos com suas justificativas.
