package com.tallyto.gestorfinanceiro.core.domain.enums;

/**
 * Enum que representa os tipos de transações realizadas no sistema.
 */
public enum TipoTransacao {
    CREDITO("Crédito", "Entrada de valores na conta"),
    DEBITO("Débito", "Saída de valores da conta"),
    TRANSFERENCIA_SAIDA("Transferência Enviada", "Transferência para outra conta"),
    TRANSFERENCIA_ENTRADA("Transferência Recebida", "Transferência de outra conta"),
    PAGAMENTO_FATURA("Pagamento de Fatura", "Pagamento de fatura de cartão de crédito");
    
    private final String nome;
    private final String descricao;
    
    TipoTransacao(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }
    
    public String getNome() {
        return nome;
    }
    
    public String getDescricao() {
        return descricao;
    }
}