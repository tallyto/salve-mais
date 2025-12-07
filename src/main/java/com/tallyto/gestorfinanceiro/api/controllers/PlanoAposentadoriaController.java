package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.api.dto.PlanoAposentadoriaDTO;
import com.tallyto.gestorfinanceiro.core.application.services.PlanoAposentadoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plano-aposentadoria")
@RequiredArgsConstructor
@Tag(name = "Plano de Aposentadoria", description = "Simulador e planejamento de aposentadoria")
public class PlanoAposentadoriaController {

    private final PlanoAposentadoriaService planoAposentadoriaService;

    @PostMapping
    @Operation(summary = "Criar plano", description = "Cria um novo plano de aposentadoria")
    public ResponseEntity<PlanoAposentadoriaDTO> criar(@Valid @RequestBody PlanoAposentadoriaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planoAposentadoriaService.criar(dto));
    }

    @PutMapping
    @Operation(summary = "Atualizar plano", description = "Atualiza os dados do plano de aposentadoria")
    public ResponseEntity<PlanoAposentadoriaDTO> atualizar(@Valid @RequestBody PlanoAposentadoriaDTO dto) {
        return ResponseEntity.ok(planoAposentadoriaService.atualizar(dto));
    }

    @GetMapping
    @Operation(summary = "Buscar plano", description = "Retorna os dados do plano de aposentadoria com simulações")
    public ResponseEntity<PlanoAposentadoriaDTO> buscar() {
        return ResponseEntity.ok(planoAposentadoriaService.buscar());
    }

    @DeleteMapping
    @Operation(summary = "Deletar plano", description = "Remove o plano de aposentadoria")
    public ResponseEntity<Void> deletar() {
        planoAposentadoriaService.deletar();
        return ResponseEntity.noContent().build();
    }
}
