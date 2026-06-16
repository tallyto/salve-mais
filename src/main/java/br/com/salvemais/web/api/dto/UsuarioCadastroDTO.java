package br.com.salvemais.web.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para cadastro de usuário")
public class UsuarioCadastroDTO {
    @Schema(description = "Nome do usuário", example = "Maria Silva")
    private String nome;

    @Schema(description = "E-mail do usuário", example = "maria@empresa.com")
    private String email;

    @Schema(description = "Senha inicial", example = "SenhaForte123")
    private String senha;

    // Getters e setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}
