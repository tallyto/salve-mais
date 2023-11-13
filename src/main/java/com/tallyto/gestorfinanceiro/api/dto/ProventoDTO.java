package com.tallyto.gestorfinanceiro.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProventoDTO(
        @NotBlank(message = "A descrição não pode estar em branco")
        String descricao,

        @NotNull(message = "O valor não pode ser nulo")
        @Positive(message = "O valor deve ser positivo")
        BigDecimal valor,

        @NotNull(message = "A data não pode ser nula")
        LocalDate data
) {
}
