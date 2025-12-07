package com.tallyto.gestorfinanceiro.core.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "plano_aposentadoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanoAposentadoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idade_atual", nullable = false)
    private Integer idadeAtual;

    @Column(name = "idade_aposentadoria", nullable = false)
    @Builder.Default
    private Integer idadeAposentadoria = 65;

    @Column(name = "renda_desejada", nullable = false, precision = 15, scale = 2)
    private BigDecimal rendaDesejada;

    @Column(name = "patrimonio_atual", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal patrimonioAtual = BigDecimal.ZERO;

    @Column(name = "contribuicao_mensal", precision = 15, scale = 2)
    private BigDecimal contribuicaoMensal;

    @Column(name = "taxa_retorno_anual", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxaRetornoAnual = BigDecimal.valueOf(8); // 8% ao ano

    @Column(name = "expectativa_vida")
    @Builder.Default
    private Integer expectativaVida = 85;

    @Column(name = "patrimonio_necessario", precision = 15, scale = 2)
    private BigDecimal patrimonioNecessario;

    @Column(name = "patrimonio_projetado", precision = 15, scale = 2)
    private BigDecimal patrimonioProjetado;

    @Column(name = "ativo")
    @Builder.Default
    private Boolean ativo = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void calcularPatrimonioNecessario() {
        // Calcula quanto precisa ter acumulado para gerar a renda desejada
        // Usando a regra dos 4% (retirar 4% ao ano do patrimônio)
        this.patrimonioNecessario = rendaDesejada
                .multiply(BigDecimal.valueOf(12)) // Renda anual
                .multiply(BigDecimal.valueOf(25)); // 1/0.04 = 25
    }

    public void calcularPatrimonioProjetado() {
        // Calcula quanto terá acumulado considerando patrimônio atual e contribuições mensais
        int anosAteAposentadoria = idadeAposentadoria - idadeAtual;
        int mesesAteAposentadoria = anosAteAposentadoria * 12;

        BigDecimal taxaMensal = taxaRetornoAnual
                .divide(BigDecimal.valueOf(100), 6, java.math.RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 6, java.math.RoundingMode.HALF_UP);

        // Valor futuro do patrimônio atual
        BigDecimal fatorPatrimonio = BigDecimal.ONE.add(taxaMensal).pow(mesesAteAposentadoria);
        BigDecimal valorFuturoPatrimonio = patrimonioAtual.multiply(fatorPatrimonio);

        // Valor futuro das contribuições mensais (anuidade)
        BigDecimal valorFuturoContribuicoes = BigDecimal.ZERO;
        if (contribuicaoMensal != null && contribuicaoMensal.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal numerador = fatorPatrimonio.subtract(BigDecimal.ONE);
            valorFuturoContribuicoes = contribuicaoMensal.multiply(
                    numerador.divide(taxaMensal, 2, java.math.RoundingMode.HALF_UP)
            );
        }

        this.patrimonioProjetado = valorFuturoPatrimonio.add(valorFuturoContribuicoes);
    }

    public BigDecimal calcularDeficitOuSuperavit() {
        if (patrimonioProjetado == null || patrimonioNecessario == null) {
            return BigDecimal.ZERO;
        }
        return patrimonioProjetado.subtract(patrimonioNecessario);
    }

    public BigDecimal calcularContribuicaoMensalNecessaria() {
        int anosAteAposentadoria = idadeAposentadoria - idadeAtual;
        if (anosAteAposentadoria <= 0) {
            return BigDecimal.ZERO;
        }

        int mesesAteAposentadoria = anosAteAposentadoria * 12;
        
        BigDecimal taxaMensal = taxaRetornoAnual
                .divide(BigDecimal.valueOf(100), 6, java.math.RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 6, java.math.RoundingMode.HALF_UP);

        // Valor futuro do patrimônio atual
        BigDecimal fatorPatrimonio = BigDecimal.ONE.add(taxaMensal).pow(mesesAteAposentadoria);
        BigDecimal valorFuturoPatrimonio = patrimonioAtual.multiply(fatorPatrimonio);

        // Quanto ainda precisa acumular
        BigDecimal valorNecessario = patrimonioNecessario.subtract(valorFuturoPatrimonio);
        
        if (valorNecessario.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // Calcular contribuição mensal necessária
        BigDecimal numerador = fatorPatrimonio.subtract(BigDecimal.ONE);
        BigDecimal fatorAnuidade = numerador.divide(taxaMensal, 6, java.math.RoundingMode.HALF_UP);
        
        return valorNecessario.divide(fatorAnuidade, 2, java.math.RoundingMode.HALF_UP);
    }

    public String avaliarStatus() {
        BigDecimal deficit = calcularDeficitOuSuperavit();
        
        if (deficit.compareTo(BigDecimal.ZERO) >= 0) {
            return "NO_CAMINHO_CERTO";
        } else if (deficit.abs().compareTo(patrimonioNecessario.multiply(BigDecimal.valueOf(0.1))) <= 0) {
            return "ATENCAO";
        } else {
            return "AJUSTE_NECESSARIO";
        }
    }

    public Integer calcularAnosAteAposentadoria() {
        return idadeAposentadoria - idadeAtual;
    }

    public Integer calcularAnosAposAposentadoria() {
        return expectativaVida - idadeAposentadoria;
    }
}
