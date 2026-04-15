# Guia de Migração de Usuários para usuario_global

## Resumo
Este guia explica como os usuários existentes são copiados para a nova tabela centralizada `usuario_global` durante as migrações do Flyway.

## Processo de Migração

### Fase 1: Criação da Tabela (V31)
- Cria tabela `usuario_global` no schema `public`
- Estrutura: id, email (unique), senha, tenant_id, ativo, criado_em, atualizado_em
- Foreign key para `tenants(id)`
- Índices para performance

### Fase 2: Migração do Schema Public (V32)
- Busca todos os usuários em `public.usuario`
- Para cada usuário:
  - Se tem `tenant_id`: usa o tenant do usuário
  - Se não tem `tenant_id`: usa tenant padrão (público)
  - Se falta tenant padrão: usa primeiro tenant disponível
- Insere em `usuario_global` com `ON CONFLICT` para evitar duplicatas
- Log de estatísticas da migração

### Fase 3: Migração de Tenants (V33)
- Itera sobre TODOS os tenants com `active = true`
- Para cada tenant:
  - Verifica se o schema do tenant existe
  - Se existir, copia usuários de `<schema>.usuario` para `usuario_global`
  - Valida com `tenant_id` correto
  - Log de progresso

## Fluxo de Dados

```
ANTES (Schema-based multitenancy):
┌─────────────────────────────────────┐
│         DATABASE DEFAULT            │
├──────────────┬──────────────────────┤
│ public       │ public.usuario       │
│              │ public.tenants       │
├──────────────┼──────────────────────┤
│ tenant1      │ tenant1.usuario ──┐  │
│ schema       │ tenant1.data   <──┤  │
├──────────────┼──────────────────────┤
│ tenant2      │ tenant2.usuario ──┐  │
│ schema       │ tenant2.data   <──┤  │
└──────────────┴──────────────────────┘

DEPOIS (Usuários centralizados):
┌─────────────────────────────────────┐
│       DATABASE DEFAULT              │
├──────────────────────────────────────┤
│ public.usuario_global (NOVO)        │
│ ↑                                    │
│ └── centraliza LOGIN                │
│                                      │
│ public.usuario (obsoleto)           │
│ public.tenants                      │
│                                      │
│ tenant1.usuario (continua)          │
│ tenant1.data                        │
│                                      │
│ tenant2.usuario (continua)          │
│ tenant2.data                        │
└──────────────────────────────────────┘
```

## Verificação da Migração

### 1. Consultar Estatísticas Gerais
```sql
-- Verificar total de usuarios migrados
SELECT 
    'Total geral' as tipo,
    COUNT(*) as quantidade
FROM public.usuario_global
UNION ALL
SELECT 
    'Ativos',
    COUNT(*) 
FROM public.usuario_global WHERE ativo = true
UNION ALL
SELECT 
    'Inativos',
    COUNT(*) 
FROM public.usuario_global WHERE ativo = false;
```

### 2. Verificar por Tenant
```sql
-- Ver distribuicao de usuarios por tenant
SELECT 
    t.domain as tenant,
    COUNT(ug.id) as total_usuarios,
    COUNT(CASE WHEN ug.ativo = true THEN 1 END) as ativos
FROM public.tenants t
LEFT JOIN public.usuario_global ug ON ug.tenant_id = t.id
GROUP BY t.id, t.domain
ORDER BY t.domain;
```

### 3. Verificar Integridade
```sql
-- Verificar se existem gaps entre tabelas
-- Usuarios em usuario local que NAO estao em usuario_global
SELECT DISTINCT
    u.email,
    u.tenant_id,
    'MIGRADO?' as status
FROM public.usuario u
LEFT JOIN public.usuario_global ug ON u.email = ug.email
WHERE ug.id IS NULL;
```

### 4. Verificar Emails Duplicados
```sql
-- Verificar se ha emails duplicados em usuario_global
SELECT 
    email,
    COUNT(*) as total
FROM public.usuario_global
GROUP BY email
HAVING COUNT(*) > 1;
```

## Troubleshooting

### Problema: Usuarios nao foram migrados
**Causa possível:** Tenants sem o campo `active = true` ou sem schema criado

**Solução:**
```sql
-- Verificar tenants
SELECT id, domain, active FROM public.tenants;

-- Confirmar se schema existe
SELECT schema_name FROM information_schema.schemata 
WHERE schema_name IN ('tenant1', 'tenant2', 'etc');

-- Executar migracao manualmente
-- (Execute novamente V32 e V33, ou use queries de INSERT direto)
```

### Problema: Duplicatas de Email
**Causa possível:** Migração parcial com re-execução

**Solução:** `ON CONFLICT (email) DO UPDATE` ja trata isso automaticamente

### Problema: Tenor ID faltando
**Causa possível:** Tenant foi deletado apos criar usuario

**Solução:**
```sql
-- Verificar usuarios orfaos
SELECT COUNT(*) 
FROM public.usuario_global 
WHERE tenant_id NOT IN (SELECT id FROM public.tenants);

-- Se necessario, reatribuir ao tenant padrao
UPDATE public.usuario_global
SET tenant_id = (SELECT id FROM public.tenants WHERE domain = 'public')
WHERE tenant_id NOT IN (SELECT id FROM public.tenants);
```

## Pós-Migração

### Próximos Passos
1. **Testar Login:** Verificar se usuarios conseguem fazer login
2. **Monitorar Logs:** Procurar por `usuario_global` em ERROR LOGS
3. **Performace:** Analisar queries em `usuario_global`
4. **Backup:** Fazer backup antes de remover tabelas antigas

### Remover Tabelas Antigas (FUTURO)
Depois de um período de transição (recomendado: 1-3 meses):

```sql
-- Apenas DEPOIS de confirmar que tu do funciona
-- DROP TABLE public.usuario;
-- Mas manter usuario em schemas tenant-specific por enquanto
```

## Monitoramento

### Query para monitorar status de migracao
```sql
WITH usuario_stats AS (
    SELECT 
        'usuario_global' as tabela,
        COUNT(*) as total,
        COUNT(DISTINCT tenant_id) as tenants_unicos
    FROM public.usuario_global
),
tenant_stats AS (
    SELECT 
        COUNT(*) as total_tenants,
        COUNT(CASE WHEN active = true THEN 1 END) as ativos
    FROM public.tenants
)
SELECT 
    u.tabela,
    u.total as usuarios_migrados,
    u.tenants_unicos as tenants_com_usuarios,
    t.total_tenants as total_de_tenants,
    ROUND(u.total::numeric / NULLIF(t.total_tenants, 0), 2) as media_usuarios_por_tenant
FROM usuario_stats u, tenant_stats t;
```

## Status de Migração

| Stage | Migracao | Status | Descricao |
|-------|----------|--------|-----------|
| 1 | V31 | ✓ | Cria tabela usuario_global |
| 2 | V32 | ✓ | Copia usuarios do schema public |
| 3 | V33 | ✓ | Copia usuarios de tenant schemas |
| 4 | - | 🔄 | Testar autenticacao com novo usuario_global |
| 5 | - | ⏳ | Remover tabelas antigas (FUTURO) |

---

**Última Atualização:** 15 de Abril de 2026
**Versão:** 1.0
