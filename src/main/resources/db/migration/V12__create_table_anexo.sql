CREATE TABLE IF NOT EXISTS anexo (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    tipo VARCHAR(100),
    dados BYTEA,
    data_upload TIMESTAMP NOT NULL,
    chave_s3 VARCHAR(500),
    conta_fixa_id BIGINT,
    CONSTRAINT fk_anexo_conta_fixa FOREIGN KEY (conta_fixa_id) REFERENCES conta_fixa(id) ON DELETE CASCADE
);
