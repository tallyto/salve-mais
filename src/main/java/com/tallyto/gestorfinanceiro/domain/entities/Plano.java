package com.tallyto.gestorfinanceiro.domain.entities;

import com.tallyto.gestorfinanceiro.domain.entities.Tenant.SubscriptionPlan;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "planos", schema = "public")
@Getter
@Setter
public class Plano {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, unique = true)
    private SubscriptionPlan tipo;

    @Column(name = "preco_mensal", nullable = false)
    private BigDecimal precoMensal;

    @Column(name = "max_usuarios", nullable = false)
    private Integer maxUsuarios;

    @Column(name = "max_transacoes_mes")
    private Integer maxTransacoesMes;

    @Column(name = "max_storage_gb")
    private BigDecimal maxStorageGb;

    @Column(name = "stripe_price_id")
    private String stripePriceId;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;
}
