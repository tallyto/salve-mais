CREATE TABLE reserva_emergencia (
    id SERIAL PRIMARY KEY,
    objetivo DECIMAL(15,2) NOT NULL,
    multiplicador_despesas INTEGER NOT NULL,
    saldo_atual DECIMAL(15,2) NOT NULL DEFAULT 0,
    percentual_concluido DECIMAL(5,2) NOT NULL DEFAULT 0,
    data_criacao DATE NOT NULL,
    data_previsao_completar DATE,
    valor_contribuicao_mensal DECIMAL(15,2) NOT NULL,
    conta_id BIGINT NOT NULL,
    CONSTRAINT fk_reserva_emergencia_conta FOREIGN KEY (conta_id) REFERENCES conta (id)
);
