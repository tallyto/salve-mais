package com.tallyto.gestorfinanceiro.web.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HistoricoContribuicaoDTO(
    Long id,
    BigDecimal valor,
    LocalDateTime data,
    String contaOrigem
) {}
