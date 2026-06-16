package com.tallyto.gestorfinanceiro.web.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Requisição para assinatura de um plano")
public record SubscriptionRequestDTO(
        @Schema(description = "ID do plano escolhido", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull UUID planoId
) {}
