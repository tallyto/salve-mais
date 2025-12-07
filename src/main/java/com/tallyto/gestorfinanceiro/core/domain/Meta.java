package com.tallyto.gestorfinanceiro.core.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "metas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Meta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(name = "valor_alvo", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorAlvo;

    @Column(name = "valor_atual", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal valorAtual = BigDecimal.ZERO;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_alvo", nullable = false)
    private LocalDate dataAlvo;

    @Column(name = "categoria_id")
    private Long categoriaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusMeta status = StatusMeta.EM_ANDAMENTO;

    @Column(name = "valor_mensal_sugerido", precision = 15, scale = 2)
    private BigDecimal valorMensalSugerido;

    @Column(name = "icone", length = 50)
    private String icone;

    @Column(name = "cor", length = 20)
    private String cor;

    @Column(name = "notificar_progresso")
    @Builder.Default
    private Boolean notificarProgresso = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum StatusMeta {
        EM_ANDAMENTO,
        CONCLUIDA,
        CANCELADA,
        PAUSADA
    }

    public BigDecimal calcularPercentualConcluido() {
        if (valorAlvo.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return valorAtual.divide(valorAlvo, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal calcularValorRestante() {
        return valorAlvo.subtract(valorAtual);
    }

    public long calcularDiasRestantes() {
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), dataAlvo);
    }
}
