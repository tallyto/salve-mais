package com.tallyto.gestorfinanceiro.api.dto;

import java.math.BigDecimal;

public record ReservaEmergenciaInputDTO(
    BigDecimal objetivo,
    Integer multiplicadorDespesas,
    BigDecimal valorContribuicaoMensal,
    Long contaId,
    BigDecimal taxaRendimento
) {}
