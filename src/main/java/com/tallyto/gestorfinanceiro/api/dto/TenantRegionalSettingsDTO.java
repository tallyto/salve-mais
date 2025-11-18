package com.tallyto.gestorfinanceiro.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "DTO para configurações regionais do Tenant")
@Getter
@Setter
public class TenantRegionalSettingsDTO {
    
    @NotBlank
    @Schema(description = "Timezone", example = "America/Sao_Paulo")
    private String timezone;

    @NotBlank
    @Schema(description = "Locale", example = "pt_BR")
    private String locale;

    @NotBlank
    @Schema(description = "Código da moeda", example = "BRL")
    private String currencyCode;

    @Schema(description = "Formato de data", example = "dd/MM/yyyy")
    private String dateFormat;
}
