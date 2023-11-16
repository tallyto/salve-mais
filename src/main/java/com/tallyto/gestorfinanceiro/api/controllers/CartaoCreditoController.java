package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.core.application.services.CartaoCreditoService;
import com.tallyto.gestorfinanceiro.core.domain.entities.CartaoCredito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cartao-credito")
public class CartaoCreditoController {

    @Autowired
    private CartaoCreditoService cartaoCreditoService;

    @GetMapping
    public List<CartaoCredito> listarCartaoCredito() {
        return cartaoCreditoService.listarCartoesCredito();
    }

    @PostMapping
    public CartaoCredito salvarCartaoCredito(@RequestBody CartaoCredito cartaoCredito) {
        return cartaoCreditoService.salvarCartaoCredito(cartaoCredito);
    }
}
