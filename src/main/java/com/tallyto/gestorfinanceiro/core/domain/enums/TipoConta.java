package com.tallyto.gestorfinanceiro.core.domain.enums;

public enum TipoConta {
    CORRENTE("Conta Corrente"),
    POUPANCA("Poupança"),
    INVESTIMENTO("Investimento"),
    RESERVA_EMERGENCIA("Reserva de Emergência");

    private final String descricao;

    TipoConta(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
