package com.tallyto.gestorfinanceiro.api.dto;

import com.tallyto.gestorfinanceiro.core.domain.entities.Usuario;
import java.time.LocalDateTime;

public class UsuarioResponseDTO {
    private Long id;
    private String email;
    private String nome;
    private LocalDateTime criadoEm;
    private LocalDateTime ultimoAcesso;

    public UsuarioResponseDTO() {}

    public UsuarioResponseDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.email = usuario.getEmail();
        this.nome = usuario.getNome();
        this.criadoEm = usuario.getCriadoEm();
        this.ultimoAcesso = usuario.getUltimoAcesso();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getUltimoAcesso() {
        return ultimoAcesso;
    }

    public void setUltimoAcesso(LocalDateTime ultimoAcesso) {
        this.ultimoAcesso = ultimoAcesso;
    }
}
