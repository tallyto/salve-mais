package com.tallyto.gestorfinanceiro.web.api.dto;

import com.tallyto.gestorfinanceiro.domain.enums.SubscriptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Status atual da assinatura do tenant")
public record BillingStatusDTO(
        @Schema(description = "Status da assinatura")
        SubscriptionStatus subscriptionStatus,
        @Schema(description = "Nome do plano")
        String nomePlano,
        @Schema(description = "Preço mensal")
        BigDecimal precoMensal,
        @Schema(description = "Fim da assinatura")
        LocalDateTime subscriptionEndDate,
        @Schema(description = "Fim do trial")
        LocalDateTime trialEndDate,
        @Schema(description = "Dias restantes do trial")
        Long diasRestantesTrial,
        @Schema(description = "Data em que a cobrança será cancelada")
        LocalDate cancelarEm,
        @Schema(description = "Quantidade de usuários ativos")
        long usuariosAtivos,
        @Schema(description = "Limite de usuários")
        Integer maxUsuarios,
        @Schema(description = "Quantidade de transações no mês")
        long transacoesMes,
        @Schema(description = "Limite de transações no mês")
        Integer maxTransacoesMes,
        @Schema(description = "Limite de armazenamento em GB")
        BigDecimal maxStorageGb
) {}
