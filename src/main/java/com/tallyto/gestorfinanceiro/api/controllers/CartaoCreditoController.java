package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.core.application.services.CartaoCreditoService;
import com.tallyto.gestorfinanceiro.core.domain.entities.CartaoCredito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    
    @GetMapping("/{id}")
    public CartaoCredito buscarCartaoCredito(@PathVariable Long id) {
        return cartaoCreditoService.findOrFail(id);
    }

    @PostMapping
    public CartaoCredito salvarCartaoCredito(@RequestBody CartaoCredito cartaoCredito) {
        return cartaoCreditoService.salvarCartaoCredito(cartaoCredito);
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirCartaoCredito(@PathVariable Long id) {
        cartaoCreditoService.excluirCartaoCredito(id);
    }
}
