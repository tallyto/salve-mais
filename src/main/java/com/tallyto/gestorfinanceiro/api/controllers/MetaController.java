package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.api.dto.MetaAtualizarProgressoDTO;
import com.tallyto.gestorfinanceiro.api.dto.MetaDTO;
import com.tallyto.gestorfinanceiro.core.application.services.MetaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metas")
@RequiredArgsConstructor
@Tag(name = "Metas", description = "Gerenciamento de metas financeiras")
public class MetaController {

    private final MetaService metaService;

    @PostMapping
    @Operation(summary = "Criar nova meta", description = "Cria uma nova meta de economia")
    public ResponseEntity<MetaDTO> criar(@Valid @RequestBody MetaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(metaService.criar(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar meta", description = "Atualiza os dados de uma meta existente")
    public ResponseEntity<MetaDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody MetaDTO dto) {
        return ResponseEntity.ok(metaService.atualizar(id, dto));
    }

    @PatchMapping("/{id}/progresso")
    @Operation(summary = "Atualizar progresso", description = "Adiciona valor ao progresso da meta")
    public ResponseEntity<MetaDTO> atualizarProgresso(
            @PathVariable Long id,
            @Valid @RequestBody MetaAtualizarProgressoDTO dto) {
        return ResponseEntity.ok(metaService.atualizarProgresso(id, dto));
    }

    @GetMapping
    @Operation(summary = "Listar metas", description = "Lista todas as metas do usuário")
    public ResponseEntity<List<MetaDTO>> listarTodas() {
        return ResponseEntity.ok(metaService.listarTodas());
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Listar por status", description = "Lista metas filtradas por status")
    public ResponseEntity<List<MetaDTO>> listarPorStatus(@PathVariable String status) {
        return ResponseEntity.ok(metaService.listarPorStatus(status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar meta", description = "Busca uma meta específica por ID")
    public ResponseEntity<MetaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(metaService.buscarPorId(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar meta", description = "Remove uma meta do sistema")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        metaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
