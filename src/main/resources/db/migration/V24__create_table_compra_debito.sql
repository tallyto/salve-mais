CREATE TABLE compra_debito (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    categoria_id BIGINT,
    conta_id BIGINT NOT NULL,
    data_compra DATE NOT NULL,
    valor DECIMAL(19, 2) NOT NULL,
    observacoes TEXT,
    FOREIGN KEY (categoria_id) REFERENCES categoria(id),
    FOREIGN KEY (conta_id) REFERENCES conta(id)
);

ALTER TABLE anexo ADD COLUMN compra_debito_id BIGINT;
ALTER TABLE anexo ADD CONSTRAINT fk_anexo_compra_debito FOREIGN KEY (compra_debito_id) REFERENCES compra_debito(id);
