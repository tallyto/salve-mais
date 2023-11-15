package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.core.application.services.CartaoCreditoService;
import com.tallyto.gestorfinanceiro.core.domain.entities.CartaoCredito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cartao-credito")
public class CartaoCreditoController {

    @Autowired
    private CartaoCreditoService cartaoCreditoService;

    @PostMapping
    public CartaoCredito salvarCartaoCredito(@RequestBody CartaoCredito cartaoCredito) {
        return cartaoCreditoService.salvarCartaoCredito(cartaoCredito);
    }
}
