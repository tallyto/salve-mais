-- Criação das tabelas de planejamento financeiro

-- Tabela de Metas
CREATE TABLE IF NOT EXISTS metas (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(500),
    valor_alvo DECIMAL(15, 2) NOT NULL,
    valor_atual DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    data_inicio DATE NOT NULL,
    data_alvo DATE NOT NULL,
    categoria_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'EM_ANDAMENTO',
    valor_mensal_sugerido DECIMAL(15, 2),
    icone VARCHAR(50),
    cor VARCHAR(20),
    notificar_progresso BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_meta_categoria FOREIGN KEY (categoria_id) REFERENCES categoria(id) ON DELETE SET NULL,
    CONSTRAINT chk_meta_status CHECK (status IN ('EM_ANDAMENTO', 'CONCLUIDA', 'CANCELADA', 'PAUSADA')),
    CONSTRAINT chk_meta_valores CHECK (valor_alvo > 0 AND valor_atual >= 0)
);

CREATE INDEX idx_metas_status ON metas(status);
CREATE INDEX idx_metas_data_alvo ON metas(data_alvo);

-- Tabela de Planos de Compra
CREATE TABLE IF NOT EXISTS planos_compra (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(500),
    valor_total DECIMAL(15, 2) NOT NULL,
    valor_entrada DECIMAL(15, 2) DEFAULT 0.00,
    numero_parcelas INTEGER,
    taxa_juros DECIMAL(5, 2) DEFAULT 0.00,
    valor_parcela DECIMAL(15, 2),
    tipo_compra VARCHAR(30) NOT NULL,
    data_prevista DATE,
    prioridade INTEGER DEFAULT 3,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANEJADO',
    observacoes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT chk_plano_tipo CHECK (tipo_compra IN ('A_VISTA', 'PARCELADO_SEM_JUROS', 'PARCELADO_COM_JUROS', 'FINANCIAMENTO')),
    CONSTRAINT chk_plano_status CHECK (status IN ('PLANEJADO', 'EM_ANDAMENTO', 'CONCLUIDO', 'CANCELADO')),
    CONSTRAINT chk_plano_prioridade CHECK (prioridade BETWEEN 1 AND 3),
    CONSTRAINT chk_plano_valores CHECK (valor_total > 0 AND valor_entrada >= 0 AND taxa_juros >= 0)
);

CREATE INDEX idx_planos_compra_status ON planos_compra(status);
CREATE INDEX idx_planos_compra_prioridade ON planos_compra(prioridade);

-- Tabela de Plano de Aposentadoria
CREATE TABLE IF NOT EXISTS plano_aposentadoria (
    id BIGSERIAL PRIMARY KEY,
    idade_atual INTEGER NOT NULL,
    idade_aposentadoria INTEGER NOT NULL DEFAULT 65,
    renda_desejada DECIMAL(15, 2) NOT NULL,
    patrimonio_atual DECIMAL(15, 2) DEFAULT 0.00,
    contribuicao_mensal DECIMAL(15, 2),
    taxa_retorno_anual DECIMAL(5, 2) DEFAULT 8.00,
    expectativa_vida INTEGER DEFAULT 85,
    patrimonio_necessario DECIMAL(15, 2),
    patrimonio_projetado DECIMAL(15, 2),
    ativo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT chk_aposentadoria_idades CHECK (idade_atual >= 18 AND idade_aposentadoria > idade_atual AND expectativa_vida > idade_aposentadoria),
    CONSTRAINT chk_aposentadoria_valores CHECK (renda_desejada > 0 AND patrimonio_atual >= 0 AND taxa_retorno_anual >= 0)
);

-- Comentários nas tabelas
COMMENT ON TABLE metas IS 'Metas de economia e objetivos financeiros';
COMMENT ON TABLE planos_compra IS 'Planejamento de grandes compras e financiamentos';
COMMENT ON TABLE plano_aposentadoria IS 'Planejamento e simulação de aposentadoria';
