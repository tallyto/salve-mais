package com.tallyto.gestorfinanceiro.api.dto;

import com.tallyto.gestorfinanceiro.core.domain.enums.TipoTransacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para retornar os dados de uma transação.
 */
public record TransacaoDTO(
        Long id,
        TipoTransacao tipo,
        BigDecimal valor,
        LocalDateTime data,
        String descricao,
        ContaResumoDTO conta,
        ContaResumoDTO contaDestino,
        FaturaResumoDTO fatura,
        CategoriaResumoDTO categoria,
        ProventoResumoDTO provento,
        ContaFixaResumoDTO contaFixa,
        String observacoes
) {
    /**
     * DTO resumido para conta
     */
    public record ContaResumoDTO(
            Long id,
            String titular
    ) {}
    
    /**
     * DTO resumido para fatura
     */
    public record FaturaResumoDTO(
            Long id,
            String cartao,
            LocalDateTime dataVencimento,
            BigDecimal valorTotal
    ) {}
    
    /**
     * DTO resumido para categoria
     */
    public record CategoriaResumoDTO(
            Long id,
            String nome
    ) {}
    
    /**
     * DTO resumido para provento
     */
    public record ProventoResumoDTO(
            Long id,
            String descricao,
            BigDecimal valor
    ) {}
    
    /**
     * DTO resumido para conta fixa
     */
    public record ContaFixaResumoDTO(
            Long id,
            String nome,
            BigDecimal valor
    ) {}
}