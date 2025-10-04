package com.tallyto.gestorfinanceiro.api.dto;

import com.tallyto.gestorfinanceiro.core.domain.entities.Parcela;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ParcelaDTO(
        Long id,
        Integer numeroParcela,
        Integer totalParcelas,
        BigDecimal valor,
        LocalDate dataVencimento,
        boolean paga,
        Long compraParceladaId,
        String descricaoCompra
) {
    public static ParcelaDTO fromEntity(Parcela parcela) {
        return new ParcelaDTO(
                parcela.getId(),
                parcela.getNumeroParcela(),
                parcela.getTotalParcelas(),
                parcela.getValor(),
                parcela.getDataVencimento(),
                parcela.isPaga(),
                parcela.getCompraParcelada().getId(),
                parcela.getCompraParcelada().getDescricao()
        );
    }
}
