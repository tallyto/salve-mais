package com.tallyto.gestorfinanceiro.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO para representar um Tenant")
public record TenantDTO(
        @NotBlank @Schema(description = "Domínio exclusivo do tenant", example = "empresa.com") String domain,

        @NotBlank @Schema(description = "Nome do tenant", example = "Empresa XYZ") String name,

        @NotBlank @Email @Schema(description = "E-mail de contato do tenant", example = "contato@empresa.com") String email,

        @Size(max = 15) @Schema(description = "Número de telefone do tenant", example = "+5511999999999") String phoneNumber,

        @Schema(description = "Endereço do tenant", example = "Rua Exemplo, 123, São Paulo - SP") String address) {
}
