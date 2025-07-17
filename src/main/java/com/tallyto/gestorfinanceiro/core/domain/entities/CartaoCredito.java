package com.tallyto.gestorfinanceiro.core.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "cartao_credito") 
public class CartaoCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private LocalDate vencimento;
    
    @Column(name = "limite_total")
    private BigDecimal limiteTotal;
    
    @Column(name = "limite_alerta_percentual")
    private Integer limiteAlertaPercentual = 80; // Padrão 80%
    
    @Column(name = "ativo")
    private Boolean ativo = true;
}
