package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.api.dto.ComparativoMensalDTO;
import com.tallyto.gestorfinanceiro.api.dto.RelatorioMensalDTO;
import com.tallyto.gestorfinanceiro.core.application.services.ExportService;
import com.tallyto.gestorfinanceiro.core.application.services.RelatorioMensalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/relatorio-mensal")
@Tag(name = "Relatório Mensal", description = "Endpoints para geração de relatórios mensais detalhados")
public class RelatorioMensalController {

    @Autowired
    private RelatorioMensalService relatorioMensalService;

    @Autowired
    private ExportService exportService;

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

    @Operation(
            summary = "Comparar dois meses",
            description = "Gera um comparativo detalhado entre dois meses, mostrando variações por categoria e destaques"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comparativo gerado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos")
    })
    @GetMapping("/comparativo/{anoAnterior}/{mesAnterior}/{anoAtual}/{mesAtual}")
    public ResponseEntity<ComparativoMensalDTO> gerarComparativo(
            @Parameter(description = "Ano do mês anterior (ex: 2024)", example = "2024")
            @PathVariable int anoAnterior,
            
            @Parameter(description = "Mês anterior (1-12)", example = "11")
            @PathVariable int mesAnterior,
            
            @Parameter(description = "Ano do mês atual (ex: 2024)", example = "2024")
            @PathVariable int anoAtual,
            
            @Parameter(description = "Mês atual (1-12)", example = "12")
            @PathVariable int mesAtual
    ) {
        if (mesAnterior < 1 || mesAnterior > 12 || mesAtual < 1 || mesAtual > 12) {
            return ResponseEntity.badRequest().build();
        }
        
        ComparativoMensalDTO comparativo = relatorioMensalService.gerarComparativoMensal(
            anoAnterior, mesAnterior, anoAtual, mesAtual
        );
        
        return ResponseEntity.ok(comparativo);
    }

    @Operation(
            summary = "Comparar mês atual com anterior",
            description = "Gera um comparativo entre o mês atual e o mês anterior automaticamente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comparativo gerado com sucesso")
    })
    @GetMapping("/comparativo/atual")
    public ResponseEntity<ComparativoMensalDTO> gerarComparativoAtual() {
        YearMonth mesAtual = YearMonth.now();
        YearMonth mesAnterior = mesAtual.minusMonths(1);
        
        ComparativoMensalDTO comparativo = relatorioMensalService.gerarComparativoMensal(
            mesAnterior.getYear(), mesAnterior.getMonthValue(),
            mesAtual.getYear(), mesAtual.getMonthValue()
        );
        
        return ResponseEntity.ok(comparativo);
    }

    @Operation(
            summary = "Exportar relatório mensal para Excel",
            description = "Gera um arquivo Excel com o relatório mensal completo"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Arquivo Excel gerado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos")
    })
    @GetMapping("/export/excel/{ano}/{mes}")
    public ResponseEntity<ByteArrayResource> exportarRelatorioParaExcel(
            @Parameter(description = "Ano do relatório (ex: 2024)", example = "2024")
            @PathVariable int ano,
            
            @Parameter(description = "Mês do relatório (1-12)", example = "12")
            @PathVariable int mes
    ) {
        try {
            if (mes < 1 || mes > 12) {
                return ResponseEntity.badRequest().build();
            }

            byte[] excelData = exportService.generateRelatorioMensalExcel(mes, ano);
            ByteArrayResource resource = new ByteArrayResource(excelData);

            String filename = String.format("relatorio-mensal-%02d-%04d.xlsx", mes, ano);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

   
}
