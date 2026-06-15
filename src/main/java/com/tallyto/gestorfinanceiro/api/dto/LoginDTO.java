package com.tallyto.gestorfinanceiro.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Credenciais de autenticação do usuário")
public class LoginDTO {
    @Schema(description = "E-mail de acesso", example = "usuario@empresa.com")
    private String email;

    @Schema(description = "Senha de acesso", example = "senhaSegura123")
    private String senha;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}
