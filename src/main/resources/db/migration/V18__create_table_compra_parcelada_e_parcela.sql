-- Cria tabela de compras parceladas
CREATE TABLE compra_parcelada (
    id BIGSERIAL PRIMARY KEY,
    descricao VARCHAR(255) NOT NULL,
    valor_total DECIMAL(19,2) NOT NULL,
    data_compra DATE NOT NULL,
    parcela_inicial INTEGER NOT NULL,
    total_parcelas INTEGER NOT NULL,
    categoria_id BIGINT,
    cartao_credito_id BIGINT NOT NULL,
    CONSTRAINT fk_compra_parcelada_categoria FOREIGN KEY (categoria_id) REFERENCES categoria(id),
    CONSTRAINT fk_compra_parcelada_cartao FOREIGN KEY (cartao_credito_id) REFERENCES cartao_credito(id),
    CONSTRAINT check_parcela_inicial_valida CHECK (parcela_inicial >= 1 AND parcela_inicial <= total_parcelas),
    CONSTRAINT check_total_parcelas_positivo CHECK (total_parcelas > 0)
);

-- Cria tabela de parcelas
CREATE TABLE parcela (
    id BIGSERIAL PRIMARY KEY,
    numero_parcela INTEGER NOT NULL,
    total_parcelas INTEGER NOT NULL,
    valor DECIMAL(19,2) NOT NULL,
    data_vencimento DATE NOT NULL,
    paga BOOLEAN NOT NULL DEFAULT FALSE,
    compra_parcelada_id BIGINT NOT NULL,
    CONSTRAINT fk_parcela_compra_parcelada FOREIGN KEY (compra_parcelada_id) REFERENCES compra_parcelada(id) ON DELETE CASCADE
);

-- Índices para melhorar a performance
CREATE INDEX idx_compra_parcelada_cartao ON compra_parcelada(cartao_credito_id);
CREATE INDEX idx_parcela_compra_parcelada ON parcela(compra_parcelada_id);
CREATE INDEX idx_parcela_data_vencimento ON parcela(data_vencimento);
CREATE INDEX idx_parcela_paga ON parcela(paga);
