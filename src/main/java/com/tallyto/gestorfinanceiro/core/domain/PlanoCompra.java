package com.tallyto.gestorfinanceiro.core.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "planos_compra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanoCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "valor_economizado", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal valorEconomizado = BigDecimal.ZERO;

    @Column(name = "valor_entrada", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal valorEntrada = BigDecimal.ZERO;

    @Column(name = "numero_parcelas")
    private Integer numeroParcelas;

    @Column(name = "taxa_juros", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxaJuros = BigDecimal.ZERO;

    @Column(name = "valor_parcela", precision = 15, scale = 2)
    private BigDecimal valorParcela;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_compra", nullable = false, length = 20)
    private TipoCompra tipoCompra;

    @Column(name = "data_prevista")
    private LocalDate dataPrevista;

    @Column(name = "prioridade")
    @Builder.Default
    private Integer prioridade = 3; // 1=Alta, 2=Média, 3=Baixa

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusPlano status = StatusPlano.PLANEJADO;

    @Column(length = 500)
    private String observacoes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum TipoCompra {
        A_VISTA,
        PARCELADO_SEM_JUROS,
        PARCELADO_COM_JUROS,
        FINANCIAMENTO
    }

    public enum StatusPlano {
        PLANEJADO,
        EM_ANDAMENTO,
        CONCLUIDO,
        CANCELADO
    }

    public BigDecimal calcularValorFinal() {
        if (tipoCompra == TipoCompra.A_VISTA) {
            return valorTotal;
        }
        
        if (valorParcela != null && numeroParcelas != null) {
            return valorParcela.multiply(BigDecimal.valueOf(numeroParcelas)).add(valorEntrada);
        }
        
        return valorTotal;
    }

    public BigDecimal calcularJurosTotal() {
        return calcularValorFinal().subtract(valorTotal);
    }

    public BigDecimal calcularPercentualEconomizado() {
        if (valorTotal.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return valorEconomizado
            .divide(valorTotal, 4, java.math.RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }

    public void calcularParcela() {
        if (numeroParcelas == null || numeroParcelas <= 0) {
            this.valorParcela = BigDecimal.ZERO;
            return;
        }

        BigDecimal valorFinanciar = valorTotal.subtract(valorEntrada);
        
        if (taxaJuros.compareTo(BigDecimal.ZERO) == 0) {
            // Sem juros
            this.valorParcela = valorFinanciar.divide(
                BigDecimal.valueOf(numeroParcelas), 
                2, 
                java.math.RoundingMode.HALF_UP
            );
        } else {
            // Com juros - Fórmula Price
            BigDecimal taxaMensal = taxaJuros.divide(BigDecimal.valueOf(100), 6, java.math.RoundingMode.HALF_UP);
            BigDecimal fator = BigDecimal.ONE.add(taxaMensal).pow(numeroParcelas);
            this.valorParcela = valorFinanciar.multiply(
                taxaMensal.multiply(fator).divide(
                    fator.subtract(BigDecimal.ONE), 
                    2, 
                    java.math.RoundingMode.HALF_UP
                )
            );
        }
    }
}
