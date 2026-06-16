package com.tallyto.gestorfinanceiro.web.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para atualização do nome do usuário")
public class UsuarioAtualizacaoDTO {
    @Schema(description = "E-mail do usuário", example = "maria@empresa.com")
    private String email;

    @Schema(description = "Novo nome do usuário", example = "Maria da Silva")
    private String nome;

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
}
