package com.tallyto.gestorfinanceiro.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para atualizar tenant")
public class TenantUpdateDTO {
    
    @Schema(description = "Status de ativação do tenant")
    private Boolean active;
    
    @Schema(description = "Nome do tenant")
    private String name;
    
    @Schema(description = "Email do tenant")
    private String email;
    
    @Schema(description = "Telefone do tenant")
    private String phoneNumber;
    
    @Schema(description = "Endereço do tenant")
    private String address;
}
