package com.tallyto.gestorfinanceiro.web.api.dto;

import java.math.BigDecimal;

public record CartaoLimiteDTO(
    Long cartaoId,
    BigDecimal limiteTotal,
    Integer limiteAlertaPercentual
) {}
