package com.tallyto.gestorfinanceiro.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "DTO para customização de marca do Tenant")
@Getter
@Setter
public class TenantBrandingDTO {
    
    @Schema(description = "Nome de exibição customizado", example = "Minha Empresa")
    private String displayName;

    @Schema(description = "URL do logotipo")
    private String logoUrl;

    @Schema(description = "URL do favicon")
    private String faviconUrl;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Cor primária deve estar no formato hexadecimal (#RRGGBB)")
    @Schema(description = "Cor primária em hexadecimal", example = "#007bff")
    private String primaryColor;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Cor secundária deve estar no formato hexadecimal (#RRGGBB)")
    @Schema(description = "Cor secundária em hexadecimal", example = "#6c757d")
    private String secondaryColor;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Cor de destaque deve estar no formato hexadecimal (#RRGGBB)")
    @Schema(description = "Cor de destaque em hexadecimal", example = "#28a745")
    private String accentColor;
}
