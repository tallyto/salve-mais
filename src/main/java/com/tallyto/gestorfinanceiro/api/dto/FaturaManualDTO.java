package com.tallyto.gestorfinanceiro.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FaturaManualDTO(
        @NotNull(message = "ID do cartão é obrigatório")
        Long cartaoCreditoId,
        
        @NotNull(message = "Valor total é obrigatório")
        @Positive(message = "Valor total deve ser positivo")
        BigDecimal valorTotal,
        
        @NotNull(message = "Data de vencimento é obrigatória")
        LocalDate dataVencimento
) {}
