package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.api.dto.CompraParceladaRequestDTO;
import com.tallyto.gestorfinanceiro.api.dto.CompraParceladaResponseDTO;
import com.tallyto.gestorfinanceiro.api.dto.ErrorResponseDTO;
import com.tallyto.gestorfinanceiro.api.dto.ParcelaDTO;
import com.tallyto.gestorfinanceiro.core.application.services.CompraParceladaService;
import com.tallyto.gestorfinanceiro.core.domain.entities.CartaoCredito;
import com.tallyto.gestorfinanceiro.core.domain.entities.Categoria;
import com.tallyto.gestorfinanceiro.core.domain.entities.CompraParcelada;
import com.tallyto.gestorfinanceiro.core.domain.entities.Parcela;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/compras-parceladas")
public class CompraParceladaController {

    @Autowired
    private CompraParceladaService compraParceladaService;

    /**
     * Cria uma nova compra parcelada
     */
    @PostMapping
    public ResponseEntity<?> criarCompraParcelada(
            @Valid @RequestBody CompraParceladaRequestDTO request) {
        try {
            CompraParcelada compraParcelada = toEntity(request);
            CompraParcelada compraSalva = compraParceladaService.criarCompraParcelada(compraParcelada);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(CompraParceladaResponseDTO.fromEntity(compraSalva));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponseDTO.of(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponseDTO.of("Erro ao criar compra parcelada: " + e.getMessage(), 
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Lista todas as compras parceladas com paginação e filtros
     * @param cartaoId ID do cartão (opcional)
     * @param categoriaId ID da categoria (opcional)
     * @param apenasPendentes Filtrar apenas compras com parcelas pendentes (opcional)
     * @param pageable Configurações de paginação
     */
    @GetMapping
    public ResponseEntity<Page<CompraParceladaResponseDTO>> listarComprasParceladas(
            @RequestParam(required = false) Long cartaoId,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Boolean apenasPendentes,
            Pageable pageable) {
        Page<CompraParcelada> compras = compraParceladaService.listarComprasComFiltros(
            cartaoId, categoriaId, apenasPendentes, pageable);
        Page<CompraParceladaResponseDTO> response = compras.map(CompraParceladaResponseDTO::fromEntity);
        return ResponseEntity.ok(response);
    }

    /**
     * Lista compras parceladas por cartão
     */
    @GetMapping("/cartao/{cartaoId}")
    public ResponseEntity<Page<CompraParceladaResponseDTO>> listarPorCartao(
            @PathVariable Long cartaoId,
            Pageable pageable) {
        Page<CompraParcelada> compras = compraParceladaService.listarComprasParceladasPorCartao(cartaoId, pageable);
        Page<CompraParceladaResponseDTO> response = compras.map(CompraParceladaResponseDTO::fromEntity);
        return ResponseEntity.ok(response);
    }

    /**
     * Busca uma compra parcelada por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CompraParceladaResponseDTO> buscarPorId(@PathVariable Long id) {
        try {
            CompraParcelada compra = compraParceladaService.buscarPorId(id);
            return ResponseEntity.ok(CompraParceladaResponseDTO.fromEntity(compra));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Atualiza uma compra parcelada
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarCompraParcelada(
            @PathVariable Long id,
            @Valid @RequestBody CompraParceladaRequestDTO request) {
        try {
            CompraParcelada compraParcelada = toEntity(request);
            CompraParcelada compraAtualizada = compraParceladaService.atualizarCompraParcelada(id, compraParcelada);
            return ResponseEntity.ok(CompraParceladaResponseDTO.fromEntity(compraAtualizada));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponseDTO.of(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrada")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponseDTO.of(e.getMessage(), HttpStatus.NOT_FOUND.value()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponseDTO.of("Erro ao atualizar compra parcelada: " + e.getMessage(), 
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Lista parcelas de uma compra parcelada
     */
    @GetMapping("/{id}/parcelas")
    public ResponseEntity<List<ParcelaDTO>> listarParcelas(@PathVariable Long id) {
        List<Parcela> parcelas = compraParceladaService.listarParcelasPorCompra(id);
        List<ParcelaDTO> response = parcelas.stream()
                .map(ParcelaDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Busca parcelas por cartão e período
     */
    @GetMapping("/parcelas/cartao/{cartaoId}")
    public ResponseEntity<List<ParcelaDTO>> listarParcelasPorCartaoEPeriodo(
            @PathVariable Long cartaoId,
            @RequestParam LocalDate inicio,
            @RequestParam LocalDate fim) {
        List<Parcela> parcelas = compraParceladaService.listarParcelasPorCartaoEPeriodo(cartaoId, inicio, fim);
        List<ParcelaDTO> response = parcelas.stream()
                .map(ParcelaDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Marca uma parcela como paga
     */
    @PatchMapping("/parcelas/{parcelaId}/pagar")
    public ResponseEntity<ParcelaDTO> marcarParcelaComoPaga(@PathVariable Long parcelaId) {
        try {
            Parcela parcela = compraParceladaService.marcarParcelaComoPaga(parcelaId);
            return ResponseEntity.ok(ParcelaDTO.fromEntity(parcela));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Desmarca uma parcela como paga
     */
    @PatchMapping("/parcelas/{parcelaId}/despagar")
    public ResponseEntity<ParcelaDTO> desmarcarParcelaComoPaga(@PathVariable Long parcelaId) {
        try {
            Parcela parcela = compraParceladaService.desmarcarParcelaComoPaga(parcelaId);
            return ResponseEntity.ok(ParcelaDTO.fromEntity(parcela));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Exclui uma compra parcelada
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirCompraParcelada(@PathVariable Long id) {
        try {
            compraParceladaService.excluirCompraParcelada(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Lista parcelas não pagas de um cartão
     */
    @GetMapping("/parcelas/nao-pagas/cartao/{cartaoId}")
    public ResponseEntity<List<ParcelaDTO>> listarParcelasNaoPagas(@PathVariable Long cartaoId) {
        List<Parcela> parcelas = compraParceladaService.listarParcelasNaoPagas(cartaoId);
        List<ParcelaDTO> response = parcelas.stream()
                .map(ParcelaDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Lista parcelas vencidas
     */
    @GetMapping("/parcelas/vencidas")
    public ResponseEntity<List<ParcelaDTO>> listarParcelasVencidas() {
        List<Parcela> parcelas = compraParceladaService.listarParcelasVencidas();
        List<ParcelaDTO> response = parcelas.stream()
                .map(ParcelaDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Converte DTO para entidade
     */
    private CompraParcelada toEntity(CompraParceladaRequestDTO dto) {
        CompraParcelada compraParcelada = new CompraParcelada();
        compraParcelada.setDescricao(dto.descricao());
        compraParcelada.setValorTotal(dto.valorTotal());
        compraParcelada.setDataCompra(dto.dataCompra());
        compraParcelada.setParcelaInicial(dto.parcelaInicial());
        compraParcelada.setTotalParcelas(dto.totalParcelas());

        CartaoCredito cartao = new CartaoCredito();
        cartao.setId(dto.cartaoId());
        compraParcelada.setCartaoCredito(cartao);

        if (dto.categoriaId() != null) {
            Categoria categoria = new Categoria();
            categoria.setId(dto.categoriaId());
            compraParcelada.setCategoria(categoria);
        }

        return compraParcelada;
    }
}
