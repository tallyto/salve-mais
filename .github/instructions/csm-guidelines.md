# Instruções para IA - Custom Software Management (CSM) - Backend

## Contexto do Projeto
Sistema de gestão financeira multi-tenant com Spring Boot, PostgreSQL e arquitetura REST. Utiliza JWT para autenticação e isolamento de dados por tenant via header HTTP.

## Estrutura do Projeto

### Pacotes Principais
```
com.tallyto.gestorfinanceiro
├── api/controllers          # Endpoints REST
├── config                   # Configurações (Security, CORS, etc)
├── context                  # TenantContext para multi-tenancy
├── core/
│   ├── application/services # Lógica de negócio
│   ├── domain/entities      # Entidades JPA
│   ├── domain/exceptions    # Exceções customizadas
│   └── infra/repositories   # Repositories JPA
```

### Padrões de Arquitetura
- **Controller**: Recebe requisições, valida entrada, chama Service
- **Service**: Lógica de negócio, transações, chama Repository
- **Repository**: Acesso a dados, queries JPA
- **Entity**: Mapeamento ORM para tabelas

## Multi-Tenancy

### TenantContext
```java
// Obtém tenant atual
String tenant = TenantContext.getCurrentTenant();

// Define tenant no contexto
TenantContext.setCurrentTenant("tenant-domain");
```

### TenantFilter
- Intercepta requisições HTTP
- Extrai header `X-Private-Tenant`
- Define contexto do tenant
- Aplica migração schema se necessário

## Autenticação e Segurança

### JWT Service
- Geração de tokens com tenant
- Validação e extração de claims
- Integração com Spring Security

### Tratamento de Erros
```java
@ExceptionHandler(UsernameNotFoundException.class)
public ResponseEntity<Problem> handleUsernameNotFound(Exception ex) {
    return new ResponseEntity<>(
        Problem.builder()
            .message("Acesso negado")
            .detail("Token inválido ou expirado")
            .build(),
        HttpStatus.FORBIDDEN
    );
}
```

## Recuperação de Senha

### Fluxo Implementado
1. `AuthController.recuperarSenha()` recebe email
2. Busca usuário e domain do tenant
3. Gera token UUID e armazena em `PasswordResetToken`
4. Inclui domain na URL: `?token=xxx&domain=tenant.com`
5. Envia email com link completo

### Implementação
```java
// Obter tenant do contexto
String tenantDomain = TenantContext.getCurrentTenant();

// Incluir no link
String link = passwordResetUrl + "?token=" + token + "&domain=" + tenantDomain;
```

## Configurações Importantes

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