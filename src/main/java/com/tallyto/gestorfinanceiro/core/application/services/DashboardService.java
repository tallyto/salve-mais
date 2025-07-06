package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.api.dto.CategoryExpenseDTO;
import com.tallyto.gestorfinanceiro.api.dto.DashboardSummaryDTO;
import com.tallyto.gestorfinanceiro.api.dto.MonthlyExpenseDTO;
import com.tallyto.gestorfinanceiro.core.domain.entities.*;
import com.tallyto.gestorfinanceiro.core.infra.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private ProventoRepository proventoRepository;

    @Autowired
    private ContaFixaRepository contaFixaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private CompraRepository compraRepository;

    /**
     * Obtém o resumo financeiro para o dashboard
     * @return DashboardSummaryDTO com os dados de resumo
     */
    public DashboardSummaryDTO getSummary() {
        // Obtém saldo total das contas
        BigDecimal saldoTotal = contaRepository.findAll().stream()
                .map(Conta::getSaldo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Obtém o mês atual
        YearMonth mesAtual = YearMonth.now();
        LocalDate inicioMesAtual = mesAtual.atDay(1);
        LocalDate fimMesAtual = mesAtual.atEndOfMonth();

        // Obtém o mês anterior
        YearMonth mesAnterior = mesAtual.minusMonths(1);
        LocalDate inicioMesAnterior = mesAnterior.atDay(1);
        LocalDate fimMesAnterior = mesAnterior.atEndOfMonth();

        // Obtém receitas do mês atual
        BigDecimal receitasMes = proventoRepository.findByDataBetween(inicioMesAtual, fimMesAtual).stream()
                .map(Provento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Obtém despesas do mês atual (contas fixas)
        BigDecimal despesasFixasMes = contaFixaRepository.findAll().stream()
                .map(ContaFixa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Obtém despesas do mês atual (compras)
        BigDecimal despesasComprasMes = compraRepository.findByDataBetween(inicioMesAtual, fimMesAtual).stream()
                .map(Compra::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Total de despesas
        BigDecimal despesasMes = despesasFixasMes.add(despesasComprasMes);

        // Calcula o saldo do mês anterior
        BigDecimal receitasMesAnterior = proventoRepository.findByDataBetween(inicioMesAnterior, fimMesAnterior).stream()
                .map(Provento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal despesasMesAnterior = compraRepository.findByDataBetween(inicioMesAnterior, fimMesAnterior).stream()
                .map(Compra::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saldoMesAnterior = receitasMesAnterior.subtract(despesasMesAnterior);

        // Conta o número de contas e categorias
        long totalContas = contaRepository.count();
        long totalCategorias = categoriaRepository.count();

        return new DashboardSummaryDTO(
                saldoTotal,
                receitasMes,
                despesasMes,
                totalContas,
                totalCategorias,
                saldoMesAnterior
        );
    }

    /**
     * Obtém os dados de despesas por categoria para o gráfico de pizza
     * @return Lista de CategoryExpenseDTO com os dados por categoria
     */
    public List<CategoryExpenseDTO> getExpensesByCategory() {
        // Obtém o mês atual
        YearMonth mesAtual = YearMonth.now();
        LocalDate inicioMesAtual = mesAtual.atDay(1);
        LocalDate fimMesAtual = mesAtual.atEndOfMonth();

        // Busca todas as compras do mês atual
        List<Compra> compras = compraRepository.findByDataBetween(inicioMesAtual, fimMesAtual);

        // Agrupa as compras por categoria e soma os valores
        Map<Categoria, BigDecimal> gastosPorCategoria = new HashMap<>();

        for (Compra compra : compras) {
            Categoria categoria = compra.getCategoria();
            if (categoria != null) {
                gastosPorCategoria.put(
                        categoria,
                        gastosPorCategoria.getOrDefault(categoria, BigDecimal.ZERO).add(compra.getValor())
                );
            }
        }

        // Calcula o total de gastos
        BigDecimal totalGastos = gastosPorCategoria.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Converte para DTOs com percentuais
        return gastosPorCategoria.entrySet().stream()
                .map(entry -> {
                    double percentual = 0.0;
                    if (totalGastos.compareTo(BigDecimal.ZERO) > 0) {
                        percentual = entry.getValue()
                                .divide(totalGastos, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                .doubleValue();
                    }
                    
                    return new CategoryExpenseDTO(
                            entry.getKey().getId(),
                            entry.getKey().getNome(),
                            entry.getValue(),
                            percentual
                    );
                })
                .sorted(Comparator.comparing(CategoryExpenseDTO::valorTotal).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Obtém dados de tendência de gastos e receitas por mês
     * @param meses Número de meses para trás para buscar (padrão: 6)
     * @return Lista de MonthlyExpenseDTO com os dados mensais
     */
    public List<MonthlyExpenseDTO> getMonthlyExpenseTrend(int meses) {
        List<MonthlyExpenseDTO> result = new ArrayList<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale.getDefault());
        
        // Mês atual
        YearMonth mesAtual = YearMonth.now();
        
        // Busca dados para cada mês
        for (int i = meses - 1; i >= 0; i--) {
            YearMonth targetMonth = mesAtual.minusMonths(i);
            LocalDate inicioMes = targetMonth.atDay(1);
            LocalDate fimMes = targetMonth.atEndOfMonth();
            
            // Busca receitas do mês
            BigDecimal receitas = proventoRepository.findByDataBetween(inicioMes, fimMes).stream()
                    .map(Provento::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Busca despesas do mês (compras)
            BigDecimal despesasCompras = compraRepository.findByDataBetween(inicioMes, fimMes).stream()
                    .map(Compra::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Adiciona contas fixas às despesas
            BigDecimal despesasFixas = contaFixaRepository.findAll().stream()
                    .map(ContaFixa::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal despesasTotal = despesasCompras.add(despesasFixas);
            
            // Cria DTO para o mês
            result.add(new MonthlyExpenseDTO(
                    inicioMes.format(monthFormatter),
                    inicioMes,
                    despesasTotal,
                    receitas
            ));
        }
        
        return result;
    }
}
