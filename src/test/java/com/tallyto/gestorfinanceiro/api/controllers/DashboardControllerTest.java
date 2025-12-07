package com.tallyto.gestorfinanceiro.api.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.tallyto.gestorfinanceiro.api.dto.BudgetRuleDTO;
import com.tallyto.gestorfinanceiro.api.dto.CategoryExpenseDTO;
import com.tallyto.gestorfinanceiro.api.dto.DashboardSummaryDTO;
import com.tallyto.gestorfinanceiro.api.dto.MonthlyExpenseDTO;
import com.tallyto.gestorfinanceiro.api.dto.VariationDataDTO;
import com.tallyto.gestorfinanceiro.core.application.services.DashboardService;
import com.tallyto.gestorfinanceiro.core.application.services.ExportService;
import com.tallyto.gestorfinanceiro.testsupport.ControllerSliceTest;

@ControllerSliceTest(controllers = DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private ExportService exportService;

    @Test
    @DisplayName("GET /api/dashboard/summary retorna resumo do dashboard")
    void getSummary() throws Exception {
        DashboardSummaryDTO dto = new DashboardSummaryDTO(
                new BigDecimal("1000.00"),
                new BigDecimal("500.00"),
                new BigDecimal("300.00"),
                3,
                8,
                new BigDecimal("200.00"),
                new BigDecimal("400.00"),
                new BigDecimal("200.00"),
                new BigDecimal("100.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("10.0"),
                6
        );
        Mockito.when(dashboardService.getSummary(Mockito.isNull(), Mockito.isNull())).thenReturn(dto);

        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoTotal").value(1000.00))
                .andExpect(jsonPath("$.totalContas").value(3))
                .andExpect(jsonPath("$.totalCategorias").value(8));
    }

    @Test
    @DisplayName("GET /api/dashboard/expenses-by-category retorna lista por categoria")
    void getExpensesByCategory() throws Exception {
        List<CategoryExpenseDTO> list = List.of(
                new CategoryExpenseDTO(1L, "Alimentação", new BigDecimal("200.00"), 40.0),
                new CategoryExpenseDTO(2L, "Transporte", new BigDecimal("300.00"), 60.0)
        );
        Mockito.when(dashboardService.getExpensesByCategory(Mockito.isNull(), Mockito.isNull())).thenReturn(list);

        mockMvc.perform(get("/api/dashboard/expenses-by-category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].categoriaId").value(1))
                .andExpect(jsonPath("$[0].valorTotal").value(200.00));
    }

    @Test
    @DisplayName("GET /api/dashboard/budget-rule retorna regra 50/30/20")
    void getBudgetRule() throws Exception {
        BudgetRuleDTO dto = new BudgetRuleDTO(
                new BigDecimal("500.00"), new BigDecimal("300.00"), new BigDecimal("200.00"),
                new BigDecimal("450.00"), new BigDecimal("250.00"), new BigDecimal("150.00"),
                45.0, 25.0, 15.0,
                new BigDecimal("-50.00"), new BigDecimal("-50.00"), new BigDecimal("-50.00"),
                "OK", "OK", "OK"
        );
        Mockito.when(dashboardService.getBudgetRule()).thenReturn(dto);

        mockMvc.perform(get("/api/dashboard/budget-rule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.necessidadesIdeal").value(500.00))
                .andExpect(jsonPath("$.desejosIdeal").value(300.00))
                .andExpect(jsonPath("$.economiaIdeal").value(200.00));
    }

    @Test
    @DisplayName("GET /api/dashboard/monthly-trend retorna tendência mensal com parâmetro months")
    void getMonthlyTrend() throws Exception {
        List<MonthlyExpenseDTO> list = List.of(
                new MonthlyExpenseDTO("2025-08", LocalDate.of(2025,8,1), new BigDecimal("100.00"), new BigDecimal("200.00")),
                new MonthlyExpenseDTO("2025-09", LocalDate.of(2025,9,1), new BigDecimal("150.00"), new BigDecimal("250.00"))
        );
        Mockito.when(dashboardService.getMonthlyExpenseTrend(6)).thenReturn(list);

        mockMvc.perform(get("/api/dashboard/monthly-trend").param("months", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].mes").value("2025-08"));
    }

    @Test
    @DisplayName("GET /api/dashboard/monthly-trend/year/{year} retorna tendência por ano")
    void getMonthlyTrendByYear() throws Exception {
        List<MonthlyExpenseDTO> list = List.of(
                new MonthlyExpenseDTO("2024-01", LocalDate.of(2024,1,1), new BigDecimal("100.00"), new BigDecimal("200.00"))
        );
        Mockito.when(dashboardService.getMonthlyExpenseTrendByYear(2024)).thenReturn(list);

        mockMvc.perform(get("/api/dashboard/monthly-trend/year/{year}", 2024))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].mes").value("2024-01"));
    }

    @Test
    @DisplayName("GET /api/dashboard/variations retorna variações mensais")
    void getVariationData() throws Exception {
        List<VariationDataDTO> list = List.of(
                new VariationDataDTO("despesas", new BigDecimal("300.00"), new BigDecimal("200.00"), new BigDecimal("100.00"), new BigDecimal("50.00"), "UP", "arrow_upward")
        );
        Mockito.when(dashboardService.getVariationData(Mockito.isNull(), Mockito.isNull())).thenReturn(list);

        mockMvc.perform(get("/api/dashboard/variations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].metric").value("despesas"));
    }
}
