package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.api.dto.NotificacaoEmailRequestDTO;
import com.tallyto.gestorfinanceiro.api.dto.NotificacaoEmailResponseDTO;
import com.tallyto.gestorfinanceiro.core.application.services.NotificacaoEmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notificacoes-email")
@Tag(name = "Notificações por Email", description = "Endpoints para configuração de notificações por email")
public class NotificacaoEmailController {

    @Autowired
    private NotificacaoEmailService notificacaoEmailService;

    @Operation(
            summary = "Habilitar ou atualizar notificação por email",
            description = "Cria ou atualiza a configuração de notificação por email do tenant atual"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuração atualizada com sucesso"),
            @ApiResponse(responseCode = "201", description = "Configuração criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<NotificacaoEmailResponseDTO> habilitarNotificacao(
            @Valid @RequestBody NotificacaoEmailRequestDTO request) {
        NotificacaoEmailResponseDTO response = notificacaoEmailService.habilitarNotificacao(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Obter configuração de notificação do tenant atual",
            description = "Retorna a configuração de notificação por email do tenant autenticado"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuração obtida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Configuração não encontrada")
    })
    @GetMapping
    public ResponseEntity<NotificacaoEmailResponseDTO> obterNotificacao() {
        NotificacaoEmailResponseDTO response = notificacaoEmailService.obterNotificacaoDoTenantAtual();
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Desabilitar notificação por email",
            description = "Desativa o envio de notificações por email para o tenant atual"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Notificação desabilitada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Configuração não encontrada")
    })
    @DeleteMapping
    public ResponseEntity<Void> desabilitarNotificacao() {
        notificacaoEmailService.desabilitarNotificacaoDoTenantAtual();
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Enviar notificação de teste",
            description = "Envia imediatamente um email de teste com as notificações atuais do tenant"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email de teste enviado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Configuração não encontrada ou nenhuma notificação disponível")
    })
    @PostMapping("/testar")
    public ResponseEntity<String> enviarNotificacaoTeste() {
        notificacaoEmailService.enviarNotificacaoTeste();
        return ResponseEntity.ok("Email de teste enviado com sucesso");
    }
}
