package com.tallyto.gestorfinanceiro.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para preview da fatura antes de criá-la
 * Mostra quais compras e parcelas seriam incluídas
 */
public record FaturaPreviewDTO(
        Long cartaoCreditoId,
        String nomeCartao,
        LocalDate dataVencimento,
        List<CompraDTO> compras,
        List<ParcelaDTO> parcelas,
        BigDecimal valorCompras,
        BigDecimal valorParcelas,
        BigDecimal valorTotal
) {
    /**
     * Classe interna para representar compras no preview
     */
    public record CompraDTO(
            Long id,
            String descricao,
            BigDecimal valor,
            LocalDate data,
            String categoria
    ) {}
}
