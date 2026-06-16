package br.com.salvemais.web.api.controllers;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

import br.com.salvemais.web.api.dto.ContaFixaDTO;
import br.com.salvemais.web.api.dto.ContaFixaRecorrenteDTO;
import br.com.salvemais.application.services.CategoriaService;
import br.com.salvemais.application.services.ContaFixaService;
import br.com.salvemais.domain.entities.Categoria;
import br.com.salvemais.domain.entities.Conta;
import br.com.salvemais.domain.entities.ContaFixa;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

@Tag(name = "Contas Fixas", description = "Gestão de contas fixas e recorrentes")
@RestController
@RequestMapping("/api/contas/fixas")
@Validated
public class ContaFixaController {

    private final ContaFixaService contaFixaService;
    private final CategoriaService categoriaService;

    public ContaFixaController(ContaFixaService contaFixaService, CategoriaService categoriaService) {
        this.contaFixaService = contaFixaService;
        this.categoriaService = categoriaService;
    }

    @PostMapping
    @Operation(summary = "Criar conta fixa")
    public ResponseEntity<ContaFixa> criarContaFixa(@Valid @RequestBody ContaFixaDTO contaFixaDTO) {
        ContaFixa contaFixa = mapDTOToEntity(contaFixaDTO);
        ContaFixa contaFixaSalva = contaFixaService.salvarContaFixa(contaFixa);
        return ResponseEntity.ok(contaFixaSalva);
    }

    @PostMapping("/recorrente")
    @Operation(summary = "Criar contas fixas recorrentes")
    public ResponseEntity<List<ContaFixa>> criarContasFixasRecorrentes(
            @Valid @RequestBody ContaFixaRecorrenteDTO contaFixaRecorrenteDTO) {
        try {
            List<ContaFixa> contasFixasCriadas = contaFixaService.criarContasFixasRecorrentes(contaFixaRecorrenteDTO);
            return ResponseEntity.ok(contasFixasCriadas);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @Operation(summary = "Listar contas fixas")
    public Page<ContaFixa> listarContasFixas(
            Pageable pageable,
            @RequestParam(value = "mes", required = false) Integer mes,
            @RequestParam(value = "ano", required = false) Integer ano) {
        
        if (mes != null && ano != null) {
            return contaFixaService.listarContasFixasPorMesEAno(pageable, mes, ano);
        }
        
        return contaFixaService.listarTodasContasFixas(pageable);
    }

    @GetMapping("/categoria/{categoriaId}")
    @Operation(summary = "Listar contas fixas por categoria")
    public ResponseEntity<List<ContaFixa>> listarContasFixasPorCategoria(
            @PathVariable Long categoriaId
    ) {
        List<ContaFixa> contasFixas = contaFixaService.listarContaFixaPorCategoria(categoriaId);
        return ResponseEntity.ok(contasFixas);
    }

    @GetMapping("/total")
    @Operation(summary = "Calcular total de contas fixas não pagas")
    public ResponseEntity<BigDecimal> calcularTotalContasFixas() {
        BigDecimal total = contaFixaService.calcularTotalContasFixasNaoPagas();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/vencidas")
    @Operation(summary = "Listar contas fixas vencidas e não pagas")
    public ResponseEntity<List<ContaFixa>> listarContasFixasVencidasNaoPagas() {
        List<ContaFixa> contasFixas = contaFixaService.listarContasFixasVencidasNaoPagas();
        return ResponseEntity.ok(contasFixas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar conta fixa por ID")
    public ResponseEntity<ContaFixa> buscarContaFixaPorId(@PathVariable Long id) {
        ContaFixa contaFixa = contaFixaService.buscarContaFixaPorId(id);
        if (contaFixa == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(contaFixa);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar conta fixa")
    public ResponseEntity<ContaFixa> atualizarContaFixa(
            @PathVariable Long id,
            @Valid @RequestBody ContaFixaDTO contaFixaDTO) {

        ContaFixa contaFixaExistente = contaFixaService.buscarContaFixaPorId(id);
        if (contaFixaExistente == null) {
            return ResponseEntity.notFound().build();
        }

        // Atualiza os dados da conta fixa existente com os novos dados
        contaFixaExistente.setNome(contaFixaDTO.nome());
        contaFixaExistente.setVencimento(contaFixaDTO.vencimento());
        contaFixaExistente.setValor(contaFixaDTO.valor());
        contaFixaExistente.setPago(contaFixaDTO.pago());

        // Atualiza a categoria
        Categoria categoria = categoriaService.buscaCategoriaPorId(contaFixaDTO.categoriaId());
        contaFixaExistente.setCategoria(categoria);

        // Atualiza a conta
        Conta conta = new Conta();
        conta.setId(contaFixaDTO.contaId());
        contaFixaExistente.setConta(conta);

        // Salva a conta fixa atualizada
        ContaFixa contaFixaAtualizada = contaFixaService.salvarContaFixa(contaFixaExistente);
        return ResponseEntity.ok(contaFixaAtualizada);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir conta fixa")
    public ResponseEntity<Void> excluirContaFixa(@PathVariable Long id) {
        ContaFixa contaFixa = contaFixaService.buscarContaFixaPorId(id);
        if (contaFixa == null) {
            return ResponseEntity.notFound().build();
        }

        contaFixaService.deletarContaFixa(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Marca uma conta fixa como paga e cria a transação correspondente
     */
    @PostMapping("/{id}/pagar")
    @Operation(summary = "Marcar conta fixa como paga")
    public ResponseEntity<ContaFixa> pagarContaFixa(
            @PathVariable Long id,
            @RequestParam(required = false) String observacoes) {
        try {
            ContaFixa contaPaga = contaFixaService.pagarContaFixa(id, observacoes);
            return ResponseEntity.ok(contaPaga);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Recria uma despesa fixa para o próximo mês como não paga
     */
    @PostMapping("/{id}/recriar-proximo-mes")
    @Operation(summary = "Recriar despesa fixa para o próximo mês")
    public ResponseEntity<ContaFixa> recriarDespesaProximoMes(@PathVariable Long id) {
        try {
            ContaFixa novaDespesa = contaFixaService.recriarDespesaProximoMes(id);
            return ResponseEntity.ok(novaDespesa);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Exporta as contas fixas para Excel
     */
    @GetMapping("/exportar")
    @Operation(summary = "Exportar contas fixas para Excel")
    public ResponseEntity<ByteArrayResource> exportarContasFixasParaExcel(
            @RequestParam(value = "mes", required = false) Integer mes,
            @RequestParam(value = "ano", required = false) Integer ano) {
        try {
            ByteArrayOutputStream outputStream = contaFixaService.exportarParaExcel(mes, ano);
            
            ByteArrayResource resource = new ByteArrayResource(outputStream.toByteArray());
            
            String filename = mes != null && ano != null 
                ? "debitos_em_conta_%02d_%d.xlsx".formatted(mes, ano)
                : "debitos_em_conta.xlsx";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(resource.contentLength())
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
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

        Conta conta = new Conta();
        conta.setId(contaFixaDTO.contaId());
        contaFixa.setConta(conta);

        return contaFixa;
    }
}
