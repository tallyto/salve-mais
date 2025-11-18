package com.tallyto.gestorfinanceiro.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "DTO para configuração SMTP customizada do Tenant")
@Getter
@Setter
public class TenantSmtpConfigDTO {
    
    @NotBlank
    @Schema(description = "Host SMTP", example = "smtp.gmail.com")
    private String host;

    @Schema(description = "Porta SMTP", example = "587")
    private Integer port;

    @NotBlank
    @Schema(description = "Usuário SMTP", example = "usuario@empresa.com")
    private String user;

    @NotBlank
    @Schema(description = "Senha SMTP")
    private String password;

    @NotBlank
    @Email
    @Schema(description = "Email de origem", example = "noreply@empresa.com")
    private String fromEmail;

    @Schema(description = "Nome de origem", example = "Minha Empresa")
    private String fromName;
}
