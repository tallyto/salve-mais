package com.tallyto.gestorfinanceiro.api.controllers;


import com.tallyto.gestorfinanceiro.api.dto.CompraDTO;
import com.tallyto.gestorfinanceiro.core.application.services.CompraService;
import com.tallyto.gestorfinanceiro.core.domain.entities.CartaoCredito;
import com.tallyto.gestorfinanceiro.core.domain.entities.Categoria;
import com.tallyto.gestorfinanceiro.core.domain.entities.Compra;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/compras")
public class CompraController {

    @Autowired
    private CompraService compraService;

    @PostMapping
    public Compra criarCompra(@RequestBody CompraDTO compra) {
        return compraService.salvarCompra(toEntity(compra));
    }

    @GetMapping

    public List<Compra> listarCompras() {
        return compraService.listarCompras();
    }

    private Compra toEntity(CompraDTO compraDTO) {
        var compra = new Compra();
        compra.setData(compraDTO.data());
        compra.setValor(compraDTO.valor());
        compra.setDescricao(compraDTO.descricao());

        var cartao = new CartaoCredito();
        cartao.setId(compraDTO.cartaoId());

        var categoria = new Categoria();
        categoria.setId(compraDTO.categoriaId());

        compra.setCategoria(categoria);
        compra.setCartaoCredito(cartao);
        return compra;
    }
}
