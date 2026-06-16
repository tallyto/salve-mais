package br.com.salvemais.application.services;

import br.com.salvemais.domain.entities.Conta;
import br.com.salvemais.infrastructure.repositories.CompraDebitoRepository;
import br.com.salvemais.infrastructure.repositories.CategoriaRepository;
import br.com.salvemais.infrastructure.repositories.ContaFixaRepository;
import br.com.salvemais.infrastructure.repositories.ContaRepository;
import br.com.salvemais.infrastructure.repositories.FaturaRepository;
import br.com.salvemais.infrastructure.repositories.ParcelaRepository;
import br.com.salvemais.infrastructure.repositories.ProventoRepository;
import br.com.salvemais.infrastructure.repositories.ReservaEmergenciaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardOverviewServiceTest {

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProventoRepository proventoRepository;

    @Mock
    private ContaFixaRepository contaFixaRepository;

    @Mock
    private FaturaRepository faturaRepository;

    @Mock
    private ReservaEmergenciaRepository reservaEmergenciaRepository;

    @Mock
    private ParcelaRepository parcelaRepository;

    @Mock
    private CompraDebitoRepository compraDebitoRepository;

    @InjectMocks
    private DashboardOverviewService dashboardOverviewService;

    @Test
    void getSummary_deveRetornarResumoBasicoSemDados() {
        when(contaRepository.findAll()).thenReturn(List.of());
        when(contaRepository.count()).thenReturn(0L);
        when(categoriaRepository.count()).thenReturn(0L);
        when(proventoRepository.findByDataBetween(any(), any())).thenReturn(List.of());
        when(contaFixaRepository.findByVencimentoBetween(any(), any())).thenReturn(List.of());
        when(faturaRepository.findByDataVencimentoBetween(any(), any())).thenReturn(List.of());
        when(compraDebitoRepository.findByDataCompraBetween(any(), any())).thenReturn(List.of());
        when(reservaEmergenciaRepository.findAll()).thenReturn(List.of());
        when(parcelaRepository.findByDataVencimentoBetweenAtivas(any(), any())).thenReturn(List.of());
        when(parcelaRepository.findParcelasNaoPagasAtivas()).thenReturn(List.of());

        var resultado = dashboardOverviewService.getSummary(null, null);

        assertEquals(BigDecimal.ZERO, resultado.getSaldoTotal());
        assertEquals(0, resultado.getTotalContas());
        assertEquals(0, resultado.getTotalCategorias());
    }

    @Test
    void getBudgetRule_deveRetornarZerosSemMovimento() {
        when(proventoRepository.findByDataBetween(any(), any())).thenReturn(List.of());
        when(contaFixaRepository.findByVencimentoBetween(any(), any())).thenReturn(List.of());
        when(faturaRepository.findByDataVencimentoBetween(any(), any())).thenReturn(List.of());
        when(compraDebitoRepository.findByDataCompraBetween(any(), any())).thenReturn(List.of());

        var resultado = dashboardOverviewService.getBudgetRule();

        assertTrue(resultado.necessidadesIdeal().compareTo(BigDecimal.ZERO) == 0);
        assertTrue(resultado.desejosIdeal().compareTo(BigDecimal.ZERO) == 0);
        assertTrue(resultado.economiaIdeal().compareTo(BigDecimal.ZERO) == 0);
    }
}
