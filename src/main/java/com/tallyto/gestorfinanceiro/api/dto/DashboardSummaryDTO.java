package com.tallyto.gestorfinanceiro.api.dto;

import java.math.BigDecimal;

public class DashboardSummaryDTO {
    private BigDecimal saldoTotal;
    private BigDecimal receitasMes;
    private BigDecimal despesasMes;
    private long totalContas;
    private long totalCategorias;
    private BigDecimal saldoMesAnterior;
    private BigDecimal receitasMesAnterior;
    private BigDecimal despesasMesAnterior;
    private ReservaEmergenciaDTO reservaEmergencia;
    private boolean temReservaEmergencia;

    public static class ReservaEmergenciaDTO {
        private Long id;
        private BigDecimal objetivo;
        private BigDecimal saldoAtual;
        private BigDecimal percentualConcluido;
        private Integer tempoRestante;

        public ReservaEmergenciaDTO() {}

        public ReservaEmergenciaDTO(Long id, BigDecimal objetivo, BigDecimal saldoAtual, 
                                    BigDecimal percentualConcluido, Integer tempoRestante) {
            this.id = id;
            this.objetivo = objetivo;
            this.saldoAtual = saldoAtual;
            this.percentualConcluido = percentualConcluido;
            this.tempoRestante = tempoRestante;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public BigDecimal getObjetivo() {
            return objetivo;
        }

        public void setObjetivo(BigDecimal objetivo) {
            this.objetivo = objetivo;
        }

        public BigDecimal getSaldoAtual() {
            return saldoAtual;
        }

        public void setSaldoAtual(BigDecimal saldoAtual) {
            this.saldoAtual = saldoAtual;
        }

        public BigDecimal getPercentualConcluido() {
            return percentualConcluido;
        }

        public void setPercentualConcluido(BigDecimal percentualConcluido) {
            this.percentualConcluido = percentualConcluido;
        }

        public Integer getTempoRestante() {
            return tempoRestante;
        }

        public void setTempoRestante(Integer tempoRestante) {
            this.tempoRestante = tempoRestante;
        }
    }

    public DashboardSummaryDTO() {}

    public DashboardSummaryDTO(BigDecimal saldoTotal, BigDecimal receitasMes, BigDecimal despesasMes,
                             long totalContas, long totalCategorias, BigDecimal saldoMesAnterior,
                             BigDecimal receitasMesAnterior, BigDecimal despesasMesAnterior,
                             BigDecimal reservaEmergenciaAtual, BigDecimal reservaEmergenciaObjetivo,
                             BigDecimal reservaEmergenciaPercentual, Integer tempoRestante) {
        this.saldoTotal = saldoTotal;
        this.receitasMes = receitasMes;
        this.despesasMes = despesasMes;
        this.totalContas = totalContas;
        this.totalCategorias = totalCategorias;
        this.saldoMesAnterior = saldoMesAnterior;
        this.receitasMesAnterior = receitasMesAnterior;
        this.despesasMesAnterior = despesasMesAnterior;

        // Configurar a reserva de emergência se existir
        if (reservaEmergenciaObjetivo != null && !reservaEmergenciaObjetivo.equals(BigDecimal.ZERO)) {
            this.reservaEmergencia = new ReservaEmergenciaDTO(
                null, // ID não disponível aqui
                reservaEmergenciaObjetivo,
                reservaEmergenciaAtual,
                reservaEmergenciaPercentual,
                tempoRestante
            );
            this.temReservaEmergencia = true;
        } else {
            this.temReservaEmergencia = false;
        }
    }

    // Getters e Setters

    public BigDecimal getSaldoTotal() {
        return saldoTotal;
    }

    public void setSaldoTotal(BigDecimal saldoTotal) {
        this.saldoTotal = saldoTotal;
    }

    public BigDecimal getReceitasMes() {
        return receitasMes;
    }

    public void setReceitasMes(BigDecimal receitasMes) {
        this.receitasMes = receitasMes;
    }

    public BigDecimal getDespesasMes() {
        return despesasMes;
    }

    public void setDespesasMes(BigDecimal despesasMes) {
        this.despesasMes = despesasMes;
    }

    public long getTotalContas() {
        return totalContas;
    }

    public void setTotalContas(long totalContas) {
        this.totalContas = totalContas;
    }

    public long getTotalCategorias() {
        return totalCategorias;
    }

    public void setTotalCategorias(long totalCategorias) {
        this.totalCategorias = totalCategorias;
    }

    public BigDecimal getSaldoMesAnterior() {
        return saldoMesAnterior;
    }

    public void setSaldoMesAnterior(BigDecimal saldoMesAnterior) {
        this.saldoMesAnterior = saldoMesAnterior;
    }

    public BigDecimal getReceitasMesAnterior() {
        return receitasMesAnterior;
    }

    public void setReceitasMesAnterior(BigDecimal receitasMesAnterior) {
        this.receitasMesAnterior = receitasMesAnterior;
    }

    public BigDecimal getDespesasMesAnterior() {
        return despesasMesAnterior;
    }

    public void setDespesasMesAnterior(BigDecimal despesasMesAnterior) {
        this.despesasMesAnterior = despesasMesAnterior;
    }

    public ReservaEmergenciaDTO getReservaEmergencia() {
        return reservaEmergencia;
    }

    public void setReservaEmergencia(ReservaEmergenciaDTO reservaEmergencia) {
        this.reservaEmergencia = reservaEmergencia;
    }

    public boolean isTemReservaEmergencia() {
        return temReservaEmergencia;
    }

    public void setTemReservaEmergencia(boolean temReservaEmergencia) {
        this.temReservaEmergencia = temReservaEmergencia;
    }
}
