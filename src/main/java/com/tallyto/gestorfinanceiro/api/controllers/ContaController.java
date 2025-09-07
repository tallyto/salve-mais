package com.tallyto.gestorfinanceiro.api.controllers;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tallyto.gestorfinanceiro.core.application.services.ContaService;
import com.tallyto.gestorfinanceiro.core.application.services.RendimentoService;
import com.tallyto.gestorfinanceiro.core.domain.entities.Conta;
import com.tallyto.gestorfinanceiro.core.domain.enums.TipoConta;

@RestController
@RequestMapping("/api/contas")
public class ContaController {
    @Autowired
    private ContaService contaService;

    @Autowired
    private RendimentoService rendimentoService;

    @GetMapping
    public Page<Conta> listar(Pageable pageable) {
        return contaService.findAllAccounts(pageable);
    }

    @GetMapping("/tipos")
    public ResponseEntity<List<TipoContaDTO>> listarTipos() {
        List<TipoContaDTO> tipos = Arrays.stream(TipoConta.values())
                .map(tipo -> new TipoContaDTO(tipo, tipo.getDescricao()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(tipos);
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<Conta>> listarPorTipo(@PathVariable TipoConta tipo) {
        return ResponseEntity.ok(contaService.findByTipo(tipo));
    }

    @GetMapping("/{id}/projetar-rendimento")
    public ResponseEntity<BigDecimal> projetarRendimento(
            @PathVariable Long id,
            @RequestParam(defaultValue = "12") int meses) {
        Conta conta = contaService.findOrFail(id);
        BigDecimal valorProjetado = rendimentoService.projetarRendimento(conta, meses);
        return ResponseEntity.ok(valorProjetado);
    }

    @PostMapping
    public Conta criar(@RequestBody Conta conta) {
        return contaService.create(conta);
    }

    @PutMapping("/{id}")
    public Conta atualizar(@PathVariable Long id, @RequestBody Conta conta) {
        return contaService.update(id, conta);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Conta> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(contaService.findOrFail(id));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        contaService.excluirConta(id);
        return ResponseEntity.noContent().build();
    }

    // Classe para representar o tipo de conta no endpoint de tipos
    record TipoContaDTO(TipoConta tipo, String descricao) {
    }
}
