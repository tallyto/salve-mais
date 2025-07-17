package com.tallyto.gestorfinanceiro.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContaFixaRecorrenteDTO(
        @NotBlank(message = "O nome não pode estar em branco")
        String nome,

        @NotNull(message = "A categoria não pode ser nula")
        Long categoriaId,
        
        @NotNull(message = "A conta não pode ser nula")
        Long contaId,

        @NotNull(message = "A data de início não pode ser nula")
        LocalDate dataInicio,

        @NotNull(message = "O valor não pode ser nulo")
        @Positive(message = "O valor deve ser positivo")
        BigDecimal valor,

        @NotNull(message = "O número de parcelas não pode ser nulo")
        @Min(value = 1, message = "O número de parcelas deve ser pelo menos 1")
        Integer numeroParcelas,

        @NotNull(message = "O tipo de recorrência é obrigatório")
        TipoRecorrencia tipoRecorrencia,

        String observacoes
) {
    public enum TipoRecorrencia {
        MENSAL("Mensal"),
        BIMENSAL("Bimensal"),
        TRIMESTRAL("Trimestral"),
        SEMESTRAL("Semestral"),
        ANUAL("Anual");

        private final String descricao;

        TipoRecorrencia(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }

        public int getMeses() {
            return switch (this) {
                case MENSAL -> 1;
                case BIMENSAL -> 2;
                case TRIMESTRAL -> 3;
                case SEMESTRAL -> 6;
                case ANUAL -> 12;
            };
        }
    }
}
