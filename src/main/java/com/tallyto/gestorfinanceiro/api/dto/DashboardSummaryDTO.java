package com.tallyto.gestorfinanceiro.api.dto;

import java.math.BigDecimal;

public record DashboardSummaryDTO(
    BigDecimal saldoTotal,
    BigDecimal receitasMes,
    BigDecimal despesasMes,
    long totalContas,
    long totalCategorias,
    BigDecimal saldoMesAnterior,
    BigDecimal receitasMesAnterior,
    BigDecimal despesasMesAnterior
) {}
