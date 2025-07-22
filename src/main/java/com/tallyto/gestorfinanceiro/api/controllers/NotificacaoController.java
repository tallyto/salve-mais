package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.api.dto.NotificacaoDTO;
import com.tallyto.gestorfinanceiro.core.application.services.NotificacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificacoes")
@Tag(name = "Notificações", description = "Endpoints para gerenciamento de notificações de contas e faturas atrasadas")
public class NotificacaoController {

    @Autowired
    private NotificacaoService notificacaoService;

    @Operation(
            summary = "Obter todas as notificações",
            description = "Retorna uma lista de todas as notificações de contas atrasadas, próximas ao vencimento e faturas pendentes"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de notificações obtida com sucesso")
    })
    @GetMapping
    public ResponseEntity<List<NotificacaoDTO>> obterNotificacoes() {
        List<NotificacaoDTO> notificacoes = notificacaoService.obterNotificacoes();
        return ResponseEntity.ok(notificacoes);
    }

    @Operation(
            summary = "Obter contas atrasadas",
            description = "Retorna apenas as notificações de contas fixas em atraso"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de contas atrasadas obtida com sucesso")
    })
    @GetMapping("/contas-atrasadas")
    public ResponseEntity<List<NotificacaoDTO>> obterContasAtrasadas() {
        List<NotificacaoDTO> notificacoes = notificacaoService.obterNotificacaoContasAtrasadas();
        return ResponseEntity.ok(notificacoes);
    }

    @Operation(
            summary = "Obter contas próximas ao vencimento",
            description = "Retorna as notificações de contas que vencem nos próximos 7 dias"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de contas próximas ao vencimento obtida com sucesso")
    })
    @GetMapping("/contas-proximas-vencimento")
    public ResponseEntity<List<NotificacaoDTO>> obterContasProximasVencimento() {
        List<NotificacaoDTO> notificacoes = notificacaoService.obterNotificacaoContasProximasVencimento();
        return ResponseEntity.ok(notificacoes);
    }

    @Operation(
            summary = "Obter faturas atrasadas",
            description = "Retorna apenas as notificações de faturas de cartão em atraso"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de faturas atrasadas obtida com sucesso")
    })
    @GetMapping("/faturas-atrasadas")
    public ResponseEntity<List<NotificacaoDTO>> obterFaturasAtrasadas() {
        List<NotificacaoDTO> notificacoes = notificacaoService.obterNotificacaoFaturasAtrasadas();
        return ResponseEntity.ok(notificacoes);
    }

    @Operation(
            summary = "Obter resumo de notificações",
            description = "Retorna um resumo com o total de notificações por tipo e prioridade"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumo de notificações obtido com sucesso")
    })
    @GetMapping("/resumo")
    public ResponseEntity<Map<String, Object>> obterResumoNotificacoes() {
        List<NotificacaoDTO> todasNotificacoes = notificacaoService.obterNotificacoes();
        
        long totalNotificacoes = todasNotificacoes.size();
        long notificacoesCriticas = todasNotificacoes.stream()
                .filter(n -> n.prioridade() == NotificacaoDTO.Prioridade.CRITICA)
                .count();
        long notificacoesAltas = todasNotificacoes.stream()
                .filter(n -> n.prioridade() == NotificacaoDTO.Prioridade.ALTA)
                .count();
        long contasAtrasadas = todasNotificacoes.stream()
                .filter(n -> "CONTA_ATRASADA".equals(n.tipo()))
                .count();
        long faturasAtrasadas = todasNotificacoes.stream()
                .filter(n -> "FATURA_ATRASADA".equals(n.tipo()))
                .count();

        Map<String, Object> resumo = Map.of(
                "totalNotificacoes", totalNotificacoes,
                "notificacoesCriticas", notificacoesCriticas,
                "notificacoesAltas", notificacoesAltas,
                "contasAtrasadas", contasAtrasadas,
                "faturasAtrasadas", faturasAtrasadas,
                "temNotificacoes", totalNotificacoes > 0
        );

        return ResponseEntity.ok(resumo);
    }
}
