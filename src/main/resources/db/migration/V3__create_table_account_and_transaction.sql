CREATE TABLE conta
(
    id       BIGSERIAL PRIMARY KEY,
    saldo    DECIMAL,
    titular  VARCHAR(255)
);

CREATE TABLE transacao
(
    id              BIGSERIAL PRIMARY KEY,
    tipo            VARCHAR(20) NOT NULL, -- ENUM substituído por VARCHAR
    valor           DECIMAL     NOT NULL,
    data            DATE        NOT NULL,
    conta_id        BIGINT      NOT NULL,
    provento_id     BIGINT,
    CONSTRAINT fk_transacao_conta FOREIGN KEY (conta_id) REFERENCES conta (id),
    CONSTRAINT fk_transacao_provento FOREIGN KEY (provento_id) REFERENCES provento (id)
);


