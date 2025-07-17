package com.tallyto.gestorfinanceiro.api.dto;

import java.math.BigDecimal;

public record CartaoLimiteStatusDTO(
    Long cartaoId,
    String nomeCartao,
    BigDecimal limiteTotal,
    BigDecimal valorUtilizado,
    BigDecimal limiteDisponivel,
    BigDecimal percentualUtilizado,
    Boolean limiteExcedido,
    Boolean alertaAtivado,
    Integer limiteAlertaPercentual
) {}
