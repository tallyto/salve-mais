# Guia para Code Review e IA - Salve Mais

Este documento contém informações essenciais para revisores de código e assistentes de IA que trabalham no projeto Salve Mais.

## Arquitetura do Projeto

### Backend (Spring Boot)
- **Linguagem**: Java 17+
- **Framework**: Spring Boot 3.x
- **Banco de Dados**: PostgreSQL com multi-tenancy (schema por tenant)
- **Autenticação**: JWT com domínio do tenant
- **Email**: JavaMailSender com templates HTML
- **Storage**: AWS S3 para comprovantes
- **Migrations**: Flyway

### Frontend (Angular)
- **Framework**: Angular 17+
- **UI**: Angular Material
- **Arquitetura**: Módulos standalone, Reactive Forms
- **Autenticação**: Interceptor HTTP com JWT
- **Multi-tenancy**: Contexto por domínio

## Estrutura de Pastas

### Backend
```
src/main/java/com/tallyto/gestorfinanceiro/
├── api/
│   ├── controllers/          # REST Controllers
│   └── dto/                  # Data Transfer Objects
├── core/
│   ├── application/services/ # Business Logic
│   ├── domain/entities/      # JPA Entities
│   └── infra/repositories/   # Data Access
├── context/                  # TenantContext
└── config/                   # Configurações Spring

src/main/resources/
├── db/migration/             # Flyway migrations (Vxx__description.sql)
├── templates/                # Email HTML templates
└── application.properties    # Configurações
```

### Frontend
```
src/app/
├── components/               # Componentes de UI
├── services/                 # Serviços HTTP
├── models/                   # Interfaces/Models
├── directives/               # Diretivas Angular
└── utils/                    # Utilidades

src/environments/
├── environment.ts            # Dev
├── environment.prod.ts       # Production
└── environment.local.ts      # Local
```

## Padrões de Código

### Backend

#### Controllers
- Use `@RestController` e `@RequestMapping`
- Documente com Swagger (`@Operation`, `@ApiResponses`)
- Validação com `@Valid` nos DTOs
- ResponseEntity com status HTTP apropriados

```java
@RestController
@RequestMapping("/api/recurso")
@Tag(name = "Nome", description = "Descrição")
public class RecursoController {
    @Autowired
    private RecursoService service;
    
    @PostMapping
    public ResponseEntity<ResponseDTO> criar(@Valid @RequestBody RequestDTO dto) {
        // implementação
    }
}
```

#### Services
- Lógica de negócio isolada
- `@Transactional` para operações de escrita
- Validações de tenant context
- Logs com SLF4J

```java
@Service
public class RecursoService {
    @Transactional
    public ResponseDTO criar(RequestDTO dto) {
        String domain = TenantContext.getCurrentTenant();
        // validações e lógica
    }
}
```

