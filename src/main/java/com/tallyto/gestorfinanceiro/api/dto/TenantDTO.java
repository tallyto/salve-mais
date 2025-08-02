package com.tallyto.gestorfinanceiro.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "DTO para representar um Tenant")
@Getter
@Setter
public class TenantDTO {
        @NotBlank
        @Schema(description = "Domínio exclusivo do tenant", example = "empresa.com")
        private String domain;

        @NotBlank
        @Schema(description = "Nome do tenant", example = "Empresa XYZ")
        private String name;

        @NotBlank
        @Email
        @Schema(description = "E-mail de contato do tenant", example = "contato@empresa.com")
        private String email;

        @Size(max = 15)
        @Schema(description = "Número de telefone do tenant", example = "+5511999999999")
        private String phoneNumber;

        @Schema(description = "Endereço do tenant", example = "Rua Exemplo, 123, São Paulo - SP")
        private String address;

}
