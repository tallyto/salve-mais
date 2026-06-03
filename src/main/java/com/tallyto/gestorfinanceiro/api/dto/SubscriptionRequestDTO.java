package com.tallyto.gestorfinanceiro.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SubscriptionRequestDTO(
        @NotNull UUID planoId
) {}
