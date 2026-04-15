# Refatoração de Arquitetura: Centralização de Usuários em Schema Public

## Visão Geral
Este documento descreve as mudanças realizadas para refatorar a arquitetura do sistema, centralizando os usuários em uma tabela `usuario_global` no schema `public`. Isso permite um login unificado que busca o tenant do usuário e usa um JWT com informações de tenant para rotear os requests para o schema correto.

## Problema Solved
**Antes:** Usuários eram armazenados em cada schema (tenant-specific), tornando o login complexo pois não há forma de saber qual tenant o usuário pertence antes de autenticar.

**Depois:** Usuários são centralizados em `usuario_global` no schema `public`. O login é simples e obtém diretamente o tenant do usuário. O JWT contém o `tenantDomain` para que os requests subsequentes sejam roteados para o schema correto.

## Mudanças Implementadas

### 1. **Migrações Flyway**

#### V31__create_usuario_global_table.sql
- Cria a tabela `usuario_global` no schema `public`
- Campos: `id`, `email` (unique), `senha`, `tenant_id`, `ativo`, `criado_em`, `atualizado_em`
- Foreign key para `tenants(id)`
- Índices para melhor performance: email, tenant_id, ativo

#### V32__migrate_usuarios_to_global.sql
- Migra usuários existentes do schema `public` para `usuario_global`
- Usa um bloco PL/SQL para fazer INSERT SELECT com tratamento de conflitos
- Associa usuários ao tenant correto

### 2. **Novas Entidades e Repositories**

#### UsuarioGlobal.java
- Entity JPA mapeada para `usuario_global` no schema `public`
- Campos: id, email, senha, tenantId, ativo, criadoEm, atualizadoEm
- Não herda de classe base - é isolada e central

#### UsuarioGlobalRepository.java
- Repository Spring Data JPA com métodos:
  - `findByEmail(email)` - busca por email (usado no login)
  - `findByTenantId(tenantId)` - busca usuários de um tenant
  - `existsByEmail(email)` - verifica se email existe
  - `findByTenantIdAndAtivo(tenantId, ativo)` - busca usuários ativos

### 3. **Serviços Atualizados**

#### JwtService.java
- **Novo método:** `gerarToken(email, tenantId, tenantDomain)`
- Agora inclui `tenantDomain` no JWT (além de `tenantId`)
- **Novo método:** `getTenantDomainFromToken(token)` - extrai domain do token
- Mantém compatibilidade com método antigo `gerarToken(email, tenantId)`

#### UsuarioDetailsService.java
- **Refatorado:** Agora busca usuários em `UsuarioGlobalRepository` (schema public)
- Valida se usuário está ativo
- Mantém a mesma interface para Spring Security

#### UsuarioService.java
- **Refatorado:** Sincronização automática com `usuario_global` quando:
  - Usuário é criado (cadastrar)
  - Senha é atualizada (atualizarSenhaPorEmail, atualizarSenhaComValidacao)
- **Compatibilidade:** Mantém métodos antigos que operam em `usuario` local
- Injeta `UsuarioGlobalRepository` para sincronização

#### JwtAuthenticationFilter.java
- **Refatorado:** Agora extrai `tenantDomain` do JWT
- Define `TenantContext` com o domain extraído do JWT
- Executa migração do schema do tenant se necessário
- Limpa o contexto ao final do processamento

### 4. **Controllers Atualizados**

#### AuthController.java
- **Refatorado método /login:**
  1. Autentica com AuthenticationManager (que usa UsuarioDetailsService)
  2. Busca usuário em `UsuarioGlobalRepository` para obter tenantId
  3. Busca Tenant para obter o domain
  4. Valida se usuário está ativo
  5. Atualiza último acesso em `usuario_global`
  6. Gera JWT com email, tenantId e tenantDomain
  
- **Refatorado método /recuperar-senha:**
  - Busca usuário em `UsuarioGlobalRepository`
  - Obtém domain do tenant
  - Usa domain para gerar link de recuperação
  
