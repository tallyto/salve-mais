package com.tallyto.gestorfinanceiro.api.dto;


import com.tallyto.gestorfinanceiro.core.domain.entities.Tenant.SubscriptionPlan;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "DTO para representar a consulta de um Tenant")
public record TenantResponseDTO(
        @Schema(description = "ID exclusivo do tenant", example = "123e4567-e89b-12d3-a456-426614174000") String id,

        @Schema(description = "Domínio exclusivo do tenant", example = "empresa.com") String domain,

        @Schema(description = "Nome do tenant", example = "Empresa XYZ") String name,

        @Schema(description = "E-mail de contato do tenant", example = "contato@empresa.com") String email,

        @Schema(description = "Número de telefone do tenant", example = "+5511999999999") String phoneNumber,

        @Schema(description = "Endereço do tenant", example = "Rua Exemplo, 123, São Paulo - SP") String address,

        // Customização de Marca
        @Schema(description = "Nome de exibição customizado") String displayName,
        @Schema(description = "URL do logotipo") String logoUrl,
        @Schema(description = "URL do favicon") String faviconUrl,

        // Configurações de Plano
        @Schema(description = "Plano de assinatura") SubscriptionPlan subscriptionPlan,
        @Schema(description = "Máximo de usuários") Integer maxUsers,
        @Schema(description = "Máximo de armazenamento em GB") BigDecimal maxStorageGb,
        @Schema(description = "Data de fim do trial") LocalDateTime trialEndDate,
        @Schema(description = "Data de início da assinatura") LocalDateTime subscriptionStartDate,
        @Schema(description = "Data de fim da assinatura") LocalDateTime subscriptionEndDate,
        @Schema(description = "Features habilitadas") Map<String, Boolean> enabledFeatures,

        // Configurações Regionais
        @Schema(description = "Timezone") String timezone,
        @Schema(description = "Locale") String locale,
        @Schema(description = "Código da moeda") String currencyCode,
        @Schema(description = "Formato de data") String dateFormat,

        @Schema(description = "Status ativo") Boolean active
) {

}
