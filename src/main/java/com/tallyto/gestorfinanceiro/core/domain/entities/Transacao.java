package com.tallyto.gestorfinanceiro.core.domain.entities;

import com.tallyto.gestorfinanceiro.core.domain.enums.TipoTransacao;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "transacao")
@Getter
@Setter
public class Transacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoTransacao tipo;

    private BigDecimal valor;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    @ManyToOne
    @JoinColumn(name = "conta_id", nullable = false)
    private Conta conta;

    @ManyToOne
    @JoinColumn(name = "provento_id", nullable = false)
    private Provento provento;

}
