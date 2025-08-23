package com.tallyto.gestorfinanceiro.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ContribuicaoReservaDTO(
    @NotNull(message = "O ID da conta de origem é obrigatório")
    Long contaOrigemId,
    
    @NotNull(message = "O valor da contribuição é obrigatório")
    @Positive(message = "O valor da contribuição deve ser positivo")
    BigDecimal valor
) {
}
