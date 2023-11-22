CREATE TABLE fatura
(
    id                BIGINT AUTO_INCREMENT NOT NULL,
    data_vencimento   DATE                  NOT NULL,
    data_pagamento    DATE,
    valor_total       DECIMAL(10, 2)        NOT NULL,
    pago              BOOLEAN               NOT NULL,
    cartao_credito_id BIGINT                NOT NULL,
    CONSTRAINT pk_fatura PRIMARY KEY (id),
    CONSTRAINT fk_fatura_cartao_credito FOREIGN KEY (cartao_credito_id) REFERENCES cartao_credito (id)
);


CREATE TABLE fatura_compra
(
    fatura_id BIGINT NOT NULL,
    compra_id BIGINT NOT NULL,
    PRIMARY KEY (fatura_id, compra_id),
    FOREIGN KEY (fatura_id) REFERENCES fatura (id),
    FOREIGN KEY (compra_id) REFERENCES compra (id)
);