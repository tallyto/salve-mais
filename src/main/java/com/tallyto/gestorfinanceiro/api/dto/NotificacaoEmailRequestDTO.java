package com.tallyto.gestorfinanceiro.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Schema(description = "DTO para habilitar notificações por email")
@Getter
@Setter
public class NotificacaoEmailRequestDTO {

    @NotBlank(message = "O domínio é obrigatório")
    @Schema(description = "Domínio do tenant", example = "empresa.com")
    private String domain;

    @NotNull(message = "O horário é obrigatório")
    @Schema(description = "Horário em que a notificação será enviada", example = "08:00:00")
    private LocalTime horario;

    @NotNull(message = "O status ativo é obrigatório")
    @Schema(description = "Define se a notificação está ativa", example = "true")
    private Boolean ativo;
}
