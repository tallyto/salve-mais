-- Atualizar usuários existentes que não têm tenant_id
-- Esta migração associa usuários órfãos ao tenant baseado no email domain ou ao primeiro tenant disponível

-- Primeiro, tentar associar usuários ao tenant baseado no domínio do email
UPDATE usuario u
SET tenant_id = (
    SELECT t.id 
    FROM public.tenants t 
    WHERE LOWER(t.email) = LOWER(u.email)
       OR LOWER(t.domain) = LOWER(SPLIT_PART(u.email, '@', 2))
    LIMIT 1
)
WHERE u.tenant_id IS NULL;

-- Para usuários que ainda não têm tenant_id (não foi possível associar por email),
-- associar ao primeiro tenant ativo disponível
UPDATE usuario u
SET tenant_id = (
    SELECT t.id 
    FROM public.tenants t 
    WHERE t.active = true
    ORDER BY t.created_at ASC
    LIMIT 1
)
WHERE u.tenant_id IS NULL;

-- Comentário
COMMENT ON COLUMN usuario.tenant_id IS 'ID do tenant ao qual o usuário pertence - atualizado para usuários existentes';
