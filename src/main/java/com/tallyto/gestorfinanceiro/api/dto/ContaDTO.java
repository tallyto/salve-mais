package com.tallyto.gestorfinanceiro.api.dto;

import com.tallyto.gestorfinanceiro.core.domain.enums.TipoConta;

import java.math.BigDecimal;

public record ContaDTO(
    Long id,
    String titular,
    BigDecimal saldo,
    TipoConta tipo,
    BigDecimal taxaRendimento,
    String descricao
) {}
