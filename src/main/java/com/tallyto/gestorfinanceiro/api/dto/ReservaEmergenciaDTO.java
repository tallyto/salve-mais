package com.tallyto.gestorfinanceiro.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReservaEmergenciaDTO(
    Long id,
    BigDecimal objetivo,
    Integer multiplicadorDespesas,
    BigDecimal saldoAtual,
    BigDecimal percentualConcluido,
    LocalDate dataCriacao,
    LocalDate dataPrevisaoCompletar,
    BigDecimal valorContribuicaoMensal,
    Long contaId
) {}
