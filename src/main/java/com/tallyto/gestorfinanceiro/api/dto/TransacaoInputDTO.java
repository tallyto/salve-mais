package com.tallyto.gestorfinanceiro.api.dto;

import com.tallyto.gestorfinanceiro.core.domain.enums.TipoTransacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO para criar uma nova transação.
 */
public record TransacaoInputDTO(
        @NotNull(message = "O tipo de transação é obrigatório")
        TipoTransacao tipo,
        
        @NotNull(message = "O valor é obrigatório")
        @Positive(message = "O valor deve ser maior que zero")
        BigDecimal valor,
        
        String descricao,
        
        @NotNull(message = "O ID da conta é obrigatório")
        Long contaId,
        
        Long contaDestinoId,
        
        Long faturaId,
        
        Long categoriaId,
        
        Long proventoId,
        
        Long contaFixaId,
                
        String observacoes
) {
    /**
     * Valida se os campos obrigatórios para cada tipo de transação estão presentes.
     */
    public void validar() {
        switch (tipo) {
            case CREDITO:
                validarCredito();
                break;
            case DEBITO:
                validarDebito();
                break;
            case TRANSFERENCIA_SAIDA:
            case TRANSFERENCIA_ENTRADA:
                validarTransferencia();
                break;
            case PAGAMENTO_FATURA:
                validarPagamentoFatura();
                break;
        }
    }
    
    private void validarCredito() {
        if (categoriaId == null && proventoId == null) {
            throw new IllegalArgumentException("Para uma transação de crédito, é necessário informar a categoria ou o provento");
        }
    }
    
    private void validarDebito() {
        if (categoriaId == null && contaFixaId == null) {
            throw new IllegalArgumentException("Para uma transação de débito, é necessário informar a categoria ou a conta fixa");
        }
    }
    
    private void validarTransferencia() {
        if (contaDestinoId == null) {
            throw new IllegalArgumentException("Para uma transferência, é necessário informar a conta de destino");
        }
        if (contaDestinoId.equals(contaId)) {
            throw new IllegalArgumentException("A conta de destino deve ser diferente da conta de origem");
        }
    }
    
    private void validarPagamentoFatura() {
        if (faturaId == null) {
            throw new IllegalArgumentException("Para um pagamento de fatura, é necessário informar a fatura");
        }
    }
}