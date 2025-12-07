package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.api.dto.RelatorioMensalDTO;
import com.tallyto.gestorfinanceiro.core.application.services.RelatorioMensalService;
import com.tallyto.gestorfinanceiro.testsupport.ControllerSliceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerSliceTest(controllers = RelatorioMensalController.class)
class RelatorioMensalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RelatorioMensalService relatorioMensalService;

    private RelatorioMensalDTO sampleRelatorio() {
        RelatorioMensalDTO.ResumoFinanceiroDTO resumo = new RelatorioMensalDTO.ResumoFinanceiroDTO(
                new BigDecimal("1000.00"), new BigDecimal("0.00"), new BigDecimal("300.00"),
                new BigDecimal("200.00"), new BigDecimal("100.00"), new BigDecimal("400.00"), new BigDecimal("400.00"), new BigDecimal("0.00")
        );
        return new RelatorioMensalDTO(
                "2025-09",
                LocalDate.of(2025, 9, 1),
                resumo,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                new BigDecimal("400.00"),
                new BigDecimal("0.00")
        );
    }

    @Test
    @DisplayName("GET /api/relatorio-mensal/{ano}/{mes} retorna 200 com relatório válido")
    void gerarRelatorio_ok() throws Exception {
        Mockito.when(relatorioMensalService.gerarRelatorioMensal(2025, 9)).thenReturn(sampleRelatorio());

        mockMvc.perform(get("/api/relatorio-mensal/{ano}/{mes}", 2025, 9))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mesReferencia").value("2025-09"))
                .andExpect(jsonPath("$.resumoFinanceiro.totalProventos").value(1000.00));
    }

    @Test
    @DisplayName("GET /api/relatorio-mensal/{ano}/{mes} retorna 400 para mês inválido")
    void gerarRelatorio_mesInvalido() throws Exception {
        mockMvc.perform(get("/api/relatorio-mensal/{ano}/{mes}", 2025, 13))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/relatorio-mensal/atual retorna relatório do mês atual")
    void gerarRelatorioAtual() throws Exception {
        Mockito.when(relatorioMensalService.gerarRelatorioMensalAtual()).thenReturn(sampleRelatorio());

        mockMvc.perform(get("/api/relatorio-mensal/atual"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mesReferencia").value("2025-09"));
    }

    @Test
    @DisplayName("GET /api/relatorio-mensal/contas-vencidas usa data informada")
    void obterContasVencidas_comData() throws Exception {
        RelatorioMensalDTO.ItemGastoFixoDTO item = new RelatorioMensalDTO.ItemGastoFixoDTO(
                1L, "Conta Luz", new BigDecimal("120.00"), LocalDate.of(2025,9,5), "Casa", true
        );
        Mockito.when(relatorioMensalService.obterContasFixasVencidas(LocalDate.of(2025,9,10)))
                .thenReturn(List.of(item));

        mockMvc.perform(get("/api/relatorio-mensal/contas-vencidas").param("dataReferencia", "2025-09-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("Conta Luz"));
    }

    @Test
    @DisplayName("GET /api/relatorio-mensal/contas-vencidas usa data atual quando não informada")
    void obterContasVencidas_semData() throws Exception {
        Mockito.when(relatorioMensalService.obterContasFixasVencidas(Mockito.any(LocalDate.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/relatorio-mensal/contas-vencidas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
