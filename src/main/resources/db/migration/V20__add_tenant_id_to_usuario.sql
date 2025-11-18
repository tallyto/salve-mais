-- Adicionar tenant_id à tabela de usuários
ALTER TABLE usuario ADD COLUMN tenant_id UUID;

-- Adicionar constraint de foreign key
ALTER TABLE usuario ADD CONSTRAINT fk_usuario_tenant 
    FOREIGN KEY (tenant_id) REFERENCES public.tenants(id) ON DELETE CASCADE;

-- Criar índice para melhorar performance
CREATE INDEX idx_usuario_tenant_id ON usuario(tenant_id);

-- Comentário
COMMENT ON COLUMN usuario.tenant_id IS 'ID do tenant ao qual o usuário pertence';
