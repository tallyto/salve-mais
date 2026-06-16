package br.com.salvemais.web.api.controllers;

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

import br.com.salvemais.web.api.dto.CompraDebitoDTO;
import br.com.salvemais.application.services.CategoriaService;
import br.com.salvemais.application.services.CompraDebitoService;
import br.com.salvemais.domain.entities.Categoria;
import br.com.salvemais.domain.entities.CompraDebito;
import br.com.salvemais.domain.entities.Conta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

@Tag(name = "Compras de Débito", description = "Gestão de compras realizadas no débito")
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
    @Operation(summary = "Criar compra de débito")
    public ResponseEntity<CompraDebito> criarCompraDebito(@Valid @RequestBody CompraDebitoDTO compraDebitoDTO) {
        CompraDebito compraDebito = mapDTOToEntity(compraDebitoDTO);
        CompraDebito compraDebitoSalva = compraDebitoService.salvarCompraDebito(compraDebito);
        return ResponseEntity.ok(compraDebitoSalva);
    }

    @GetMapping
    @Operation(summary = "Listar compras de débito")
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
    @Operation(summary = "Listar compras de débito por categoria")
    public ResponseEntity<List<CompraDebito>> listarComprasPorCategoria(
            @PathVariable Long categoriaId
    ) {
        List<CompraDebito> compras = compraDebitoService.listarCompraPorCategoria(categoriaId);
        return ResponseEntity.ok(compras);
    }

    @GetMapping("/total")
    @Operation(summary = "Calcular total de compras de débito por período")
    public ResponseEntity<BigDecimal> calcularTotalPorPeriodo(
            @RequestParam LocalDate dataInicio,
            @RequestParam LocalDate dataFim) {
        BigDecimal total = compraDebitoService.calcularTotalPorPeriodo(dataInicio, dataFim);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar compra de débito por ID")
    public ResponseEntity<CompraDebito> buscarCompraDebitoPorId(@PathVariable Long id) {
        CompraDebito compraDebito = compraDebitoService.buscarCompraDebitoPorId(id);
        if (compraDebito == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(compraDebito);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar compra de débito")
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
    @Operation(summary = "Excluir compra de débito")
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
