package com.tallyto.gestorfinanceiro.web.api.dto;

import java.math.BigDecimal;

public record ReservaEmergenciaInputDTO(
    BigDecimal objetivo,
    Integer multiplicadorDespesas,
    BigDecimal valorContribuicaoMensal,
    Long contaId,
    BigDecimal taxaRendimento
) {}
