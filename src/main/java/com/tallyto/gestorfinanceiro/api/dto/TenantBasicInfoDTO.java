package com.tallyto.gestorfinanceiro.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO para informações básicas do Tenant")
@Getter
@Setter
public class TenantBasicInfoDTO {
    
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 255, message = "Nome deve ter entre 2 e 255 caracteres")
    @Schema(description = "Nome do tenant", example = "Minha Empresa LTDA")
    private String name;
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter um formato válido")
    @Schema(description = "Email principal do tenant", example = "contato@minhaempresa.com")
    private String email;
    
    @Schema(description = "Número de telefone", example = "+55 11 99999-9999")
    private String phoneNumber;
    
    @Schema(description = "Endereço completo", example = "Rua das Flores, 123 - Centro - São Paulo - SP")
    private String address;
    
    @Schema(description = "Nome de exibição customizado", example = "Minha Empresa")
    private String displayName;
    
    @Schema(description = "URL do logotipo", example = "https://minhaempresa.com/logo.png")
    private String logoUrl;
    
    @Schema(description = "URL do favicon", example = "https://minhaempresa.com/favicon.ico")
    private String faviconUrl;
}