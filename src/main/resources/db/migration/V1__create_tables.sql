CREATE TABLE cartao_credito
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    nome       VARCHAR(255)          NULL,
    vencimento date                  NULL,
    CONSTRAINT pk_cartao_credito PRIMARY KEY (id)
);

CREATE TABLE categoria
(
    id   BIGINT AUTO_INCREMENT NOT NULL,
    nome VARCHAR(255)          NULL,
    CONSTRAINT pk_categoria PRIMARY KEY (id)
);

CREATE TABLE compra
(
    id                BIGINT AUTO_INCREMENT NOT NULL,
    descricao         VARCHAR(255)          NULL,
    valor             DOUBLE                NULL,
    data              date                  NULL,
    categoria_id      BIGINT                NULL,
    cartao_credito_id BIGINT                NULL,
    CONSTRAINT pk_compra PRIMARY KEY (id)
);

ALTER TABLE compra
    ADD CONSTRAINT FK_COMPRA_ON_CARTAO_CREDITO FOREIGN KEY (cartao_credito_id) REFERENCES cartao_credito (id);

ALTER TABLE compra
    ADD CONSTRAINT FK_COMPRA_ON_CATEGORIA FOREIGN KEY (categoria_id) REFERENCES categoria (id);

CREATE TABLE conta_fixa
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    nome         VARCHAR(255)          NULL,
    categoria_id BIGINT                NULL,
    vencimento   date                  NULL,
    valor        DECIMAL               NULL,
    pago         BIT(1)                NOT NULL,
    CONSTRAINT pk_conta_fixa PRIMARY KEY (id)
);

ALTER TABLE conta_fixa
    ADD CONSTRAINT FK_CONTA_FIXA_ON_CATEGORIA FOREIGN KEY (categoria_id) REFERENCES categoria (id);

CREATE TABLE provento
(
    id        BIGINT AUTO_INCREMENT NOT NULL,
    descricao VARCHAR(255)          NULL,
    valor     DECIMAL               NULL,
    data      date                  NULL,
    CONSTRAINT pk_provento PRIMARY KEY (id)
);