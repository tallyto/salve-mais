package com.tallyto.gestorfinanceiro.api.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanoAposentadoriaDTO {

    private Long id;

    @NotNull(message = "Idade atual é obrigatória")
    @Min(value = 18, message = "Idade atual deve ser no mínimo 18 anos")
    @Max(value = 100, message = "Idade atual deve ser no máximo 100 anos")
    private Integer idadeAtual;

    @NotNull(message = "Idade de aposentadoria é obrigatória")
    @Min(value = 50, message = "Idade de aposentadoria deve ser no mínimo 50 anos")
    @Max(value = 100, message = "Idade de aposentadoria deve ser no máximo 100 anos")
    private Integer idadeAposentadoria;

    @NotNull(message = "Renda desejada é obrigatória")
    @DecimalMin(value = "0.01", message = "Renda desejada deve ser maior que zero")
    private BigDecimal rendaDesejada;

    @DecimalMin(value = "0.00", message = "Patrimônio atual não pode ser negativo")
    private BigDecimal patrimonioAtual;

    @DecimalMin(value = "0.00", message = "Contribuição mensal não pode ser negativa")
    private BigDecimal contribuicaoMensal;

    @NotNull(message = "Taxa de retorno anual é obrigatória")
    @DecimalMin(value = "0.00", message = "Taxa de retorno não pode ser negativa")
    @DecimalMax(value = "50.00", message = "Taxa de retorno não pode ser maior que 50%")
    private BigDecimal taxaRetornoAnual;

    @Min(value = 60, message = "Expectativa de vida deve ser no mínimo 60 anos")
    @Max(value = 120, message = "Expectativa de vida deve ser no máximo 120 anos")
    private Integer expectativaVida;

    private BigDecimal patrimonioNecessario;

    private BigDecimal patrimonioProjetado;

    private Boolean ativo;

    // Campos calculados
    private BigDecimal deficitOuSuperavit;

    private BigDecimal contribuicaoMensalNecessaria;

    private String status; // NO_CAMINHO_CERTO, ATENCAO, AJUSTE_NECESSARIO

    private Integer anosAteAposentadoria;

    private Integer anosAposAposentadoria;
}
