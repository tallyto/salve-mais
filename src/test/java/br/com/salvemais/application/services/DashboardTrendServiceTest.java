package br.com.salvemais.application.services;

import br.com.salvemais.infrastructure.repositories.CompraDebitoRepository;
import br.com.salvemais.infrastructure.repositories.ContaFixaRepository;
import br.com.salvemais.infrastructure.repositories.ContaRepository;
import br.com.salvemais.infrastructure.repositories.FaturaRepository;
import br.com.salvemais.infrastructure.repositories.ProventoRepository;
import br.com.salvemais.infrastructure.repositories.ReservaEmergenciaRepository;
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
class DashboardTrendServiceTest {

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private ProventoRepository proventoRepository;

    @Mock
    private ContaFixaRepository contaFixaRepository;

    @Mock
    private FaturaRepository faturaRepository;

    @Mock
    private ReservaEmergenciaRepository reservaEmergenciaRepository;

    @Mock
    private CompraDebitoRepository compraDebitoRepository;

    @InjectMocks
    private DashboardTrendService dashboardTrendService;

    @Test
    void getMonthlyExpenseTrend_deveRetornarMesesSolicitados() {
        when(proventoRepository.findByDataBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());
        when(contaFixaRepository.findByVencimentoBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());
        when(faturaRepository.findByDataVencimentoBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());
        when(compraDebitoRepository.findByDataCompraBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());

        var resultado = dashboardTrendService.getMonthlyExpenseTrend(2);

        assertEquals(2, resultado.size());
        assertEquals(BigDecimal.ZERO, resultado.getFirst().valorDespesas());
        assertEquals(BigDecimal.ZERO, resultado.getFirst().valorReceitas());
    }

    @Test
    void getVariationData_deveRetornarQuatroIndicadoresSemReserva() {
        when(contaRepository.findAll()).thenReturn(List.of());
        when(proventoRepository.findByDataBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());
        when(contaFixaRepository.findByVencimentoBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());
        when(faturaRepository.findByDataVencimentoBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());
        when(compraDebitoRepository.findByDataCompraBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());
        when(reservaEmergenciaRepository.findAll()).thenReturn(List.of());

        var resultado = dashboardTrendService.getVariationData(null, null);

        assertEquals(4, resultado.size());
        assertEquals("Saldo Total", resultado.getFirst().metric());
    }
}
