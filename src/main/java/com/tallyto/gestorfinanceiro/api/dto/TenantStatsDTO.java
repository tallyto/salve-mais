package com.tallyto.gestorfinanceiro.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO com estatísticas dos tenants")
public class TenantStatsDTO {
    
    @Schema(description = "Total de tenants cadastrados")
    private Long totalTenants;
    
    @Schema(description = "Total de tenants ativos")
    private Long activeTenants;
    
    @Schema(description = "Total de tenants inativos")
    private Long inactiveTenants;
    
    @Schema(description = "Total de usuários em todos os tenants")
    private Long totalUsers;
}
