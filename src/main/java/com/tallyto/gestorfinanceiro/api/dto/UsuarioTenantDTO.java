package com.tallyto.gestorfinanceiro.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para usuário associado a um tenant")
public class UsuarioTenantDTO {
    
    @Schema(description = "ID do usuário")
    private Long id;
    
    @Schema(description = "Email do usuário")
    private String email;
    
    @Schema(description = "Nome do usuário")
    private String nome;
    
    @Schema(description = "ID do tenant")
    private UUID tenantId;
    
    @Schema(description = "Data de criação")
    private LocalDateTime criadoEm;
    
    @Schema(description = "Data do último acesso")
    private LocalDateTime ultimoAcesso;
}
