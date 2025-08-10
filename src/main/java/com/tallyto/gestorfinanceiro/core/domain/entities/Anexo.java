package com.tallyto.gestorfinanceiro.core.domain.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "anexo")
public class Anexo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String tipo;
    private byte[] dados;
    
    @Column(name = "data_upload")
    private LocalDateTime dataUpload;
    
    @Column(name = "chave_s3")
    private String chaveS3;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_fixa_id")
    @JsonBackReference
    private ContaFixa contaFixa;
    
    @PrePersist
    public void prePersist() {
        this.dataUpload = LocalDateTime.now();
    }
}
