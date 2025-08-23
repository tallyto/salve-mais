package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.api.dto.ContribuicaoReservaDTO;
import com.tallyto.gestorfinanceiro.api.dto.ReservaEmergenciaDTO;
import com.tallyto.gestorfinanceiro.api.dto.ReservaEmergenciaDetalheDTO;
import com.tallyto.gestorfinanceiro.api.dto.ReservaEmergenciaInputDTO;
import com.tallyto.gestorfinanceiro.core.application.services.ReservaEmergenciaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/reserva-emergencia")
public class ReservaEmergenciaController {

    @Autowired
    private ReservaEmergenciaService reservaEmergenciaService;

    @GetMapping
    public ResponseEntity<List<ReservaEmergenciaDTO>> findAll() {
        return ResponseEntity.ok(reservaEmergenciaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaEmergenciaDetalheDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(reservaEmergenciaService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ReservaEmergenciaDTO> create(@Valid @RequestBody ReservaEmergenciaInputDTO inputDTO) {
        return new ResponseEntity<>(reservaEmergenciaService.create(inputDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservaEmergenciaDTO> update(@PathVariable Long id, @Valid @RequestBody ReservaEmergenciaInputDTO inputDTO) {
        return ResponseEntity.ok(reservaEmergenciaService.update(id, inputDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservaEmergenciaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/saldo")
    public ResponseEntity<ReservaEmergenciaDTO> atualizarSaldo(@PathVariable Long id, @RequestBody BigDecimal valor) {
        return ResponseEntity.ok(reservaEmergenciaService.atualizarSaldo(id, valor));
    }

    @GetMapping("/calcular-objetivo")
    public ResponseEntity<BigDecimal> calcularObjetivoAutomatico(@RequestParam Integer multiplicadorDespesas) {
        return ResponseEntity.ok(reservaEmergenciaService.calcularObjetivoAutomatico(multiplicadorDespesas));
    }

    @GetMapping("/simulacao")
    public ResponseEntity<Integer> simularTempoParaCompletar(
            @RequestParam BigDecimal objetivo, 
            @RequestParam BigDecimal valorContribuicaoMensal) {
        return ResponseEntity.ok(reservaEmergenciaService.simularTempoParaCompletar(objetivo, valorContribuicaoMensal));
    }
    
    @PostMapping("/{id}/contribuir")
    public ResponseEntity<ReservaEmergenciaDTO> contribuirParaReserva(
            @PathVariable Long id,
            @Valid @RequestBody ContribuicaoReservaDTO contribuicaoDTO) {
        return ResponseEntity.ok(reservaEmergenciaService.contribuirParaReserva(id, contribuicaoDTO));
    }
}
