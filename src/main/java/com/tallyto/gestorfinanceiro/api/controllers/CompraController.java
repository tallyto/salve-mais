package com.tallyto.gestorfinanceiro.api.controllers;


import com.tallyto.gestorfinanceiro.api.dto.CompraDTO;
import com.tallyto.gestorfinanceiro.core.application.services.CompraService;
import com.tallyto.gestorfinanceiro.core.domain.entities.CartaoCredito;
import com.tallyto.gestorfinanceiro.core.domain.entities.Categoria;
import com.tallyto.gestorfinanceiro.core.domain.entities.Compra;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
    public Page<Compra> listarCompras(Pageable pageable) {
        return compraService.listarCompras(pageable);
    }

    @GetMapping("/cartao/{cartaoId}")
    public List<Compra> listarPorCartao(@PathVariable Long cartaoId, @RequestParam LocalDate dataVencimento) {
        return compraService.comprasPorCartaoAteData(cartaoId, dataVencimento);
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
