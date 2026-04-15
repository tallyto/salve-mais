-- V31: Criar tabela usuario_global e migrar usuários de todos os schemas
-- Este script:
-- 1. Cria a tabela usuario_global no schema public
-- 2. Busca todos os schemas que existem no banco de dados
-- 3. Busca usuários em cada schema e insere na tabela usuario_global

-- ============================================================================
-- PARTE 1: Criar tabela usuario_global no schema public
-- ============================================================================

CREATE TABLE IF NOT EXISTS public.usuario_global (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    tenant_id UUID NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_usuario_global_tenant 
        FOREIGN KEY (tenant_id) 
        REFERENCES public.tenants(id) 
        ON DELETE CASCADE
);

-- Criar índices para melhorar performance
CREATE INDEX IF NOT EXISTS idx_usuario_global_email ON public.usuario_global(email);
CREATE INDEX IF NOT EXISTS idx_usuario_global_tenant_id ON public.usuario_global(tenant_id);
CREATE INDEX IF NOT EXISTS idx_usuario_global_ativo ON public.usuario_global(ativo);

-- Comentários
COMMENT ON TABLE public.usuario_global IS 'Tabela centralizada de usuários no schema public para autenticação';
COMMENT ON COLUMN public.usuario_global.email IS 'Email único do usuário';
COMMENT ON COLUMN public.usuario_global.senha IS 'Senha criptografada do usuário (BCrypt)';
COMMENT ON COLUMN public.usuario_global.tenant_id IS 'ID do tenant ao qual o usuário pertence';
COMMENT ON COLUMN public.usuario_global.ativo IS 'Flag indicando se o usuário está ativo';
COMMENT ON COLUMN public.usuario_global.criado_em IS 'Data de criação do usuário';
COMMENT ON COLUMN public.usuario_global.atualizado_em IS 'Data da última atualização';

-- ============================================================================
-- PARTE 2: Migrar usuários de todos os schemas
-- ============================================================================

DO $$
DECLARE
    v_schema_name VARCHAR;
    v_default_tenant_id UUID;
    v_users_migrated INT := 0;
    v_total_processed INT := 0;
BEGIN
    RAISE NOTICE '=== Iniciando migração de usuários para usuario_global ===';
    
    -- Passo 1: Obter um tenant padrão (preferir 'public', depois qualquer um)
    SELECT id INTO v_default_tenant_id 
    FROM public.tenants 
    WHERE domain = 'public' 
    LIMIT 1;
    
    -- Se não houver tenant 'public', usar o primeiro tenant disponível
    IF v_default_tenant_id IS NULL THEN
        SELECT id INTO v_default_tenant_id 
        FROM public.tenants 
        LIMIT 1;
    END IF;
    
    IF v_default_tenant_id IS NULL THEN
        RAISE WARNING 'Nenhum tenant encontrado! Migração abortada.';
        RETURN;
    END IF;
    
    RAISE NOTICE 'Usando tenant padrão: %', v_default_tenant_id;
    
    -- Passo 2: Buscar todos os schemas e migrar usuários
    FOR v_schema_name IN 
        SELECT schema_name 
        FROM information_schema.schemata
        WHERE schema_name NOT IN ('pg_catalog', 'information_schema', 'pg_temp_1', 'pg_toast')
        AND schema_name NOT LIKE 'pg_temp_%'
        AND schema_name NOT LIKE 'pg_toast_%'
        ORDER BY schema_name
    LOOP
        BEGIN
            RAISE NOTICE '-> Processando schema: %', v_schema_name;
            
            -- Verificar se a tabela usuario existe neste schema
            IF EXISTS (
                SELECT 1 
                FROM information_schema.tables 
                WHERE table_schema = v_schema_name 
                AND table_name = 'usuario'
            ) THEN
                -- Migrar usuários deste schema
                EXECUTE 
                    'INSERT INTO public.usuario_global (email, senha, tenant_id, ativo, criado_em, atualizado_em)
                    SELECT 
                        u.email,
                        u.senha,
                        COALESCE(u.tenant_id, $1),
                        COALESCE(u.ativo, TRUE),
                        COALESCE(u.criado_em, CURRENT_TIMESTAMP),
                        CURRENT_TIMESTAMP
                    FROM ' || quote_ident(v_schema_name) || '.usuario u
                    WHERE NOT EXISTS (
                        SELECT 1 
                        FROM public.usuario_global ug 
                        WHERE ug.email = u.email
                    )
                    ON CONFLICT (email) DO NOTHING'
                USING v_default_tenant_id;
                
                GET DIAGNOSTICS v_users_migrated = ROW_COUNT;
                v_total_processed := v_total_processed + v_users_migrated;
                
                RAISE NOTICE '   ✓ %d usuários migrados do schema %', v_users_migrated, v_schema_name;
            ELSE
                RAISE NOTICE '   - Tabela usuario não encontrada neste schema';
            END IF;
            
        EXCEPTION WHEN OTHERS THEN
            RAISE WARNING 'Erro ao migrar usuarios do schema %: %', v_schema_name, SQLERRM;
            CONTINUE;
        END;
    END LOOP;
    
    RAISE NOTICE '=== Migração concluída! Total de usuários migrados: % ===', v_total_processed;
    
END $$;

-- ============================================================================
-- PARTE 3: Log da migração (para auditoria)
-- ============================================================================

SELECT 
    COUNT(*) as total_usuarios_glob,
    COUNT(DISTINCT tenant_id) as total_tenants,
    COUNT(CASE WHEN ativo = TRUE THEN 1 END) as usuarios_ativos
FROM public.usuario_global;
