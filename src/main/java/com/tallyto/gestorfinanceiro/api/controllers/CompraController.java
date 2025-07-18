package com.tallyto.gestorfinanceiro.api.controllers;


import com.tallyto.gestorfinanceiro.api.dto.CompraDTO;
import com.tallyto.gestorfinanceiro.core.application.services.CompraService;
import com.tallyto.gestorfinanceiro.core.domain.entities.CartaoCredito;
import com.tallyto.gestorfinanceiro.core.domain.entities.Categoria;
import com.tallyto.gestorfinanceiro.core.domain.entities.Compra;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
    public Page<Compra> listarCompras(
            Pageable pageable,
            @RequestParam(value = "mes", required = false) Integer mes,
            @RequestParam(value = "ano", required = false) Integer ano) {
        
        if (mes != null && ano != null) {
            return compraService.listarComprasPorMesEAno(pageable, mes, ano);
        }
        
        return compraService.listarCompras(pageable);
    }

    @GetMapping("/cartao/{cartaoId}")
    public List<Compra> listarPorCartao(@PathVariable Long cartaoId, @RequestParam LocalDate dataVencimento) {
        return compraService.comprasPorCartaoAteData(cartaoId, dataVencimento);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Compra> buscarPorId(@PathVariable Long id) {
        try {
            Compra compra = compraService.buscarCompraPorId(id);
            return ResponseEntity.ok(compra);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Compra> atualizarCompra(@PathVariable Long id, @RequestBody CompraDTO compraDTO) {
        try {
            Compra compra = toEntity(compraDTO);
            compra.setId(id); // Garante que o ID está correto
            Compra compraAtualizada = compraService.atualizarCompra(id, compra);
            return ResponseEntity.ok(compraAtualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirCompra(@PathVariable Long id) {
        try {
            compraService.excluirCompra(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private Compra toEntity(CompraDTO compraDTO) {
        var compra = new Compra();
        
        // Se houver ID (caso de atualização), mantém o ID
        if (compraDTO.id() != null) {
            compra.setId(compraDTO.id());
        }
        
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