#### Entities
- Use JPA annotations
- Lombok para getters/setters (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- Auditoria com timestamps (`createdAt`, `updatedAt`)
- Multi-tenancy: entidades não precisam de `tenant_id` (filtrado por schema)

#### Migrations
- Padrão: `Vxx__description.sql`
- SEMPRE use `IF NOT EXISTS` para evitar conflitos
- Execute no schema public para tabelas globais (tenant, usuario)
- Execute em cada schema para tabelas do tenant

### Frontend

#### Components
- Use standalone components quando possível
- Reactive Forms para formulários complexos
- Material Design components
- Tipagem forte com TypeScript

```typescript
@Component({
    selector: 'app-componente',
    templateUrl: './componente.component.html',
    styleUrls: ['./componente.component.css'],
    standalone: false
})
export class ComponenteComponent implements OnInit {
    form!: FormGroup;
    
    constructor(private fb: FormBuilder, private service: Service) {}
}
```

#### Services
- `providedIn: 'root'` para singleton
- Observable pattern para HTTP
- Tratamento de erros consistente
- Tipagem de interfaces

```typescript
@Injectable({
    providedIn: 'root'
})
export class RecursoService {
    private apiUrl = environment.apiUrl + '/recurso';
    
    constructor(private http: HttpClient) {}
    
    obter(): Observable<Recurso> {
        return this.http.get<Recurso>(this.apiUrl);
    }
}
```

## Funcionalidades Principais

### Multi-tenancy
- Cada tenant tem seu próprio schema PostgreSQL
- Context gerenciado por `TenantContext`
- Autenticação via domínio no JWT
- Isolamento completo de dados

### Notificações
- **Sistema de Notificações**: Alertas no frontend sobre contas/faturas
- **Notificações por Email**: Scheduler que envia resumo diário configurável
  - Configurável por tenant (horário, ativo/inativo)
  - Templates HTML em `templates/notificacao-diaria.html`
  - Scheduler roda a cada hora (cron: `0 0 * * * *`)
  - Timezone: America/Sao_Paulo
  - Endpoint de teste: `POST /api/notificacoes-email/testar`

### Autenticação
- JWT com claims: `sub` (email), `domain` (tenant)
- Interceptor no frontend adiciona token
- Filtro no backend valida e configura context

### Upload de Arquivos
- AWS S3 para comprovantes
- Presigned URLs com expiração de 30 minutos
- Limite: 10MB por arquivo

## Dependências Principais

### Backend
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Mail
- Spring Boot Starter Validation
- PostgreSQL Driver
- Flyway Migration
- AWS SDK S3
- Lombok
- JWT (io.jsonwebtoken)

### Frontend
- Angular Material
- RxJS
- Chart.js
- ExcelJS
- File-saver

## Configurações de Ambiente

### Backend (.env ou variáveis)
```properties
DB_URL=jdbc:postgresql://localhost:5432/salvemais
DB_USERNAME=postgres
DB_PASSWORD=senha
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587
EMAIL_USERNAME=usuario@gmail.com
EMAIL_PASSWORD=senha-app
EMAIL_SENDER=noreply@salvemais.com
AWS_ACCESS_KEY=xxx
AWS_SECRET_KEY=xxx
AWS_REGION=us-east-1
AWS_S3_BUCKET=bucket-name
```

### Frontend (environment.ts)
```typescript
export const environment = {
    production: false,
    apiUrl: 'http://localhost:3001/api'
};
```

## Boas Práticas para Code Review

### Checklist Backend
- [ ] Controllers documentados com Swagger
- [ ] Services com `@Transactional` quando necessário
- [ ] Validação de tenant context
- [ ] DTOs validados com Bean Validation
- [ ] Tratamento de exceções adequado
- [ ] Logs informativos (não excessivos)
- [ ] Migrations com `IF NOT EXISTS`
- [ ] Testes unitários (quando aplicável)

### Checklist Frontend
- [ ] Componentes com tipagem forte
- [ ] Forms com validação
- [ ] Tratamento de erros HTTP
- [ ] Loading states e feedback visual
- [ ] Responsividade (Material Design)
- [ ] Sem vazamento de memória (unsubscribe)
- [ ] Acessibilidade básica (labels, aria)

### Checklist Segurança
- [ ] Validação de entrada (backend e frontend)
- [ ] Sanitização de dados
- [ ] Autorização por tenant
- [ ] Senhas nunca em logs
- [ ] HTTPS em produção
- [ ] CORS configurado corretamente
- [ ] JWT com expiração adequada

## Comandos Úteis

### Backend
```bash
# Compilar
./mvnw clean install

# Rodar
./mvnw spring-boot:run

# Rodar testes
./mvnw test

# Criar migration
# Arquivo: src/main/resources/db/migration/Vxx__description.sql
```

### Frontend
```bash
# Instalar dependências
npm install

# Rodar dev
npm start

# Build produção
npm run build

# Testes
npm test
```

## Troubleshooting Comum

### Backend
- **Erro de schema não encontrado**: Verificar se tenant existe na tabela `public.tenant`
- **Flyway conflito**: Adicionar `IF NOT EXISTS` nas migrations
- **Email não envia**: Verificar credenciais e portas SMTP
- **Dependência circular**: Usar `@Lazy` em um dos `@Autowired`

### Frontend
- **CORS error**: Verificar configuração no backend
- **Token expirado**: Implementar refresh token ou logout
- **Componente não encontrado**: Verificar import no módulo
- **Erro de compilação**: Limpar cache (`rm -rf node_modules && npm install`)

## Convenções de Nomenclatura

### Backend
- **Classes**: PascalCase (`UsuarioService`)
- **Métodos**: camelCase (`obterUsuario`)
- **Constantes**: UPPER_SNAKE_CASE (`MAX_UPLOAD_SIZE`)
- **Packages**: lowercase (`com.tallyto.gestorfinanceiro`)

### Frontend
- **Components**: kebab-case (`usuario-form.component.ts`)
- **Services**: kebab-case (`usuario.service.ts`)
- **Interfaces**: PascalCase (`Usuario`, `UsuarioDTO`)
- **Variáveis**: camelCase (`nomeUsuario`)

### Database
- **Tables**: snake_case (`usuario`, `conta_fixa`)
- **Columns**: snake_case (`created_at`, `tenant_id`)
- **Indexes**: `idx_table_column` (`idx_usuario_email`)

## Fluxo de Desenvolvimento

1. **Feature Branch**: Criar branch a partir de `master`
2. **Desenvolvimento**: Implementar feature/fix
3. **Testes Locais**: Testar backend e frontend
4. **Code Review**: Revisar contra este guia
5. **Merge**: Após aprovação, merge para `master`
6. **Deploy**: CI/CD automático ou manual

## Contato e Suporte

- **Repositório**: GitHub - tallyto/salve-mais
- **Ambiente de Produção**: https://salvemais.lyto.com.br
- **Backend API**: https://salvemais.lyto.com.br/api

---

**Última atualização**: Dezembro 2025
