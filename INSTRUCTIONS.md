# Instruções para Implementação de Funcionalidades Multi-Tenant

## Recuperação de Senha com Tenant

### Passos Implementados:

#### 1. **Backend (Java/Spring Boot)**

**AuthController.java:**
- Buscar domínio do tenant do usuário ou contexto atual
- Incluir domain no link de recuperação: `?token=xxx&domain=tenant.com`
- Usar `TenantContext.getCurrentTenant()` para obter tenant do contexto

**JwtAuthenticationFilter.java:**
- Capturar `UsernameNotFoundException` e retornar 403 ao invés de 500
- Log menos verboso (warning ao invés de error)
- Resposta JSON estruturada para o frontend

**ApiExceptionHandler.java:**
- Handler específico para `UsernameNotFoundException`
- Status 403 (Forbidden) com mensagem amigável

#### 2. **Frontend (Angular)**

**RedefinirSenhaComponent:**
- Extrair `domain` dos query parameters da URL
- Passar domain para AuthService nas chamadas

**AuthService:**
- Métodos `redefinirSenha()` e `verificarToken()` aceitam parâmetro `domain`
- Adicionar header `X-Private-Tenant` quando domain é fornecido

#### 3. **Versionamento e Release**

**Changelog:**
- Documentar mudanças em ambos os projetos
- Incrementar versões (backend: 1.18.0, frontend: 1.32.0)

**Git Workflow:**
```bash
# Criar branch feature
git checkout -b feature/password-reset-with-tenant

# Fazer alterações e commit
git add .
git commit -m "feat: descrição da funcionalidade"

# Enviar para GitHub
git push -u origin feature/password-reset-with-tenant

# Criar PR via GitHub web interface
```

### Padrões a Seguir:

1. **Sempre incluir tenant no contexto de URLs sensíveis**
2. **Tratar erros de autenticação com status apropriados (403 vs 500)**
3. **Logs informativos mas não verbosos para segurança**
4. **Headers consistentes para multi-tenant (`X-Private-Tenant`)**
5. **Versionamento semântico e changelog detalhado**
6. **PRs para revisão antes do merge**

### Links dos PRs:

- Backend: https://github.com/tallyto/salve-mais/pull/new/feature/password-reset-with-tenant
- Frontend: https://github.com/tallyto/salve-mais-ui/pull/new/feature/password-reset-with-tenant

### Próximos Passos:

1. Revisar PRs
2. Testar funcionalidade em ambiente de dev
3. Merge após aprovação
4. Deploy das versões 1.18.0 (backend) e 1.32.0 (frontend)