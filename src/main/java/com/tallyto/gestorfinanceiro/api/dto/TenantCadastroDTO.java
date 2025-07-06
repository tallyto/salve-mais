package com.tallyto.gestorfinanceiro.api.dto;

public record TenantCadastroDTO(
        String name,
        String domain,
        String email
) {
}
