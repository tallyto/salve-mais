package br.com.salvemais.web.api.dto;

import java.math.BigDecimal;

public record CartaoLimiteDTO(
    Long cartaoId,
    BigDecimal limiteTotal,
    Integer limiteAlertaPercentual
) {}
