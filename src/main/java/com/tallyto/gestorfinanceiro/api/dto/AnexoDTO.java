package com.tallyto.gestorfinanceiro.api.dto;

import java.time.LocalDateTime;

public record AnexoDTO(
    Long id,
    String nome,
    String tipo,
    LocalDateTime dataUpload,
    Long contaFixaId
) {}
