package com.tallyto.gestorfinanceiro.api.dto;


import com.tallyto.gestorfinanceiro.core.domain.entities.Categoria.TipoCategoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoriaDTO(
        Long id,
        
        @NotBlank(message = "O nome da categoria não pode estar em branco")
        String nome,
        
        @NotNull(message = "O tipo da categoria não pode ser nulo")
        TipoCategoria tipo
) {
}