- **Refatorado método /redefinir-senha:**
  - Atualiza senha em `usuario_global`
  - Sincroniza com `usuario` local (compatibilidade temporária)

## Fluxo de Autenticação (Novo)

```
1. Cliente faz POST /api/auth/login com email e senha

2. AuthController:
   - Autentica com AuthenticationManager
   - Busca UsuarioGlobal por email
   - Busca Tenant para obter domain
   - Gera JWT com {email, tenantId, tenantDomain}

3. Cliente usa JWT em requests subsequentes (header: Authorization: Bearer token)

4. JwtAuthenticationFilter:
   - Extrai email e tenantDomain do JWT
   - Define TenantContext com domain
   - Executa migração do schema se necessário
   - Carrega UserDetails de UsuarioDetailsService
   - Configura SecurityContext
   - Limpa TenantContext ao final

5. Servlço recebe request com TenantContext definido
   - Efetua operação no schema do tenant
```

## Sincronização de Dados

### Direção: usuario_local → usuario_global
- Quando um novo usuário é criado em um tenant, ele é:
  1. Criado em `usuario` (schema específico)
  2. Sincronizado automaticamente para `usuario_global` (schema public)

- Quando senha é atualizada em `usuario`:
  1. Atualização é feita em `usuario`
  2. Sincronização automática ocorre em `usuario_global`

### Direção: usuario_global → usuario_local (login)
- Quando usuário faz login:
  1. Credenciais são validadas em `usuario_global`
  2. tenantId é obtido de `usuario_global`
  3. JWT é gerado com tenantDomain
  4. TenantContext é definido para acessar schema correto

## Compatibilidade

- ✅ Tabela `usuario` permanece intacta (não foi removida)
- ✅ Todos os métodos antigos em UsuarioService continuam funcionando
- ✅ Dados existentes são migrados automaticamente (V32)
- ✅ Atualizações em `usuario` também atualizam `usuario_global` (sincronização bidirecional parcial)
- ⚠️ Longo prazo: Remover tabela `usuario` e manter apenas `usuario_global`

## Próximas Etapas Recomendadas

1. **Testes:**
   - Testar login com múltiplos tenants
   - Testar JWT com e sem tenantDomain
   - Testar TenantContext em requests autenticados

2. **Migração Completa (Futuro):**
   - Remover sincronização bidirecional (usuario → usuario_global)
   - Remover references a TenantContext.getCurrentTenant() no login
   - Remove tabela `usuario` após período de transição

3. **Performance:**
   - Monitorar queries em `usuario_global`
   - Considerar cache para tenant lookups
   - Analisar impact de índices

4. **Segurança:**
   - Implementar rate limiting no /login endpoint
   - Considerar adicionar refresh tokens
   - Implementar audit trail para logins

## Arquivos Modificados

```
Migrações:
- V31__create_usuario_global_table.sql (novo)
- V32__migrate_usuarios_to_global.sql (novo)

Entidades:
- UsuarioGlobal.java (novo)

Repositories:
- UsuarioGlobalRepository.java (novo)

Serviços:
- JwtService.java (refatorado)
- UsuarioDetailsService.java (refatorado)
- UsuarioService.java (refatorado)

Configuração:
- JwtAuthenticationFilter.java (refatorado)

Controllers:
- AuthController.java (refatorado)
```

## Rollback

Se necessário fazer rollback:
1. Remover injeção de `UsuarioGlobalRepository` dos services
2. Reverter métodos em `AuthController`, `UsuarioService`, `UsuarioDetailsService`
3. Executar `TRUNCATE TABLE public.usuario_global`
4. Código antigo em `git stash` pode ser recuperado

---

**Status:** ✅ Implementação Completa
**Data:** 15 de Abril de 2026
**Compatibilidade:** Java 17+, Spring Boot 3.x
