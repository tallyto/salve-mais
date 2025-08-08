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
@Table(name = "conta_fixa") // Specify the table name if needed
public class ContaFixa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "conta_id")
    private Conta conta;

    private LocalDate vencimento;
    private BigDecimal valor;

    private boolean pago;
    
    @OneToMany(mappedBy = "contaFixa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Anexo> anexos = new ArrayList<>();
    
    public void adicionarAnexo(Anexo anexo) {
        anexos.add(anexo);
        anexo.setContaFixa(this);
    }
    
    public void removerAnexo(Anexo anexo) {
        anexos.remove(anexo);
        anexo.setContaFixa(null);
    }
}