package com.tallyto.gestorfinanceiro.api.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tallyto.gestorfinanceiro.api.dto.CompraDebitoDTO;
import com.tallyto.gestorfinanceiro.core.application.services.CategoriaService;
import com.tallyto.gestorfinanceiro.core.application.services.CompraDebitoService;
import com.tallyto.gestorfinanceiro.core.domain.entities.Categoria;
import com.tallyto.gestorfinanceiro.core.domain.entities.CompraDebito;
import com.tallyto.gestorfinanceiro.core.domain.entities.Conta;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/compras/debito")
@Validated
public class CompraDebitoController {

    private final CompraDebitoService compraDebitoService;
    private final CategoriaService categoriaService;

    public CompraDebitoController(CompraDebitoService compraDebitoService, CategoriaService categoriaService) {
        this.compraDebitoService = compraDebitoService;
        this.categoriaService = categoriaService;
    }

    @PostMapping
    public ResponseEntity<CompraDebito> criarCompraDebito(@Valid @RequestBody CompraDebitoDTO compraDebitoDTO) {
        CompraDebito compraDebito = mapDTOToEntity(compraDebitoDTO);
        CompraDebito compraDebitoSalva = compraDebitoService.salvarCompraDebito(compraDebito);
        return ResponseEntity.ok(compraDebitoSalva);
    }

    @GetMapping
    public Page<CompraDebito> listarCompras(
            Pageable pageable,
            @RequestParam(value = "mes", required = false) Integer mes,
            @RequestParam(value = "ano", required = false) Integer ano) {
        if (mes != null && ano != null) {
            return compraDebitoService.listarComprasPorMesEAno(pageable, mes, ano);
        }
        return compraDebitoService.listarTodasCompras(pageable);
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<CompraDebito>> listarComprasPorCategoria(
            @PathVariable Long categoriaId
    ) {
        List<CompraDebito> compras = compraDebitoService.listarCompraPorCategoria(categoriaId);
        return ResponseEntity.ok(compras);
    }

    @GetMapping("/total")
    public ResponseEntity<BigDecimal> calcularTotalPorPeriodo(
            @RequestParam LocalDate dataInicio,
            @RequestParam LocalDate dataFim) {
        BigDecimal total = compraDebitoService.calcularTotalPorPeriodo(dataInicio, dataFim);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompraDebito> buscarCompraDebitoPorId(@PathVariable Long id) {
        CompraDebito compraDebito = compraDebitoService.buscarCompraDebitoPorId(id);
        if (compraDebito == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(compraDebito);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompraDebito> atualizarCompraDebito(
            @PathVariable Long id,
            @Valid @RequestBody CompraDebitoDTO compraDebitoDTO) {
        CompraDebito compraDebitoExistente = compraDebitoService.buscarCompraDebitoPorId(id);
        if (compraDebitoExistente == null) {
            return ResponseEntity.notFound().build();
        }

        compraDebitoExistente.setNome(compraDebitoDTO.nome());
        compraDebitoExistente.setObservacoes(compraDebitoDTO.observacoes());

        // Atualiza a categoria
        if (compraDebitoDTO.categoriaId() != null) {
            Categoria categoria = categoriaService.buscaCategoriaPorId(compraDebitoDTO.categoriaId());
            compraDebitoExistente.setCategoria(categoria);
        }

        // Atualiza a compra (não permite alterar valor)
        CompraDebito compraDebitoAtualizada = compraDebitoService.atualizarCompraDebito(id, compraDebitoExistente);
        return ResponseEntity.ok(compraDebitoAtualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirCompraDebito(@PathVariable Long id) {
        CompraDebito compraDebito = compraDebitoService.buscarCompraDebitoPorId(id);
        if (compraDebito == null) {
            return ResponseEntity.notFound().build();
        }

        compraDebitoService.deletarCompraDebito(id);
        return ResponseEntity.noContent().build();
    }

    private CompraDebito mapDTOToEntity(CompraDebitoDTO compraDebitoDTO) {
        CompraDebito compraDebito = new CompraDebito();
        compraDebito.setNome(compraDebitoDTO.nome());
        compraDebito.setDataCompra(compraDebitoDTO.dataCompra());
        compraDebito.setValor(compraDebitoDTO.valor());
        compraDebito.setObservacoes(compraDebitoDTO.observacoes());

        // Buscar a categoria por ID no banco de dados e associá-la
        if (compraDebitoDTO.categoriaId() != null) {
            Categoria categoria = categoriaService.buscaCategoriaPorId(compraDebitoDTO.categoriaId());
            compraDebito.setCategoria(categoria);
        }

        Conta conta = new Conta();
        conta.setId(compraDebitoDTO.contaId());
        compraDebito.setConta(conta);

        return compraDebito;
    }
}
