package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.core.application.services.ContaService;
import com.tallyto.gestorfinanceiro.core.domain.entities.Conta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/conta")
public class ContaController {
    @Autowired
    private ContaService contaService;

    @GetMapping
    public Page<Conta> listar (Pageable pageable) {
        return contaService.findAllAccounts(pageable);
    }

    @PostMapping
    public Conta criar(@RequestBody Conta conta) {
        return contaService.create(conta);
    }

    @PutMapping("/{id}")
    public Conta atualizar(@PathVariable Long id, @RequestBody Conta conta) {
        return contaService.update(id, conta);
    }
}
