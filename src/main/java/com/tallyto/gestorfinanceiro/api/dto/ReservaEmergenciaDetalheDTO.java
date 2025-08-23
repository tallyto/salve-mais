package com.tallyto.gestorfinanceiro.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReservaEmergenciaDetalheDTO(
    Long id,
    BigDecimal objetivo,
    Integer multiplicadorDespesas,
    BigDecimal saldoAtual,
    BigDecimal percentualConcluido,
    LocalDate dataCriacao,
    LocalDate dataPrevisaoCompletar,
    BigDecimal valorContribuicaoMensal,
    ContaDTO conta,
    int mesesRestantes,
    BigDecimal despesasMensaisMedia
) {}
