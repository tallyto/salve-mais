package com.tallyto.gestorfinanceiro.api.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetaDTO {

    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String descricao;

    @NotNull(message = "Valor alvo é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor alvo deve ser maior que zero")
    private BigDecimal valorAlvo;

    @DecimalMin(value = "0.00", message = "Valor atual não pode ser negativo")
    private BigDecimal valorAtual;

    @NotNull(message = "Data de início é obrigatória")
    private LocalDate dataInicio;

    @NotNull(message = "Data alvo é obrigatória")
    private LocalDate dataAlvo;

    private Long categoriaId;

    private String categoriaNome;

    private String status;

    private BigDecimal valorMensalSugerido;

    private String icone;

    private String cor;

    private Boolean notificarProgresso;

    // Campos calculados
    private BigDecimal percentualConcluido;

    private BigDecimal valorRestante;

    private Long diasRestantes;
}
