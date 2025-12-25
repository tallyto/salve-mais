-- Migração para corrigir problema de colunas duplicadas create_user_token
-- Esta migração adiciona as colunas apenas se elas não existirem

DO $$
BEGIN
    -- Adicionar coluna create_user_token se não existir
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'tenants' 
        AND column_name = 'create_user_token'
    ) THEN
        ALTER TABLE public.tenants ADD COLUMN create_user_token VARCHAR(255);
    END IF;

    -- Adicionar coluna create_user_token_expiry se não existir
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'tenants' 
        AND column_name = 'create_user_token_expiry'
    ) THEN
        ALTER TABLE public.tenants ADD COLUMN create_user_token_expiry TIMESTAMP;
    END IF;
END $$;

-- Adicionar índice se não existir
CREATE INDEX IF NOT EXISTS idx_tenant_create_user_token ON public.tenants(create_user_token);

-- Garantir que os comentários existem
DO $$
BEGIN
    -- Adicionar comentário para create_user_token
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'tenants' 
        AND column_name = 'create_user_token'
    ) THEN
        EXECUTE 'COMMENT ON COLUMN public.tenants.create_user_token IS ''Token para criação do primeiro usuário do tenant''';
    END IF;

    -- Adicionar comentário para create_user_token_expiry
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'tenants' 
        AND column_name = 'create_user_token_expiry'
    ) THEN
        EXECUTE 'COMMENT ON COLUMN public.tenants.create_user_token_expiry IS ''Data de expiração do token de criação de usuário''';
    END IF;
END $$;