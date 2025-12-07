package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.api.dto.PlanoCompraDTO;
import com.tallyto.gestorfinanceiro.core.application.services.PlanoCompraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/planos-compra")
@RequiredArgsConstructor
@Tag(name = "Planos de Compra", description = "Planejamento de grandes compras e financiamentos")
public class PlanoCompraController {

    private final PlanoCompraService planoCompraService;

    @PostMapping
    @Operation(summary = "Criar plano", description = "Cria um novo plano de compra")
    public ResponseEntity<PlanoCompraDTO> criar(@Valid @RequestBody PlanoCompraDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planoCompraService.criar(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar plano", description = "Atualiza os dados de um plano existente")
    public ResponseEntity<PlanoCompraDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody PlanoCompraDTO dto) {
        return ResponseEntity.ok(planoCompraService.atualizar(id, dto));
    }

    @GetMapping
    @Operation(summary = "Listar planos", description = "Lista todos os planos de compra ordenados por prioridade")
    public ResponseEntity<List<PlanoCompraDTO>> listarTodos() {
        return ResponseEntity.ok(planoCompraService.listarTodos());
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Listar por status", description = "Lista planos filtrados por status")
    public ResponseEntity<List<PlanoCompraDTO>> listarPorStatus(@PathVariable String status) {
        return ResponseEntity.ok(planoCompraService.listarPorStatus(status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar plano", description = "Busca um plano específico por ID")
    public ResponseEntity<PlanoCompraDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(planoCompraService.buscarPorId(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar plano", description = "Remove um plano do sistema")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        planoCompraService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
