package com.tallyto.gestorfinanceiro.core.domain.entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity(name = "fatura")
@Getter
@Setter
public class Fatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_vencimento")
    LocalDate dataVencimento;

    @Column(name = "data_pagamento")
    LocalDate dataPagamento;

    @Column(name = "valor_total")
    BigDecimal valorTotal;

    boolean pago;

    @ManyToOne
    @JoinColumn(name = "cartao_credito_id")
    CartaoCredito cartaoCredito;

    @ManyToMany
    @JoinTable(
            name = "fatura_compra",
            joinColumns = @JoinColumn(name = "fatura_id"),
            inverseJoinColumns = @JoinColumn(name = "compra_id")
    )
    private List<Compra> compras;


}


