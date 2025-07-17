package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.api.dto.RelatorioMensalDTO;
import com.tallyto.gestorfinanceiro.core.application.services.RelatorioMensalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RelatorioMensalController.class)
public class RelatorioMensalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RelatorioMensalService relatorioMensalService;

    @Test
    public void testGerarRelatorioMensal() throws Exception {
        // Arrange
        RelatorioMensalDTO.ResumoFinanceiroDTO resumo = new RelatorioMensalDTO.ResumoFinanceiroDTO(
                new BigDecimal("7900.00"),
                BigDecimal.ZERO,
                new BigDecimal("1344.91"),
                new BigDecimal("2817.10"),
                new BigDecimal("1589.01"),
                new BigDecimal("2148.98"),
                new BigDecimal("5751.02")
        );

        List<RelatorioMensalDTO.ItemProventoDTO> proventos = List.of(
                new RelatorioMensalDTO.ItemProventoDTO(1L, "Salário Tallyto", new BigDecimal("5800.00"), LocalDate.now(), "Tallyto"),
                new RelatorioMensalDTO.ItemProventoDTO(2L, "Bolsa Kamila", new BigDecimal("2100.00"), LocalDate.now(), "Kamila")
        );

        RelatorioMensalDTO mockRelatorio = new RelatorioMensalDTO(
                "dezembro 2024",
                LocalDate.of(2024, 12, 1),
                resumo,
                proventos,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new BigDecimal("2148.98"),
                new BigDecimal("5751.02")
        );

        when(relatorioMensalService.gerarRelatorioMensal(anyInt(), anyInt())).thenReturn(mockRelatorio);

        // Act & Assert
        mockMvc.perform(get("/api/relatorio-mensal/2024/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mesReferencia").value("dezembro 2024"))
                .andExpect(jsonPath("$.resumoFinanceiro.totalProventos").value(7900.00))
                .andExpect(jsonPath("$.resumoFinanceiro.saldoFinal").value(2148.98))
                .andExpect(jsonPath("$.proventos[0].descricao").value("Salário Tallyto"))
                .andExpect(jsonPath("$.proventos[0].valor").value(5800.00))
                .andExpect(jsonPath("$.proventos[1].descricao").value("Bolsa Kamila"))
                .andExpect(jsonPath("$.proventos[1].valor").value(2100.00));
    }

    @Test
    public void testGerarRelatorioAtual() throws Exception {
        // Arrange
        RelatorioMensalDTO.ResumoFinanceiroDTO resumo = new RelatorioMensalDTO.ResumoFinanceiroDTO(
                new BigDecimal("7900.00"),
                BigDecimal.ZERO,
                new BigDecimal("1344.91"),
                new BigDecimal("2817.10"),
                new BigDecimal("1589.01"),
                new BigDecimal("2148.98"),
                new BigDecimal("5751.02")
        );

        RelatorioMensalDTO mockRelatorio = new RelatorioMensalDTO(
                "dezembro 2024",
                LocalDate.now(),
                resumo,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new BigDecimal("2148.98"),
                new BigDecimal("5751.02")
        );

        when(relatorioMensalService.gerarRelatorioMensalAtual()).thenReturn(mockRelatorio);

        // Act & Assert
        mockMvc.perform(get("/api/relatorio-mensal/atual"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mesReferencia").value("dezembro 2024"))
                .andExpect(jsonPath("$.resumoFinanceiro.totalProventos").value(7900.00));
    }

    @Test
    public void testObterContasVencidas() throws Exception {
        // Arrange
        List<RelatorioMensalDTO.ItemGastoFixoDTO> contasVencidas = List.of(
                new RelatorioMensalDTO.ItemGastoFixoDTO(1L, "Aluguel", new BigDecimal("1562.57"), LocalDate.now(), "Moradia", false),
                new RelatorioMensalDTO.ItemGastoFixoDTO(2L, "Internet", new BigDecimal("149.90"), LocalDate.now(), "Utilidades", false)
        );

        when(relatorioMensalService.obterContasFixasVencidas(any(LocalDate.class))).thenReturn(contasVencidas);

        // Act & Assert
        mockMvc.perform(get("/api/relatorio-mensal/contas-vencidas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Aluguel"))
                .andExpect(jsonPath("$[0].valor").value(1562.57))
                .andExpect(jsonPath("$[1].nome").value("Internet"))
                .andExpect(jsonPath("$[1].valor").value(149.90));
    }

    @Test
    public void testRelatorioComMesInvalido() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/relatorio-mensal/2024/13"))
                .andExpect(status().isBadRequest());
        
        mockMvc.perform(get("/api/relatorio-mensal/2024/0"))
                .andExpect(status().isBadRequest());
    }

}
