package com.tallyto.gestorfinanceiro.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PlanoDTO(
        UUID id,
        String nome,
        String descricao,
        String tipo,
        BigDecimal precoMensal,
        Integer maxUsuarios,
        Integer maxTransacoesMes,
        BigDecimal maxStorageGb
) {}
