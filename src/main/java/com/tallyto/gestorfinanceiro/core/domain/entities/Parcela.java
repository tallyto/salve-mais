package com.tallyto.gestorfinanceiro.core.domain.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "parcela")
public class Parcela {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_parcela")
    private Integer numeroParcela;
    
    @Column(name = "total_parcelas")
    private Integer totalParcelas;
    
    private BigDecimal valor;
    
    @Column(name = "data_vencimento")
    private LocalDate dataVencimento;
    
    private boolean paga = false;

    @ManyToOne
    @JoinColumn(name = "compra_parcelada_id", nullable = false)
    private CompraParcelada compraParcelada;
}
