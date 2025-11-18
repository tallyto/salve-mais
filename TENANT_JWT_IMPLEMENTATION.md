# Implementação: TenantId no JWT

## Resumo das Alterações

O ID do tenant foi adicionado ao token JWT do usuário para permitir identificação automática do tenant a partir do token de autenticação.

## Mudanças no Backend (Java/Spring Boot)

### 1. **Entidade Usuario**
- ✅ Adicionado campo `tenantId` (UUID)
- ✅ Adicionados getters e setters

### 2. **Migration V20**
```sql
ALTER TABLE usuario ADD COLUMN tenant_id UUID;
ALTER TABLE usuario ADD CONSTRAINT fk_usuario_tenant 
    FOREIGN KEY (tenant_id) REFERENCES public.tenants(id) ON DELETE CASCADE;
CREATE INDEX idx_usuario_tenant_id ON usuario(tenant_id);
```

### 3. **JwtService**
- ✅ Método `gerarToken` atualizado para receber `UUID tenantId`
- ✅ TenantId incluído nos claims do JWT
- ✅ Novo método `getTenantIdFromToken(String token)` para extrair tenantId

**Exemplo de payload JWT:**
```json
{
  "tenantId": "123e4567-e89b-12d3-a456-426614174000",
  "sub": "usuario@example.com",
  "iat": 1700000000,
  "exp": 1700086400
}
```

### 4. **AuthController**
- ✅ Ao fazer login, busca o usuário para obter o `tenantId`
- ✅ Passa o `tenantId` ao gerar o token

### 5. **UsuarioService**
- ✅ No método `cadastrar`, obtém o tenant do `TenantContext`
- ✅ Define automaticamente o `tenantId` ao criar usuário

### 6. **UsuarioResponseDTO**
- ✅ Adicionado campo `tenantId` no DTO de resposta

## Mudanças no Frontend (Angular)

### 1. **Usuario Model**
```typescript
export interface Usuario {
  id: number;
  nome: string;
  email: string;
  criadoEm: string;
  ultimoAcesso?: string;
  tenantId?: string; // ✅ Novo campo
}
```

### 2. **JWT Utils**
Novas funções utilitárias:
```typescript
// Decodificar token completo
decodeToken(token: string): JwtPayload

// Extrair tenantId
getTenantIdFromToken(token: string): string | null

// Extrair email
getEmailFromToken(token: string): string | null
```

## Como Usar

### Backend

**Gerar token com tenantId:**
```java
Usuario usuario = usuarioService.buscarPorEmail(email);
String token = jwtService.gerarToken(email, usuario.getTenantId());
```

**Extrair tenantId do token:**
```java
UUID tenantId = jwtService.getTenantIdFromToken(token);
```

### Frontend

**Extrair informações do token:**
```typescript
import { getTenantIdFromToken, getEmailFromToken } from './utils/jwt.util';

const token = localStorage.getItem('token');
const tenantId = getTenantIdFromToken(token!);
const email = getEmailFromToken(token!);

console.log('Tenant ID:', tenantId);
console.log('Email:', email);
```

**Usar em um guard ou interceptor:**
```typescript
export class TenantInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = localStorage.getItem('token');
    
    if (token) {
      const tenantId = getTenantIdFromToken(token);
      
      if (tenantId) {
        // Adicionar tenantId aos headers ou usar conforme necessário
        console.log('Request para tenant:', tenantId);
      }
    }
    
    return next.handle(req);
  }
}
```

## Benefícios

1. **Identificação Automática**: Não é necessário passar o tenant separadamente
2. **Segurança**: TenantId está criptografado no JWT
3. **Rastreabilidade**: Todas as requisições podem ser associadas ao tenant
4. **Facilidade**: Frontend pode extrair o tenant diretamente do token
5. **Auditoria**: Logs podem incluir automaticamente o tenant de cada operação

## Testes

Execute os testes para validar:

```bash
# Backend
mvn test

# Frontend
npm test
```

## Próximos Passos Sugeridos

1. **Interceptor HTTP**: Criar interceptor que adiciona automaticamente o tenantId nos headers
2. **Guard de Tenant**: Validar se o tenant do token corresponde ao tenant da URL
3. **Middleware**: Adicionar validação de tenant em todas as requisições
4. **Analytics**: Usar tenantId para métricas por cliente
5. **Rate Limiting**: Aplicar limites diferentes por tenant
