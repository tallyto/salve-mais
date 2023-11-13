package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.api.dto.ContaFixaDTO;
import com.tallyto.gestorfinanceiro.core.application.services.CategoriaService;
import com.tallyto.gestorfinanceiro.core.application.services.ContaFixaService;
import com.tallyto.gestorfinanceiro.core.domain.entities.Categoria;
import com.tallyto.gestorfinanceiro.core.domain.entities.ContaFixa;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contasfixas")
@Validated
public class ContaFixaController {

    private final ContaFixaService contaFixaService;
    private final CategoriaService categoriaService;

    @Autowired
    public ContaFixaController(ContaFixaService contaFixaService, CategoriaService categoriaService) {
        this.contaFixaService = contaFixaService;
        this.categoriaService = categoriaService;
    }

    @PostMapping
    public ResponseEntity<ContaFixa> criarContaFixa(@Valid @RequestBody ContaFixaDTO contaFixaDTO) {
        ContaFixa contaFixa = mapDTOToEntity(contaFixaDTO);
        ContaFixa contaFixaSalva = contaFixaService.salvarContaFixa(contaFixa);
        return ResponseEntity.ok(contaFixaSalva);
    }

    @GetMapping
    public ResponseEntity<List<ContaFixa>> listarContasFixas() {
        List<ContaFixa> contasFixas = contaFixaService.listarTodasContasFixas();
        return ResponseEntity.ok(contasFixas);
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<ContaFixa>> listarContasFixasPorCategoria(
            @PathVariable Long categoriaId
    ) {
        List<ContaFixa> contasFixas = contaFixaService.listarContaFixaPorCategoria(categoriaId);
        return ResponseEntity.ok(contasFixas);
    }

    @GetMapping("/vencidas")
    public ResponseEntity<List<ContaFixa>> listarContasFixasVencidasNaoPagas() {
        List<ContaFixa> contasFixas = contaFixaService.listarContasFixasVencidasNaoPagas();
        return ResponseEntity.ok(contasFixas);
    }

    // Outros métodos relacionados a contas fixas

    private ContaFixa mapDTOToEntity(ContaFixaDTO contaFixaDTO) {
        ContaFixa contaFixa = new ContaFixa();
        contaFixa.setNome(contaFixaDTO.nome());
        contaFixa.setVencimento(contaFixaDTO.vencimento());
        contaFixa.setValor(contaFixaDTO.valor());
        contaFixa.setPago(contaFixaDTO.pago());

        // Buscar a categoria por ID no banco de dados e associá-la à conta fixa
        Categoria categoria = categoriaService.buscaCategoriaPorId(contaFixaDTO.categoriaId());
        contaFixa.setCategoria(categoria);

        return contaFixa;
    }
}