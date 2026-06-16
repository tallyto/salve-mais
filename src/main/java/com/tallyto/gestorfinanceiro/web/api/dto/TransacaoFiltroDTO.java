package com.tallyto.gestorfinanceiro.web.api.dto;

import java.time.LocalDateTime;

import com.tallyto.gestorfinanceiro.domain.enums.TipoTransacao;

/**
 * DTO para filtrar transações.
 */
public record TransacaoFiltroDTO(
        Long contaId,
        TipoTransacao tipo,
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        Long categoriaId,
        Long faturaId,
        Long contaFixaId,
        Long proventoId
) {}