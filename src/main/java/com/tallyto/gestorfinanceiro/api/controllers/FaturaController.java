package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.api.dto.FaturaManualDTO;
import com.tallyto.gestorfinanceiro.api.dto.FaturaResponseDTO;
import com.tallyto.gestorfinanceiro.core.application.services.FaturaService;
import com.tallyto.gestorfinanceiro.core.domain.entities.Fatura;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/faturas")
@Tag(name = "Faturas", description = "Endpoints para gerenciamento de faturas de cartão de crédito")
public class FaturaController {

    @Autowired
    private FaturaService faturaService;

    @Operation(
            summary = "Listar todas as faturas",
            description = "Retorna uma lista de todas as faturas cadastradas no sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de faturas retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<List<FaturaResponseDTO>> listarFaturas() {
        List<Fatura> faturas = faturaService.listar();
        List<FaturaResponseDTO> faturasDTOs = faturas.stream()
                .map(FaturaResponseDTO::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(faturasDTOs);
    }

    @Operation(
            summary = "Buscar fatura por ID",
            description = "Retorna uma fatura específica pelo seu ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fatura encontrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Fatura não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<FaturaResponseDTO> buscarFatura(
            @Parameter(description = "ID da fatura", example = "1")
            @PathVariable Long id
    ) {
        Fatura fatura = faturaService.findOrFail(id);
        return ResponseEntity.ok(FaturaResponseDTO.fromEntity(fatura));
    }

    @Operation(
            summary = "Criar fatura manual",
            description = "Cria uma nova fatura manualmente, especificando cartão, valor e data de vencimento"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Fatura criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "404", description = "Cartão de crédito não encontrado")
    })
    @PostMapping("/manual")
    public ResponseEntity<FaturaResponseDTO> criarFaturaManual(
            @Valid @RequestBody FaturaManualDTO faturaManualDTO
    ) {
        Fatura fatura = faturaService.criarFaturaManual(
                faturaManualDTO.cartaoCreditoId(),
                faturaManualDTO.valorTotal(),
                faturaManualDTO.dataVencimento()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FaturaResponseDTO.fromEntity(fatura));
    }

    @Operation(
            summary = "Gerar fatura automática",
            description = "Gera uma fatura automaticamente baseada nas compras do cartão"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Fatura gerada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cartão de crédito não encontrado")
    })
    @PostMapping("/gerar/{cartaoCreditoId}")
    public ResponseEntity<Void> gerarFaturaAutomatica(
            @Parameter(description = "ID do cartão de crédito", example = "1")
            @PathVariable Long cartaoCreditoId
    ) {
        faturaService.gerarFatura(cartaoCreditoId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "Marcar fatura como paga",
            description = "Marca uma fatura como paga (método legado)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fatura marcada como paga com sucesso"),
            @ApiResponse(responseCode = "404", description = "Fatura não encontrada")
    })
    @PatchMapping("/{id}/pagar")
    public ResponseEntity<Void> marcarComoPaga(
            @Parameter(description = "ID da fatura", example = "1")
            @PathVariable Long id
    ) {
        faturaService.marcarComoPaga(id);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Pagar fatura com conta específica",
            description = "Paga uma fatura debitando o valor de uma conta específica"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fatura paga com sucesso"),
            @ApiResponse(responseCode = "400", description = "Saldo insuficiente ou fatura já paga"),
            @ApiResponse(responseCode = "404", description = "Fatura ou conta não encontrada")
    })
    @PatchMapping("/{faturaId}/pagar/{contaId}")
    public ResponseEntity<Void> pagarFaturaComConta(
            @Parameter(description = "ID da fatura", example = "1")
            @PathVariable Long faturaId,
            @Parameter(description = "ID da conta", example = "1")
            @PathVariable Long contaId
    ) {
        faturaService.marcarComoPaga(faturaId, contaId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Excluir fatura",
            description = "Exclui uma fatura do sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Fatura excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Fatura não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirFatura(
            @Parameter(description = "ID da fatura", example = "1")
            @PathVariable Long id
    ) {
        faturaService.excluirFatura(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Listar faturas não pagas",
            description = "Retorna uma lista de todas as faturas pendentes"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de faturas pendentes retornada com sucesso")
    })
    @GetMapping("/pendentes")
    public ResponseEntity<List<FaturaResponseDTO>> listarFaturasPendentes() {
        List<Fatura> faturas = faturaService.listarNaoPagas();
        List<FaturaResponseDTO> faturasDTOs = faturas.stream()
                .map(FaturaResponseDTO::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(faturasDTOs);
    }

    @Operation(
            summary = "Listar faturas por conta",
            description = "Retorna uma lista de todas as faturas pagas com uma conta específica"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de faturas da conta retornada com sucesso")
    })
    @GetMapping("/conta/{contaId}")
    public ResponseEntity<List<FaturaResponseDTO>> listarFaturasPorConta(
            @Parameter(description = "ID da conta", example = "1")
            @PathVariable Long contaId
    ) {
        List<Fatura> faturas = faturaService.listarPorConta(contaId);
        List<FaturaResponseDTO> faturasDTOs = faturas.stream()
                .map(FaturaResponseDTO::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(faturasDTOs);
    }

    // Método legado mantido para compatibilidade
    @PostMapping("/{cardId}")
    public void gerarFatura(@PathVariable Long cardId) {
        faturaService.gerarFatura(cardId);
    }
}
