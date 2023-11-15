package com.tallyto.gestorfinanceiro.api.controllers;


import com.tallyto.gestorfinanceiro.core.application.services.CompraService;
import com.tallyto.gestorfinanceiro.core.domain.entities.Compra;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/compras")
public class CompraController {

    @Autowired
    private CompraService compraService;

    @PostMapping
    public Compra criarCompra(@RequestBody Compra compra) {
        return compraService.salvarCompra(compra);
    }
}
