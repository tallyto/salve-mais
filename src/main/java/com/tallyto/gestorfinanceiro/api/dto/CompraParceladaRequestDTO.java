package com.tallyto.gestorfinanceiro.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CompraParceladaRequestDTO(
        @NotBlank(message = "Descrição é obrigatória")
        String descricao,

        @NotNull(message = "Valor total é obrigatório")
        @Min(value = 0, message = "Valor deve ser positivo")
        BigDecimal valorTotal,

        @NotNull(message = "Data da compra é obrigatória")
        LocalDate dataCompra,

        @NotNull(message = "Parcela inicial é obrigatória")
        @Min(value = 1, message = "Parcela inicial deve ser no mínimo 1")
        Integer parcelaInicial,

        @NotNull(message = "Total de parcelas é obrigatório")
        @Min(value = 1, message = "Total de parcelas deve ser no mínimo 1")
        Integer totalParcelas,

        @NotNull(message = "Categoria é obrigatória")
        Long categoriaId,

        @NotNull(message = "Cartão de crédito é obrigatório")
        Long cartaoId
) {
    public CompraParceladaRequestDTO {
        if (parcelaInicial != null && totalParcelas != null && parcelaInicial > totalParcelas) {
            throw new IllegalArgumentException("Parcela inicial não pode ser maior que o total de parcelas");
        }
    }
}
