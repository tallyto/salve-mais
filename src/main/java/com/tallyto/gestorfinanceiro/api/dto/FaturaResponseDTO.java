package com.tallyto.gestorfinanceiro.api.dto;

import com.tallyto.gestorfinanceiro.core.domain.entities.Fatura;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FaturaResponseDTO(
        Long id,
        Long cartaoCreditoId,
        String nomeCartao,
        BigDecimal valorTotal,
        LocalDate dataVencimento,
        LocalDate dataPagamento,
        boolean pago,
        Long contaPagamentoId,
        String nomeContaPagamento,
        int totalCompras
) {
    public static FaturaResponseDTO fromEntity(Fatura fatura) {
        return new FaturaResponseDTO(
                fatura.getId(),
                fatura.getCartaoCredito().getId(),
                fatura.getCartaoCredito().getNome(),
                fatura.getValorTotal(),
                fatura.getDataVencimento(),
                fatura.getDataPagamento(),
                fatura.isPago(),
                fatura.getContaPagamento() != null ? fatura.getContaPagamento().getId() : null,
                fatura.getContaPagamento() != null ? fatura.getContaPagamento().getTitular() : null,
                fatura.getCompras() != null ? fatura.getCompras().size() : 0
        );
    }
}
