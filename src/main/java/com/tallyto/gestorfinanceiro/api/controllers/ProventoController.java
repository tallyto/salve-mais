package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.api.dto.ProventoDTO;
import com.tallyto.gestorfinanceiro.core.domain.entities.Conta;
import com.tallyto.gestorfinanceiro.core.domain.entities.Provento;
import com.tallyto.gestorfinanceiro.core.application.services.ProventoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/proventos")
@Validated
public class ProventoController {

    private final ProventoService proventoService;

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
    public Page<Provento> listarProventos(Pageable pageable) {
        return proventoService.listarProventos(pageable);
    }

    private Provento mapDTOToEntity(ProventoDTO proventoDTO) {
        Provento provento = new Provento();
        provento.setDescricao(proventoDTO.descricao());
        provento.setValor(proventoDTO.valor());
        provento.setData(proventoDTO.data());
        var conta = new Conta();
        conta.setId(proventoDTO.contaId());
        provento.setConta(conta);
        return provento;
    }
}