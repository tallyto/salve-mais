package com.tallyto.gestorfinanceiro.api.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanoCompraDTO {

    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String descricao;

    @NotNull(message = "Valor total é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor total deve ser maior que zero")
    private BigDecimal valorTotal;

    @DecimalMin(value = "0.00", message = "Valor economizado não pode ser negativo")
    private BigDecimal valorEconomizado;

    @DecimalMin(value = "0.00", message = "Valor de entrada não pode ser negativo")
    private BigDecimal valorEntrada;

    @Min(value = 1, message = "Número de parcelas deve ser maior que zero")
    private Integer numeroParcelas;

    @DecimalMin(value = "0.00", message = "Taxa de juros não pode ser negativa")
    @DecimalMax(value = "100.00", message = "Taxa de juros não pode ser maior que 100%")
    private BigDecimal taxaJuros;

    private BigDecimal valorParcela;

    @NotNull(message = "Tipo de compra é obrigatório")
    private String tipoCompra;

    private LocalDate dataPrevista;

    @Min(value = 1, message = "Prioridade deve ser 1, 2 ou 3")
    @Max(value = 3, message = "Prioridade deve ser 1, 2 ou 3")
    private Integer prioridade;

    private String status;

    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    private String observacoes;

    // Campos calculados
    private BigDecimal valorFinal;

    private BigDecimal jurosTotal;

    private BigDecimal percentualEconomizado;
}
