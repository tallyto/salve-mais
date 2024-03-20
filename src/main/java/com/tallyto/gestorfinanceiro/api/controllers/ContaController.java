package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.core.application.services.ContaService;
import com.tallyto.gestorfinanceiro.core.domain.entities.Conta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conta")
public class ContaController {
    @Autowired
    private ContaService contaService;

    @GetMapping
    public List<Conta> listar () {
        return contaService.findAllAccounts();
    }

    @PostMapping
    public Conta criar(@RequestBody Conta conta) {
        return contaService.create(conta);
    }
}
