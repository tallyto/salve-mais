# Instruções CSM - Backend (Spring Boot)

Sistema de gestão financeira multi-tenant com Spring Boot 3.x, PostgreSQL e JWT.

## 🚀 Implementando Nova Feature

### 1. Preparação
```bash
# Criar branch feature  
git checkout master && git pull
git checkout -b feature/nome-da-feature
```

### 2. Desenvolvimento
- **Controller**: Endpoints REST com validação `@Valid`
- **Service**: Lógica de negócio com `@Transactional`
- **Repository**: Interface JPA com queries customizadas
- **Entity**: Mapeamento JPA com relacionamentos
- **DTO**: Request/Response com validações

### 3. Padrões Obrigatórios
- **Multi-tenant**: Header `X-Private-Tenant` via `TenantFilter`
- **JWT**: Tokens com tenant claims via `JwtService`
- **Validação**: Bean Validation com mensagens customizadas
- **Logs**: SLF4J com níveis apropriados (INFO, WARN, ERROR)
- **Exception**: Handlers globais com `@ControllerAdvice`

### 4. Finalização
```bash
# Atualizar versão no pom.xml
<version>1.X.Y</version>

# Atualizar CHANGELOG.md
## [1.X.Y] - Data
### Adicionado
- Descrição da feature

# Commit organizado
git add -A  
git commit -m "feat: descrição da feature"

# Push e PR
git push origin feature/nome-da-feature
```

## 📋 Checklist de Feature

### Backend Core
- [ ] Controller com endpoints documentados (`@Operation`)
- [ ] Service com lógica de negócio e validações
- [ ] Repository com queries otimizadas
- [ ] Entity com mapeamento JPA correto
- [ ] DTO com validações Bean Validation
- [ ] Exception handling personalizado
- [ ] Logs estruturados (sem console.out)
- [ ] Testes unitários para service/repository

### Multi-tenant & Segurança
- [ ] TenantContext utilizado corretamente
- [ ] Isolamento de dados garantido
- [ ] JWT com claims de tenant
- [ ] Endpoints protegidos com `@PreAuthorize` se necessário
- [ ] Validação de permissões por tenant

### Performance & Qualidade
- [ ] Queries JPA otimizadas (evitar N+1)
- [ ] Paginação em listagens grandes
- [ ] Connection pooling configurado
- [ ] Transações bem definidas
- [ ] Código limpo sem duplicação

## 🔧 Configurações Técnicas

### Multi-tenant Setup
```java
// TenantFilter extrai automaticamente
String tenant = TenantContext.getCurrentTenant();

// Service sempre usar contexto
@Service
public class MinhaService {
    public List<Entity> listar() {
        // Dados automaticamente filtrados por tenant
        return repository.findAll();
    }
}

// Schema migration automática por tenant
```

### Authentication & JWT
```java
// Gerar token com tenant
String token = jwtService.gerarToken(email, usuario.getTenantId());

// Validar token e extrair claims
String email = jwtService.getEmailFromToken(token);
UUID tenantId = jwtService.getTenantIdFromToken(token);
boolean isExpired = jwtService.isTokenExpired(token);
```

### Exception Handling
```java
@ControllerAdvice
public class ApiExceptionHandler {
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Problem> handleValidation(ValidationException ex) {
        return ResponseEntity.badRequest()
            .body(Problem.builder()
                .message("Dados inválidos")
                .detail(ex.getMessage())
                .build());
    }
}
```

## 🛠️ Comandos Essenciais

```bash
# Desenvolvimento
./mvnw spring-boot:run -Dspring.profiles.active=dev

# Build
./mvnw clean package -DskipTests

# Testes
./mvnw test
./mvnw test -Dtest=ClasseTest#metodoTest

# Docker
docker build -t salve-mais-backend .
docker run -p 3001:3001 salve-mais-backend

# Database migration
./mvnw flyway:migrate
./mvnw flyway:info
```

## ⚠️ Regras Críticas

### Git Workflow
- **NUNCA** commit direto na master
- Branch nomenclature: `feature/`, `fix/`, `refactor/`
- Commits semânticos: `feat:`, `fix:`, `docs:`, `refactor:`
- PR obrigatório com review

### Multi-tenant
- Toda operação DEVE respeitar contexto do tenant
- TenantFilter configura automaticamente
- Dados isolados por schema/tenant
- Migrations aplicadas por tenant

### Performance
- Lazy loading padrão no JPA
- Fetch joins para evitar N+1
- Pagination em queries grandes
- Connection pool otimizado (HikariCP)
- Índices no banco para campos filtrados

### Segurança
- JWT com expiração configurável
- Passwords hasheadas (BCrypt)
- Headers CORS configurados
- Rate limiting em endpoints sensíveis
- Logs sem informações sensíveis

## 🏗️ Estrutura de Código

