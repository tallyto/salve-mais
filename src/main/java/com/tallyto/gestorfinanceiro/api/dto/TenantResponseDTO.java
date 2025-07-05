package com.tallyto.gestorfinanceiro.api.dto;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO para representar a consulta de um Tenant")
public record TenantResponseDTO(
        @Schema(description = "ID exclusivo do tenant", example = "123e4567-e89b-12d3-a456-426614174000") String id,

        @Schema(description = "Domínio exclusivo do tenant", example = "empresa.com") String domain,

        @Schema(description = "Nome do tenant", example = "Empresa XYZ") String name,

        @Schema(description = "E-mail de contato do tenant", example = "contato@empresa.com") String email,

        @Schema(description = "Número de telefone do tenant", example = "+5511999999999") String phoneNumber,

        @Schema(description = "Endereço do tenant", example = "Rua Exemplo, 123, São Paulo - SP") String address) {

}
