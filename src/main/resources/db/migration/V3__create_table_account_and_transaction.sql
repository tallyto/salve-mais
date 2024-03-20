CREATE TABLE conta
(
    id       BIGINT AUTO_INCREMENT NOT NULL,
    saldo    DECIMAL               NULL,
    titular  VARCHAR(255)          NULL,
    CONSTRAINT pk_conta PRIMARY KEY (id)
);


CREATE TABLE transacao
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    tipo            ENUM('DEBITO', 'CREDITO') NOT NULL,
    valor           DECIMAL               NOT NULL,
    data            DATE                  NOT NULL,
    conta_id        BIGINT                NOT NULL,
    provento_id     BIGINT                NULL,
    CONSTRAINT pk_transacao PRIMARY KEY (id),
    CONSTRAINT fk_transacao_conta FOREIGN KEY (conta_id) REFERENCES conta (id),
    CONSTRAINT fk_transacao_provento FOREIGN KEY (provento_id) REFERENCES provento (id)
);


