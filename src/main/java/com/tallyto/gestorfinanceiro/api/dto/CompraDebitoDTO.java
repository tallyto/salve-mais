package com.tallyto.gestorfinanceiro.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CompraDebitoDTO(
        @NotBlank(message = "O nome não pode estar em branco")
        String nome,

        Long categoriaId,
        
        @NotNull(message = "A conta não pode ser nula")
        Long contaId,

        @NotNull(message = "A data da compra não pode ser nula")
        LocalDate dataCompra,

        @NotNull(message = "O valor não pode ser nulo")
        @Positive(message = "O valor deve ser positivo")
        BigDecimal valor,

        String observacoes
) {
}
