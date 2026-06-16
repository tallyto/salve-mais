package br.com.salvemais.web.api.dto;

import java.time.LocalDateTime;

import br.com.salvemais.domain.enums.TipoTransacao;

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