package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.api.dto.ContribuicaoReservaDTO;
import com.tallyto.gestorfinanceiro.api.dto.HistoricoContribuicaoDTO;
import com.tallyto.gestorfinanceiro.api.dto.ReservaEmergenciaDTO;
import com.tallyto.gestorfinanceiro.api.dto.ReservaEmergenciaDetalheDTO;
import com.tallyto.gestorfinanceiro.api.dto.ReservaEmergenciaInputDTO;
import com.tallyto.gestorfinanceiro.core.application.services.ReservaEmergenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Reserva de Emergência", description = "Gestão de reserva de emergência")
@RestController
@RequestMapping("/api/reserva-emergencia")
public class ReservaEmergenciaController {

    @Autowired
    private ReservaEmergenciaService reservaEmergenciaService;

    @GetMapping
    @Operation(summary = "Listar reservas de emergência")
    public ResponseEntity<List<ReservaEmergenciaDTO>> findAll() {
        return ResponseEntity.ok(reservaEmergenciaService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar reserva de emergência por ID")
    public ResponseEntity<ReservaEmergenciaDetalheDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(reservaEmergenciaService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Criar reserva de emergência")
    public ResponseEntity<ReservaEmergenciaDTO> create(@Valid @RequestBody ReservaEmergenciaInputDTO inputDTO) {
        return new ResponseEntity<>(reservaEmergenciaService.create(inputDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar reserva de emergência")
    public ResponseEntity<ReservaEmergenciaDTO> update(@PathVariable Long id, @Valid @RequestBody ReservaEmergenciaInputDTO inputDTO) {
        return ResponseEntity.ok(reservaEmergenciaService.update(id, inputDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir reserva de emergência")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservaEmergenciaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/saldo")
    @Operation(summary = "Atualizar saldo da reserva")
    public ResponseEntity<ReservaEmergenciaDTO> atualizarSaldo(@PathVariable Long id, @RequestBody BigDecimal valor) {
        return ResponseEntity.ok(reservaEmergenciaService.atualizarSaldo(id, valor));
    }

    @GetMapping("/calcular-objetivo")
    @Operation(summary = "Calcular objetivo automático da reserva")
    public ResponseEntity<BigDecimal> calcularObjetivoAutomatico(@RequestParam Integer multiplicadorDespesas) {
        return ResponseEntity.ok(reservaEmergenciaService.calcularObjetivoAutomatico(multiplicadorDespesas));
    }

    @GetMapping("/simulacao")
    @Operation(summary = "Simular tempo para completar a reserva")
    public ResponseEntity<Integer> simularTempoParaCompletar(
            @RequestParam BigDecimal objetivo, 
            @RequestParam BigDecimal valorContribuicaoMensal) {
        return ResponseEntity.ok(reservaEmergenciaService.simularTempoParaCompletar(objetivo, valorContribuicaoMensal));
    }
    
    @PostMapping("/{id}/contribuir")
    @Operation(summary = "Contribuir para a reserva")
    public ResponseEntity<ReservaEmergenciaDTO> contribuirParaReserva(
            @PathVariable Long id,
            @Valid @RequestBody ContribuicaoReservaDTO contribuicaoDTO) {
        return ResponseEntity.ok(reservaEmergenciaService.contribuirParaReserva(id, contribuicaoDTO));
    }

    @GetMapping("/{id}/historico")
    @Operation(summary = "Listar histórico de contribuições da reserva")
    public ResponseEntity<List<HistoricoContribuicaoDTO>> historico(@PathVariable Long id) {
        return ResponseEntity.ok(reservaEmergenciaService.historico(id));
    }
}
