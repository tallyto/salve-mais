package com.tallyto.gestorfinanceiro.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CompraDTO (
        @NotBlank
        String descricao,

        @NotNull
        Double valor,

        @NotNull
        LocalDate data,

        @NotNull
        Long categoriaId,
        @NotNull
        Long cartaoId

        )
{}