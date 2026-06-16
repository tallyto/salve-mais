package com.tallyto.gestorfinanceiro.web.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Plano de assinatura disponível")
public record PlanoDTO(
        @Schema(description = "ID do plano")
        UUID id,
        @Schema(description = "Nome do plano")
        String nome,
        @Schema(description = "Descrição do plano")
        String descricao,
        @Schema(description = "Tipo do plano")
        String tipo,
        @Schema(description = "Preço mensal")
        BigDecimal precoMensal,
        @Schema(description = "Máximo de usuários")
        Integer maxUsuarios,
        @Schema(description = "Máximo de transações por mês")
        Integer maxTransacoesMes,
        @Schema(description = "Limite de armazenamento em GB")
        BigDecimal maxStorageGb
) {}
