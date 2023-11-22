package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.core.application.services.FaturaService;
import com.tallyto.gestorfinanceiro.core.domain.entities.Fatura;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/faturas")
public class FaturaController {

    @Autowired
    private FaturaService faturaService;

    @PostMapping
    public void gerarFatura() {
        faturaService.gerarFatura(1L);
    }

    @GetMapping
    public List<Fatura> listarFaturas() {
       return faturaService.listar();
    }
}
