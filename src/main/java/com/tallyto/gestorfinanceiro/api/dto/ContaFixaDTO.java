package com.tallyto.gestorfinanceiro.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContaFixaDTO(
        @NotBlank(message = "O nome não pode estar em branco")
        String nome,

        @NotNull(message = "A categoria não pode ser nula")
        Long categoriaId,
        @NotNull(message = "A conta não pode ser nula")
        Long contaId,

        @NotNull(message = "O vencimento não pode ser nulo")
        LocalDate vencimento,

        @NotNull(message = "O valor não pode ser nulo")
        @Positive(message = "O valor deve ser positivo")
        BigDecimal valor,

        boolean pago
) {
}