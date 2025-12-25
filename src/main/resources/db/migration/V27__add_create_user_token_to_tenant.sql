-- Adicionar colunas para token de criação de usuário na tabela tenants
ALTER TABLE public.tenants
ADD COLUMN create_user_token VARCHAR(255),
ADD COLUMN create_user_token_expiry TIMESTAMP;

-- Adicionar índice para busca rápida por token
CREATE INDEX idx_tenant_create_user_token ON public.tenants(create_user_token);

-- Comentários
COMMENT ON COLUMN public.tenants.create_user_token IS 'Token para criação do primeiro usuário do tenant';
COMMENT ON COLUMN public.tenants.create_user_token_expiry IS 'Data de expiração do token de criação de usuário';
