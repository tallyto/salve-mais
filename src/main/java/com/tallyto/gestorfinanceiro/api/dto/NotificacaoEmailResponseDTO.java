package com.tallyto.gestorfinanceiro.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Schema(description = "DTO de resposta para notificações por email")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificacaoEmailResponseDTO {

    @Schema(description = "ID da notificação")
    private UUID id;

    @Schema(description = "Domínio do tenant", example = "empresa.com")
    private String domain;

    @Schema(description = "Horário em que a notificação será enviada", example = "08:00:00")
    private LocalTime horario;

    @Schema(description = "Define se a notificação está ativa", example = "true")
    private Boolean ativo;

    @Schema(description = "Data de criação")
    private LocalDateTime createdAt;

    @Schema(description = "Data de atualização")
    private LocalDateTime updatedAt;
}
