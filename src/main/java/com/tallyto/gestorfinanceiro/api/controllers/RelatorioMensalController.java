package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.api.dto.RelatorioMensalDTO;
import com.tallyto.gestorfinanceiro.core.application.services.RelatorioMensalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/relatorio-mensal")
@Tag(name = "Relatório Mensal", description = "Endpoints para geração de relatórios mensais detalhados")
public class RelatorioMensalController {

    @Autowired
    private RelatorioMensalService relatorioMensalService;

    @Operation(
            summary = "Gerar relatório mensal",
            description = "Gera um relatório completo com proventos, gastos fixos, cartões e outras despesas para um mês específico"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos")
    })
    @GetMapping("/{ano}/{mes}")
    public ResponseEntity<RelatorioMensalDTO> gerarRelatorio(
            @Parameter(description = "Ano do relatório (ex: 2024)", example = "2024")
            @PathVariable int ano,
            
            @Parameter(description = "Mês do relatório (1-12)", example = "12")
            @PathVariable int mes
    ) {
        if (mes < 1 || mes > 12) {
            return ResponseEntity.badRequest().build();
        }
        
        RelatorioMensalDTO relatorio = relatorioMensalService.gerarRelatorioMensal(ano, mes);
        return ResponseEntity.ok(relatorio);
    }

    @Operation(
            summary = "Gerar relatório do mês atual",
            description = "Gera um relatório completo para o mês atual"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso")
    })
    @GetMapping("/atual")
    public ResponseEntity<RelatorioMensalDTO> gerarRelatorioAtual() {
        RelatorioMensalDTO relatorio = relatorioMensalService.gerarRelatorioMensalAtual();
        return ResponseEntity.ok(relatorio);
    }

    @Operation(
            summary = "Obter contas fixas vencidas",
            description = "Retorna uma lista de contas fixas vencidas e não pagas até a data especificada"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de contas vencidas obtida com sucesso"),
            @ApiResponse(responseCode = "400", description = "Data inválida")
    })
    @GetMapping("/contas-vencidas")
    public ResponseEntity<List<RelatorioMensalDTO.ItemGastoFixoDTO>> obterContasVencidas(
            @Parameter(description = "Data de referência para verificar vencimentos (formato: yyyy-mm-dd)", example = "2024-12-31")
            @RequestParam(required = false) LocalDate dataReferencia
    ) {
        if (dataReferencia == null) {
            dataReferencia = LocalDate.now();
        }
        
        List<RelatorioMensalDTO.ItemGastoFixoDTO> contasVencidas = 
                relatorioMensalService.obterContasFixasVencidas(dataReferencia);
        
        return ResponseEntity.ok(contasVencidas);
    }

   
}
