package com.tallyto.gestorfinanceiro.api.dto;

import java.time.LocalDateTime;

public record ErrorResponseDTO(
        String message,
        int status,
        LocalDateTime timestamp
) {
    public static ErrorResponseDTO of(String message, int status) {
        return new ErrorResponseDTO(message, status, LocalDateTime.now());
    }
}
