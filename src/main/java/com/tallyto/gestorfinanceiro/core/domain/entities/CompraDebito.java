package com.tallyto.gestorfinanceiro.core.domain.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@Table(name = "compra_debito")
public class CompraDebito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "conta_id")
    private Conta conta;

    @Column(name = "data_compra")
    private LocalDate dataCompra;
    
    private BigDecimal valor;

    private String observacoes;
    
    @OneToMany(mappedBy = "compraDebito", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Anexo> anexos = new ArrayList<>();
    
    public void adicionarAnexo(Anexo anexo) {
        anexos.add(anexo);
        anexo.setCompraDebito(this);
    }
    
    public void removerAnexo(Anexo anexo) {
        anexos.remove(anexo);
        anexo.setCompraDebito(null);
    }
}
