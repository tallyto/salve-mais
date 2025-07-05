package com.tallyto.gestorfinanceiro.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CompraDTO (
        Long id,
        
        @NotBlank
        String descricao,

        @NotNull
        BigDecimal valor,

        @NotNull
        LocalDate data,

        @NotNull
        Long categoriaId,
        
        @NotNull
        Long cartaoId
)
{}