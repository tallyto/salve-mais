package com.tallyto.gestorfinanceiro.api.dto;

import com.tallyto.gestorfinanceiro.core.domain.entities.Tenant.SubscriptionPlan;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "DTO para configuração de plano do Tenant")
@Getter
@Setter
public class TenantSubscriptionDTO {
    
    @NotNull
    @Schema(description = "Plano de assinatura", example = "PREMIUM")
    private SubscriptionPlan subscriptionPlan;

    @Schema(description = "Máximo de usuários permitidos", example = "50")
    private Integer maxUsers;

    @Schema(description = "Máximo de armazenamento em GB", example = "100.00")
    private BigDecimal maxStorageGb;

    @Schema(description = "Data de fim do período trial")
    private LocalDateTime trialEndDate;

    @Schema(description = "Data de início da assinatura")
    private LocalDateTime subscriptionStartDate;

    @Schema(description = "Data de fim da assinatura")
    private LocalDateTime subscriptionEndDate;

    @Schema(description = "Features habilitadas")
    private Map<String, Boolean> enabledFeatures;
}
