CREATE TABLE cartao_credito
(
    id         BIGSERIAL PRIMARY KEY,
    nome       VARCHAR(255),
    vencimento DATE
);

CREATE TABLE categoria
(
    id   BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255)
);

CREATE TABLE compra
(
    id                BIGSERIAL PRIMARY KEY,
    descricao         VARCHAR(255),
    valor             DOUBLE PRECISION,
    data              DATE,
    categoria_id      BIGINT,
    cartao_credito_id BIGINT,
    CONSTRAINT FK_COMPRA_ON_CARTAO_CREDITO FOREIGN KEY (cartao_credito_id) REFERENCES cartao_credito (id),
    CONSTRAINT FK_COMPRA_ON_CATEGORIA FOREIGN KEY (categoria_id) REFERENCES categoria (id)
);

CREATE TABLE conta_fixa
(
    id           BIGSERIAL PRIMARY KEY,
    nome         VARCHAR(255),
    categoria_id BIGINT,
    vencimento   DATE,
    valor        DECIMAL,
    pago         BOOLEAN NOT NULL,
    CONSTRAINT FK_CONTA_FIXA_ON_CATEGORIA FOREIGN KEY (categoria_id) REFERENCES categoria (id)
);

CREATE TABLE provento
(
    id        BIGSERIAL PRIMARY KEY,
    descricao VARCHAR(255),
    valor     DECIMAL,
    data      DATE
);