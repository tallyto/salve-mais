package br.com.salvemais.domain.entities;

import br.com.salvemais.domain.enums.TipoConta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "conta")
public class Conta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "saldo")
    private BigDecimal saldo;

    @Column(name = "titular")
    private String titular;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo")
    private TipoConta tipo = TipoConta.CORRENTE; // Valor padrão
    
    @Column(name = "taxa_rendimento")
    private BigDecimal taxaRendimento; // Taxa de rendimento anual em percentual (ex: 13.25 para 13.25%)
    
    @Column(name = "descricao")
    private String descricao;
}
