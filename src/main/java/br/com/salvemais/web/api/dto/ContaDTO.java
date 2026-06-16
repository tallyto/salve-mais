package br.com.salvemais.web.api.dto;

import br.com.salvemais.domain.enums.TipoConta;

import java.math.BigDecimal;

public record ContaDTO(
    Long id,
    String titular,
    BigDecimal saldo,
    TipoConta tipo,
    BigDecimal taxaRendimento,
    String descricao
) {}
