package com.tallyto.gestorfinanceiro.core.domain.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.tallyto.gestorfinanceiro.core.domain.enums.TipoTransacao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidade que representa uma transação financeira no sistema.
 * As transações são imutáveis após criadas.
 */
@Entity(name = "transacao")
@Getter
@Setter
public class Transacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoTransacao tipo;

    @Column(name = "valor", nullable = false)
    private BigDecimal valor;

    @Column(name = "data", nullable = false)
    private LocalDateTime data;
    
    @Column(name = "descricao")
    private String descricao;

    @ManyToOne
    @JoinColumn(name = "conta_id", nullable = false)
    private Conta conta;

    @ManyToOne
    @JoinColumn(name = "conta_destino_id")
    private Conta contaDestino;
    
    @ManyToOne
    @JoinColumn(name = "fatura_id")
    private Fatura fatura;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;
    
    @ManyToOne
    @JoinColumn(name = "provento_id")
    private Provento provento;
    
    @ManyToOne
    @JoinColumn(name = "conta_fixa_id")
    private ContaFixa contaFixa;
    
    @Column(name = "observacoes")
    private String observacoes;
    
    /**
     * Indica se a transação é do sistema, como ajustes automáticos,
     * ou criada por usuário.
     */
    @Column(name = "sistema")
    private boolean sistema = false;
}
