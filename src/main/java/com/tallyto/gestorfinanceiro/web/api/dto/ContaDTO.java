package com.tallyto.gestorfinanceiro.web.api.dto;

import com.tallyto.gestorfinanceiro.domain.enums.TipoConta;

import java.math.BigDecimal;

public record ContaDTO(
    Long id,
    String titular,
    BigDecimal saldo,
    TipoConta tipo,
    BigDecimal taxaRendimento,
    String descricao
) {}
