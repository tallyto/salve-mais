-- Extensão da tabela transacao criada originalmente em V3
-- Adiciona novas colunas à tabela transacao
ALTER TABLE transacao 
    ADD COLUMN IF NOT EXISTS descricao VARCHAR(255),
    ADD COLUMN IF NOT EXISTS conta_destino_id BIGINT,
    ADD COLUMN IF NOT EXISTS fatura_id BIGINT,
    ADD COLUMN IF NOT EXISTS categoria_id BIGINT,
    ADD COLUMN IF NOT EXISTS conta_fixa_id BIGINT,
    ADD COLUMN IF NOT EXISTS observacoes TEXT,
    ADD COLUMN IF NOT EXISTS sistema BOOLEAN DEFAULT FALSE;

-- Modifica coluna data para timestamp
ALTER TABLE transacao ALTER COLUMN data TYPE TIMESTAMP;

-- Adiciona novas foreign keys
ALTER TABLE transacao 
    ADD CONSTRAINT fk_transacao_conta_destino FOREIGN KEY (conta_destino_id) REFERENCES conta(id),
    ADD CONSTRAINT fk_transacao_fatura FOREIGN KEY (fatura_id) REFERENCES fatura(id),
    ADD CONSTRAINT fk_transacao_categoria FOREIGN KEY (categoria_id) REFERENCES categoria(id),
    ADD CONSTRAINT fk_transacao_conta_fixa FOREIGN KEY (conta_fixa_id) REFERENCES conta_fixa(id);

-- Adiciona índices para melhorar a performance de consultas
CREATE INDEX idx_transacao_conta ON transacao(conta_id);
CREATE INDEX idx_transacao_tipo ON transacao(tipo);
CREATE INDEX idx_transacao_data ON transacao(data);
CREATE INDEX idx_transacao_conta_tipo ON transacao(conta_id, tipo);
CREATE INDEX idx_transacao_categoria ON transacao(categoria_id);
CREATE INDEX idx_transacao_provento ON transacao(provento_id);
CREATE INDEX idx_transacao_conta_fixa ON transacao(conta_fixa_id);
CREATE INDEX idx_transacao_fatura ON transacao(fatura_id);
