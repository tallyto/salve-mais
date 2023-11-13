package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.api.dto.ProventoDTO;
import com.tallyto.gestorfinanceiro.core.domain.entities.Provento;
import com.tallyto.gestorfinanceiro.core.application.services.ProventoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/proventos")
@Validated
public class ProventoController {

    private final ProventoService proventoService;

    @Autowired
    public ProventoController(ProventoService proventoService) {
        this.proventoService = proventoService;
    }

    @PostMapping
    public ResponseEntity<Provento> criarProvento(@Valid @RequestBody ProventoDTO proventoDTO) {
        Provento provento = mapDTOToEntity(proventoDTO);
        Provento proventoSalvo = proventoService.salvarProvento(provento);
        return ResponseEntity.ok(proventoSalvo);
    }

    @GetMapping
    public ResponseEntity<List<Provento>> listarProventos() {
        List<Provento> proventos = proventoService.listarProventos();
        return ResponseEntity.ok(proventos);
    }

    private Provento mapDTOToEntity(ProventoDTO proventoDTO) {
        Provento provento = new Provento();
        provento.setDescricao(proventoDTO.descricao());
        provento.setValor(proventoDTO.valor());
        provento.setData(proventoDTO.data());
        return provento;
    }
}