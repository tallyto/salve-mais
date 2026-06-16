package br.com.salvemais.web.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para atualização de senha do usuário")
public class UsuarioSenhaDTO {
    @Schema(description = "E-mail do usuário", example = "maria@empresa.com")
    private String email;

    @Schema(description = "Senha atual", example = "SenhaAtual123")
    private String senhaAtual;

    @Schema(description = "Nova senha", example = "NovaSenha123")
    private String novaSenha;

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getSenhaAtual() {
        return senhaAtual;
    }
    public void setSenhaAtual(String senhaAtual) {
        this.senhaAtual = senhaAtual;
    }
    public String getNovaSenha() {
        return novaSenha;
    }
    public void setNovaSenha(String novaSenha) {
        this.novaSenha = novaSenha;
    }
}
