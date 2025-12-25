package com.tallyto.gestorfinanceiro.core.domain.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String senha;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "ultimo_acesso")
    private LocalDateTime ultimoAcesso;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "ativo")
    private Boolean ativo = true;

    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    @Column(name = "reset_password_token_expiry")
    private LocalDateTime resetPasswordTokenExpiry;

    // Getters e setters
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public LocalDateTime getUltimoAcesso() { return ultimoAcesso; }
    public void setUltimoAcesso(LocalDateTime ultimoAcesso) { this.ultimoAcesso = ultimoAcesso; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public String getResetPasswordToken() { return resetPasswordToken; }
    public void setResetPasswordToken(String resetPasswordToken) { this.resetPasswordToken = resetPasswordToken; }
    public LocalDateTime getResetPasswordTokenExpiry() { return resetPasswordTokenExpiry; }
    public void setResetPasswordTokenExpiry(LocalDateTime resetPasswordTokenExpiry) { this.resetPasswordTokenExpiry = resetPasswordTokenExpiry; }
}
