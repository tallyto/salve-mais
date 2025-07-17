-- Migration para adicionar sistema de limites aos cartões de crédito
ALTER TABLE cartao_credito 
ADD COLUMN limite_total DECIMAL(10,2),
ADD COLUMN limite_alerta_percentual INTEGER DEFAULT 80,
ADD COLUMN ativo BOOLEAN DEFAULT true;

-- Índices para melhorar performance
CREATE INDEX idx_cartao_ativo ON cartao_credito(ativo);

