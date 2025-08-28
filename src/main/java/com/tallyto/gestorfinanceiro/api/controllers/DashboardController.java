package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.api.dto.BudgetRuleDTO;
import com.tallyto.gestorfinanceiro.api.dto.CategoryExpenseDTO;
import com.tallyto.gestorfinanceiro.api.dto.DashboardSummaryDTO;
import com.tallyto.gestorfinanceiro.api.dto.MonthlyExpenseDTO;
import com.tallyto.gestorfinanceiro.api.dto.VariationDataDTO;
import com.tallyto.gestorfinanceiro.core.application.services.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Endpoint para obter resumo financeiro para o dashboard
     * @return Resumo com saldos, receitas e despesas
     */
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    /**
     * Endpoint para obter despesas por categoria (para gráfico de pizza)
     * @return Lista de categorias com valores e percentuais
     */
    @GetMapping("/expenses-by-category")
    public ResponseEntity<List<CategoryExpenseDTO>> getExpensesByCategory() {
        return ResponseEntity.ok(dashboardService.getExpensesByCategory());
    }
    
    /**
     * Endpoint para obter a análise da regra 50/30/20
     * @return Dados da regra 50/30/20 com valores ideais e reais
     */
    @GetMapping("/budget-rule")
    public ResponseEntity<BudgetRuleDTO> getBudgetRule() {
        return ResponseEntity.ok(dashboardService.getBudgetRule());
    }

    /**
     * Endpoint para obter tendência de gastos e receitas por mês
     * @param months Número de meses para trás para buscar (opcional, padrão: 6)
     * @return Lista com valores por mês
     */
    @GetMapping("/monthly-trend")
    public ResponseEntity<List<MonthlyExpenseDTO>> getMonthlyTrend(
            @RequestParam(value = "months", defaultValue = "6") int months) {
        return ResponseEntity.ok(dashboardService.getMonthlyExpenseTrend(months));
    }

    /**
     * Endpoint para obter tendência mensal por ano específico
     * @param year Ano para buscar os dados
     * @return Lista com valores mensais do ano
     */
    @GetMapping("/monthly-trend/year/{year}")
    public ResponseEntity<List<MonthlyExpenseDTO>> getMonthlyTrendByYear(@PathVariable int year) {
        return ResponseEntity.ok(dashboardService.getMonthlyExpenseTrendByYear(year));
    }

    /**
     * Endpoint para obter dados de variação mensal
     * @return Lista com variações comparando mês atual com anterior
     */
    @GetMapping("/variations")
    public ResponseEntity<List<VariationDataDTO>> getVariationData() {
        return ResponseEntity.ok(dashboardService.getVariationData());
    }
}
