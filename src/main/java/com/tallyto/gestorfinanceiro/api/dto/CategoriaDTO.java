package com.tallyto.gestorfinanceiro.api.dto;


import jakarta.validation.constraints.NotBlank;

public record CategoriaDTO(
        @NotBlank(message = "O nome da categoria não pode estar em branco")
        String nome
) {
}