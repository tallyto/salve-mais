package br.com.salvemais.application.services;

import br.com.salvemais.domain.entities.Categoria;
import br.com.salvemais.domain.entities.Compra;
import br.com.salvemais.domain.entities.CompraDebito;
import br.com.salvemais.domain.entities.ContaFixa;
import br.com.salvemais.domain.entities.Fatura;
import br.com.salvemais.infrastructure.repositories.CategoriaRepository;
import br.com.salvemais.infrastructure.repositories.CompraDebitoRepository;
import br.com.salvemais.infrastructure.repositories.ContaFixaRepository;
import br.com.salvemais.infrastructure.repositories.FaturaRepository;
import br.com.salvemais.web.api.dto.CategoryExpenseDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardCategoryExpenseService {

    private final ContaFixaRepository contaFixaRepository;
    private final CategoriaRepository categoriaRepository;
    private final FaturaRepository faturaRepository;
    private final CompraDebitoRepository compraDebitoRepository;

    public DashboardCategoryExpenseService(ContaFixaRepository contaFixaRepository,
                                           CategoriaRepository categoriaRepository,
                                           FaturaRepository faturaRepository,
                                           CompraDebitoRepository compraDebitoRepository) {
        this.contaFixaRepository = contaFixaRepository;
        this.categoriaRepository = categoriaRepository;
        this.faturaRepository = faturaRepository;
        this.compraDebitoRepository = compraDebitoRepository;
    }

    public List<CategoryExpenseDTO> getExpensesByCategory(Integer mes, Integer ano) {
        YearMonth mesAtual = mes != null && ano != null ? YearMonth.of(ano, mes) : YearMonth.now();
        LocalDate inicioMesAtual = mesAtual.atDay(1);
        LocalDate fimMesAtual = mesAtual.atEndOfMonth();

        Map<Categoria, BigDecimal> gastosPorCategoria = new HashMap<>();

        for (ContaFixa conta : contaFixaRepository.findByVencimentoBetween(inicioMesAtual, fimMesAtual)) {
            addExpense(gastosPorCategoria, conta.getCategoria(), conta.getValor());
        }

        for (CompraDebito compra : compraDebitoRepository.findByDataCompraBetween(inicioMesAtual, fimMesAtual)) {
            addExpense(gastosPorCategoria, compra.getCategoria(), compra.getValor());
        }

        Categoria categoriaCartao = categoriaRepository.findByNome("Cartões de Crédito");
        for (Fatura fatura : faturaRepository.findByDataVencimentoBetween(inicioMesAtual, fimMesAtual)) {
            if (categoriaCartao != null) {
                addExpense(gastosPorCategoria, categoriaCartao, fatura.getValorTotal());
            } else {
                for (Compra compra : fatura.getCompras()) {
                    addExpense(gastosPorCategoria, compra.getCategoria(), compra.getValor());
                }
            }
        }

        BigDecimal totalGastos = gastosPorCategoria.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return gastosPorCategoria.entrySet().stream()
                .map(entry -> new CategoryExpenseDTO(
                        entry.getKey().getId(),
                        entry.getKey().getNome(),
                        entry.getValue(),
                        calcularPercentual(entry.getValue(), totalGastos)
                ))
                .sorted(Comparator.comparing(CategoryExpenseDTO::valorTotal).reversed())
                .collect(Collectors.toList());
    }

    private void addExpense(Map<Categoria, BigDecimal> gastosPorCategoria, Categoria categoria, BigDecimal valor) {
        if (categoria == null) {
            return;
        }
        gastosPorCategoria.put(
                categoria,
                gastosPorCategoria.getOrDefault(categoria, BigDecimal.ZERO).add(valor)
        );
    }

    private double calcularPercentual(BigDecimal valor, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }
        return valor.divide(total, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }
}
