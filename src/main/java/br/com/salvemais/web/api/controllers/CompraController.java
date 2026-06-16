package br.com.salvemais.web.api.controllers;


import br.com.salvemais.web.api.dto.CompraDTO;
import br.com.salvemais.application.services.CompraService;
import br.com.salvemais.domain.entities.CartaoCredito;
import br.com.salvemais.domain.entities.Categoria;
import br.com.salvemais.domain.entities.Compra;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Compras", description = "Gestão de compras no cartão de crédito")
@RestController
@RequestMapping("api/compras")
public class CompraController {

    @Autowired
    private CompraService compraService;

    @PostMapping
    @Operation(summary = "Criar compra")
    public Compra criarCompra(@RequestBody CompraDTO compra) {
        return compraService.salvarCompra(toEntity(compra));
    }

    @GetMapping
    @Operation(summary = "Listar compras com filtros opcionais")
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
    @Operation(summary = "Listar compras de um cartão até uma data")
    public List<Compra> listarPorCartao(@PathVariable Long cartaoId, @RequestParam LocalDate dataVencimento) {
        return compraService.comprasPorCartaoAteData(cartaoId, dataVencimento);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar compra por ID")
    public ResponseEntity<Compra> buscarPorId(@PathVariable Long id) {
        try {
            Compra compra = compraService.buscarCompraPorId(id);
            return ResponseEntity.ok(compra);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar compra")
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
    @Operation(summary = "Excluir compra")
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
