package com.tallyto.gestorfinanceiro.core.domain.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "reserva_emergencia")
public class ReservaEmergencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "objetivo")
    private BigDecimal objetivo;
    
    @Column(name = "multiplicador_despesas")
    private Integer multiplicadorDespesas;
    
    @Column(name = "saldo_atual")
    private BigDecimal saldoAtual;
    
    @Column(name = "percentual_concluido")
    private BigDecimal percentualConcluido;
    
    @Column(name = "data_criacao")
    private LocalDate dataCriacao;
    
    @Column(name = "data_previsao_completar")
    private LocalDate dataPrevisaoCompletar;
    
    @Column(name = "valor_contribuicao_mensal")
    private BigDecimal valorContribuicaoMensal;
    
    @ManyToOne
    @JoinColumn(name = "conta_id")
    private Conta conta;
    
    // Construtor padrão
    public ReservaEmergencia() {
        this.dataCriacao = LocalDate.now();
        this.saldoAtual = BigDecimal.ZERO;
        this.percentualConcluido = BigDecimal.ZERO;
    }
}
