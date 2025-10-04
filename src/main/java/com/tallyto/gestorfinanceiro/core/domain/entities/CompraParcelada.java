package com.tallyto.gestorfinanceiro.core.domain.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "compra_parcelada")
public class CompraParcelada {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descricao;
    
    @Column(name = "valor_total")
    private BigDecimal valorTotal;
    
    @Column(name = "data_compra")
    private LocalDate dataCompra;
    
    @Column(name = "parcela_inicial")
    private Integer parcelaInicial;
    
    @Column(name = "total_parcelas")
    private Integer totalParcelas;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "cartao_credito_id")
    private CartaoCredito cartaoCredito;

    @OneToMany(mappedBy = "compraParcelada", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Parcela> parcelas = new ArrayList<>();
}
