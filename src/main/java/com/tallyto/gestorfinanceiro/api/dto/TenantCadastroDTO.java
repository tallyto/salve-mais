package com.tallyto.gestorfinanceiro.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record TenantCadastroDTO(
        @NotBlank(message = "Nome é obrigatório")
        String name,
        
        @NotBlank(message = "Domínio é obrigatório")
        String domain,
        
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        String email
) {
}
