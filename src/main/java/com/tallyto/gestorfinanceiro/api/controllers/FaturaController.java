package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.core.application.services.FaturaService;
import com.tallyto.gestorfinanceiro.core.domain.entities.Fatura;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faturas")
public class FaturaController {

    @Autowired
    private FaturaService faturaService;

    @PostMapping("/{cardId}")
    public void gerarFatura(@PathVariable Long cardId) {
        faturaService.gerarFatura(cardId);
    }

    @GetMapping
    public List<Fatura> listarFaturas() {
       return faturaService.listar();
    }
}