### Controller Pattern
```java
@RestController
@RequestMapping("/api/entidades")
public class EntidadeController {
    
    @PostMapping
    public ResponseEntity<EntidadeDTO> criar(@Valid @RequestBody EntidadeDTO dto) {
        EntidadeDTO criada = service.criar(dto);
        return ResponseEntity.status(201).body(criada);
    }
    
    @GetMapping
    public ResponseEntity<Page<EntidadeDTO>> listar(Pageable pageable) {
        Page<EntidadeDTO> entidades = service.listar(pageable);
        return ResponseEntity.ok(entidades);
    }
}
```

### Service Pattern  
```java
@Service
@Transactional
public class EntidadeService {
    
    public EntidadeDTO criar(EntidadeDTO dto) {
        // Validações de negócio
        validarRegrasNegocio(dto);
        
        // Conversão e persistência
        Entidade entidade = mapper.toEntity(dto);
        entidade = repository.save(entidade);
        
        // Log da operação
        logger.info("Entidade criada: {}", entidade.getId());
        
        return mapper.toDTO(entidade);
    }
}
```

## 🐛 Troubleshooting

### Erro de Tenant
```
1. Verificar header X-Private-Tenant na requisição
2. Confirmar TenantFilter está executando
3. Validar contexto no TenantContext
4. Verificar schema do tenant no banco
```

### JWT Issues
```
1. Verificar chave secreta configurada
2. Confirmar expiração do token
3. Validar claims do tenant no token
4. Testar JwtService.validateToken()
```

### Performance Issues
```
1. Analisar queries com spring.jpa.show-sql=true
2. Verificar índices no banco de dados
3. Otimizar fetch strategies no JPA
4. Monitorar connection pool (HikariCP)
```

### Multi-tenant Problems
```
1. Verificar migration aplicada em todos os tenants
2. Confirmar isolamento de dados
3. Validar schema switching
4. Testar criação de novo tenant
```

### application.properties
```properties
# Database multi-tenant
spring.datasource.url=jdbc:postgresql://localhost:5432/gestorfinanceiro
spring.jpa.hibernate.ddl-auto=validate

# JWT
jwt.secret=your-secret-key
jwt.expiration=86400000

# Email
spring.mail.host=smtp.gmail.com
app.password.reset.url=http://localhost:4200/redefinir-senha
```

### SecurityConfig
- Endpoints públicos: `/auth/**`, `/usuarios` (POST)
- JWT Authentication Filter
- CORS configurado para frontend

## Migrations (Flyway)

### Naming Convention
- `V{version}__{description}.sql`
- Exemplo: `V29__add_tenant_id_to_password_reset_token.sql`

### Schema Multi-tenant
- Schema público para tenants
- Schemas específicos para dados dos tenants
- Migração automática via FlywayMigrationService

## Logging e Debugging

### Padrões de Log
```java
private static final Logger logger = LoggerFactory.getLogger(ClassName.class);

// Logs informativos
logger.info("Token gerado para email: {}", email);

// Warnings para situações esperadas
logger.warn("Token inválido para email: {}", email);

// Errors apenas para situações inesperadas  
logger.error("Erro inesperado: {}", ex.getMessage(), ex);
```

### MDC (Mapped Diagnostic Context)
- TenantContext automaticamente adiciona tenant_id aos logs
- Facilita debugging em ambiente multi-tenant

## Testing

### Test Structure
```java
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class ServiceTest {
    
    @MockBean
    private Repository repository;
    
    @Test
    void shouldDoSomething() {
        // Arrange, Act, Assert
    }
}
```

## Database Schema

### Tabelas Principais
- `usuario`: Usuários do sistema
- `tenants`: Configuração dos tenants
- `password_reset_token`: Tokens de recuperação
- Tabelas de negócio isoladas por tenant

### Constraints
- Foreign keys respeitam isolamento por tenant
- Indexes otimizados para queries multi-tenant

## Deployment

### Build
```bash
./mvnw clean package -DskipTests
```

### Docker
```dockerfile
FROM openjdk:21-jdk-slim
COPY target/app.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

## Troubleshooting

### Problemas Comuns

1. **Erro 403 inesperado**
   - Verificar se TenantContext está definido
   - Validar header X-Private-Tenant
   - Conferir token JWT

2. **Usuário não encontrado**
   - Verificar se usuário existe no tenant correto
   - Confirmar isolamento de dados

3. **Schema migration fails**
   - Verificar permissões do usuário do banco
   - Conferir sintaxe SQL do migration

### Debug Tips
- Habilitar SQL logging: `spring.jpa.show-sql=true`
- Log level DEBUG para pacotes específicos
- Usar breakpoints em TenantFilter para debug de contexto

## Regras de Negócio

### Multi-tenant
- Dados sempre isolados por tenant
- Context obrigatório em todas as operações
- Domain como identificador principal

### Segurança  
- JWT com expiração configurável
- Passwords hashadas com BCrypt
- Rate limiting em endpoints sensíveis

### Email
- Templates HTML para recuperação de senha
- Configuração SMTP externa
- Logs de envio para auditoria

## Performance

### JPA Optimization
- Lazy loading por padrão
- Fetch joins para evitar N+1
- Pagination em listagens

### Caching
- Spring Cache para dados estáticos
- Redis para sessões distribuídas (futuro)

### Connection Pool
- HikariCP configurado adequadamente
- Monitoring de conexões ativas