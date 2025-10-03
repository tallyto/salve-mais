package com.tallyto.gestorfinanceiro.api.controllers;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tallyto.gestorfinanceiro.api.dto.TransacaoDTO;
import com.tallyto.gestorfinanceiro.api.dto.TransacaoFiltroDTO;
import com.tallyto.gestorfinanceiro.api.dto.TransacaoInputDTO;
import com.tallyto.gestorfinanceiro.core.application.services.TransacaoService;
import com.tallyto.gestorfinanceiro.core.domain.enums.TipoTransacao;
import com.tallyto.gestorfinanceiro.core.domain.exceptions.TransacaoException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/transacoes")
public class TransacaoController {

    private final TransacaoService transacaoService;

    public TransacaoController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    /**
     * Lista todas as transações com filtros opcionais
     */
    @GetMapping
    public ResponseEntity<Page<TransacaoDTO>> listarTransacoes(
            @RequestParam(required = false) Long contaId,
            @RequestParam(required = false) TipoTransacao tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long faturaId,
            @RequestParam(required = false) Long contaFixaId,
            @RequestParam(required = false) Long proventoId,
            Pageable pageable) {

        TransacaoFiltroDTO filtro = new TransacaoFiltroDTO(
                contaId, tipo, dataInicio, dataFim, categoriaId,
                faturaId, contaFixaId, proventoId);

        return ResponseEntity.ok(transacaoService.listarTransacoes(filtro, pageable));
    }

    /**
     * Busca uma transação por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransacaoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(transacaoService.toDTO(transacaoService.findById(id)));
    }

    /**
     * Cria uma nova transação
     */
    @PostMapping
    public ResponseEntity<TransacaoDTO> criarTransacao(@Valid @RequestBody TransacaoInputDTO transacaoDTO) {
        try {
            return new ResponseEntity<>(transacaoService.criarTransacao(transacaoDTO), HttpStatus.CREATED);
        } catch (TransacaoException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Remove uma transação por ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerTransacao(@PathVariable Long id) {
        transacaoService.removerTransacao(id);
        return ResponseEntity.noContent().build();
    }
}
