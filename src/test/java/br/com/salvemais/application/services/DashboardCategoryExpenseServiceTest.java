package br.com.salvemais.application.services;

import br.com.salvemais.domain.entities.Categoria;
import br.com.salvemais.domain.entities.CompraDebito;
import br.com.salvemais.domain.entities.ContaFixa;
import br.com.salvemais.domain.entities.Fatura;
import br.com.salvemais.infrastructure.repositories.CategoriaRepository;
import br.com.salvemais.infrastructure.repositories.CompraDebitoRepository;
import br.com.salvemais.infrastructure.repositories.ContaFixaRepository;
import br.com.salvemais.infrastructure.repositories.FaturaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardCategoryExpenseServiceTest {

    @Mock
    private ContaFixaRepository contaFixaRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private FaturaRepository faturaRepository;

    @Mock
    private CompraDebitoRepository compraDebitoRepository;

    @InjectMocks
    private DashboardCategoryExpenseService dashboardCategoryExpenseService;

    @Test
    void getExpensesByCategory_deveAgruparPorCategoria() {
        Categoria alimentacao = new Categoria();
        alimentacao.setId(1L);
        alimentacao.setNome("Alimentação");

        ContaFixa contaFixa = new ContaFixa();
        contaFixa.setCategoria(alimentacao);
        contaFixa.setValor(new BigDecimal("100.00"));

        CompraDebito compraDebito = new CompraDebito();
        compraDebito.setCategoria(alimentacao);
        compraDebito.setValor(new BigDecimal("50.00"));

        when(contaFixaRepository.findByVencimentoBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of(contaFixa));
        when(compraDebitoRepository.findByDataCompraBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of(compraDebito));
        when(faturaRepository.findByDataVencimentoBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());
        when(categoriaRepository.findByNome("Cartões de Crédito")).thenReturn(null);

        var resultado = dashboardCategoryExpenseService.getExpensesByCategory(null, null);

        assertEquals(1, resultado.size());
        assertEquals("Alimentação", resultado.getFirst().categoriaNome());
        assertEquals(new BigDecimal("150.00"), resultado.getFirst().valorTotal());
    }
}
