package com.tallyto.gestorfinanceiro.api.dto;

import com.tallyto.gestorfinanceiro.core.domain.enums.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BillingStatusDTO(
        SubscriptionStatus subscriptionStatus,
        String nomePlano,
        BigDecimal precoMensal,
        LocalDateTime subscriptionEndDate,
        LocalDateTime trialEndDate,
        Long diasRestantesTrial,
        LocalDate cancelarEm,
        long usuariosAtivos,
        Integer maxUsuarios,
        long transacoesMes,
        Integer maxTransacoesMes,
        BigDecimal maxStorageGb
) {}
