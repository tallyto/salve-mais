package com.tallyto.gestorfinanceiro.api.dto;

import java.math.BigDecimal;

public record VariationDataDTO(
    String metric,
    BigDecimal currentValue,
    BigDecimal previousValue,
    BigDecimal variation,
    BigDecimal variationPercent,
    String trend,
    String icon
) {}
