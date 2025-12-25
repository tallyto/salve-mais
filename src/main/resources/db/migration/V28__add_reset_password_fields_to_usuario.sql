-- Adiciona campos para reset de senha e status ativo na tabela usuario

ALTER TABLE usuario
ADD COLUMN IF NOT EXISTS ativo BOOLEAN DEFAULT TRUE,
ADD COLUMN IF NOT EXISTS reset_password_token VARCHAR(255),
ADD COLUMN IF NOT EXISTS reset_password_token_expiry TIMESTAMP;

-- Atualizar usuários existentes para serem ativos
UPDATE usuario SET ativo = TRUE WHERE ativo IS NULL;

-- Criar índice para busca rápida por token
CREATE INDEX IF NOT EXISTS idx_usuario_reset_token ON usuario(reset_password_token);