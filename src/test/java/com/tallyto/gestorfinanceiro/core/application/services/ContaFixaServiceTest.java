package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.api.dto.ContaFixaRecorrenteDTO;
import com.tallyto.gestorfinanceiro.core.domain.entities.Categoria;
import com.tallyto.gestorfinanceiro.core.domain.entities.Conta;
import com.tallyto.gestorfinanceiro.core.domain.entities.ContaFixa;
import com.tallyto.gestorfinanceiro.core.infra.repositories.ContaFixaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContaFixaServiceTest {

    @Mock
    private ContaFixaRepository contaFixaRepository;

    @Mock
    private ContaService contaService;

    @Mock
    private CategoriaService categoriaService;

    @InjectMocks
    private ContaFixaService contaFixaService;

    private Conta conta;
    private Categoria categoria;

    @BeforeEach
    void setUp() {
        conta = new Conta();
        conta.setId(1L);

        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNome("Moradia");
    }

    @Test
    void deveCriarContasFixasRecorrentesMensais() {
        // Arrange
        ContaFixaRecorrenteDTO dto = new ContaFixaRecorrenteDTO(
                "Aluguel",
                1L,
                1L,
                LocalDate.of(2025, 1, 1),
                new BigDecimal("1200.00"),
                12,
                ContaFixaRecorrenteDTO.TipoRecorrencia.MENSAL,
                "Aluguel apartamento centro"
        );

        when(contaService.getOne(1L)).thenReturn(conta);
        when(categoriaService.buscaCategoriaPorId(1L)).thenReturn(categoria);
        when(contaFixaRepository.save(any(ContaFixa.class))).thenAnswer(invocation -> {
            ContaFixa contaFixa = invocation.getArgument(0);
            contaFixa.setId(System.currentTimeMillis()); // Simula ID gerado
            return contaFixa;
        });

        // Act
        List<ContaFixa> resultado = contaFixaService.criarContasFixasRecorrentes(dto);

        // Assert
        assertEquals(12, resultado.size());
        
        // Verificar primeira parcela
        ContaFixa primeiraParcela = resultado.get(0);
        assertEquals("Aluguel (1/12)", primeiraParcela.getNome());
        assertEquals(LocalDate.of(2025, 1, 1), primeiraParcela.getVencimento());
        assertEquals(new BigDecimal("1200.00"), primeiraParcela.getValor());
        assertEquals(conta, primeiraParcela.getConta());
        assertEquals(categoria, primeiraParcela.getCategoria());
        assertFalse(primeiraParcela.isPago());

        // Verificar última parcela
        ContaFixa ultimaParcela = resultado.get(11);
        assertEquals("Aluguel (12/12)", ultimaParcela.getNome());
        assertEquals(LocalDate.of(2025, 12, 1), ultimaParcela.getVencimento());

        // Verificar parcela do meio
        ContaFixa parcelaMeio = resultado.get(5);
        assertEquals("Aluguel (6/12)", parcelaMeio.getNome());
        assertEquals(LocalDate.of(2025, 6, 1), parcelaMeio.getVencimento());

        // Verificar que save foi chamado 12 vezes
        verify(contaFixaRepository, times(12)).save(any(ContaFixa.class));
    }

    @Test
    void deveCriarContasFixasRecorrentesTrimestral() {
        // Arrange
        ContaFixaRecorrenteDTO dto = new ContaFixaRecorrenteDTO(
                "Seguro Carro",
                1L,
                1L,
                LocalDate.of(2025, 3, 15),
                new BigDecimal("450.00"),
                4,
                ContaFixaRecorrenteDTO.TipoRecorrencia.TRIMESTRAL,
                null
        );

        when(contaService.getOne(1L)).thenReturn(conta);
        when(categoriaService.buscaCategoriaPorId(1L)).thenReturn(categoria);
        when(contaFixaRepository.save(any(ContaFixa.class))).thenAnswer(invocation -> {
            ContaFixa contaFixa = invocation.getArgument(0);
            contaFixa.setId(System.currentTimeMillis());
            return contaFixa;
        });

        // Act
        List<ContaFixa> resultado = contaFixaService.criarContasFixasRecorrentes(dto);

        // Assert
        assertEquals(4, resultado.size());
        
        // Verificar datas trimestrais
        assertEquals(LocalDate.of(2025, 3, 15), resultado.get(0).getVencimento());
        assertEquals(LocalDate.of(2025, 6, 15), resultado.get(1).getVencimento());
        assertEquals(LocalDate.of(2025, 9, 15), resultado.get(2).getVencimento());
        assertEquals(LocalDate.of(2025, 12, 15), resultado.get(3).getVencimento());

        verify(contaFixaRepository, times(4)).save(any(ContaFixa.class));
    }

    @Test
    void deveCriarContasFixasRecorrentesAnual() {
        // Arrange
        ContaFixaRecorrenteDTO dto = new ContaFixaRecorrenteDTO(
                "IPVA",
                1L,
                1L,
                LocalDate.of(2025, 1, 31),
                new BigDecimal("800.00"),
                3,
                ContaFixaRecorrenteDTO.TipoRecorrencia.ANUAL,
                "IPVA Civic"
        );

        when(contaService.getOne(1L)).thenReturn(conta);
        when(categoriaService.buscaCategoriaPorId(1L)).thenReturn(categoria);
        when(contaFixaRepository.save(any(ContaFixa.class))).thenAnswer(invocation -> {
            ContaFixa contaFixa = invocation.getArgument(0);
            contaFixa.setId(System.currentTimeMillis());
            return contaFixa;
        });

        // Act
        List<ContaFixa> resultado = contaFixaService.criarContasFixasRecorrentes(dto);

        // Assert
        assertEquals(3, resultado.size());
        
        // Verificar datas anuais
        assertEquals(LocalDate.of(2025, 1, 31), resultado.get(0).getVencimento());
        assertEquals(LocalDate.of(2026, 1, 31), resultado.get(1).getVencimento());
        assertEquals(LocalDate.of(2027, 1, 31), resultado.get(2).getVencimento());

        // Verificar nomes
        assertEquals("IPVA (1/3)", resultado.get(0).getNome());
        assertEquals("IPVA (2/3)", resultado.get(1).getNome());
        assertEquals("IPVA (3/3)", resultado.get(2).getNome());

        verify(contaFixaRepository, times(3)).save(any(ContaFixa.class));
    }

    @Test
    void deveLancarExcecaoQuandoContaNaoExiste() {
        // Arrange
        ContaFixaRecorrenteDTO dto = new ContaFixaRecorrenteDTO(
                "Teste",
                1L,
                999L, // Conta inexistente
                LocalDate.now(),
                new BigDecimal("100.00"),
                1,
                ContaFixaRecorrenteDTO.TipoRecorrencia.MENSAL,
                null
        );

        when(contaService.getOne(999L)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> contaFixaService.criarContasFixasRecorrentes(dto)
        );

        assertEquals("Conta não encontrada", exception.getMessage());
        verify(contaFixaRepository, never()).save(any(ContaFixa.class));
    }

    @Test
    void deveLancarExcecaoQuandoCategoriaNaoExiste() {
        // Arrange
        ContaFixaRecorrenteDTO dto = new ContaFixaRecorrenteDTO(
                "Teste",
                999L, // Categoria inexistente
                1L,
                LocalDate.now(),
                new BigDecimal("100.00"),
                1,
                ContaFixaRecorrenteDTO.TipoRecorrencia.MENSAL,
                null
        );

        when(contaService.getOne(1L)).thenReturn(conta);
        when(categoriaService.buscaCategoriaPorId(999L)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> contaFixaService.criarContasFixasRecorrentes(dto)
        );

        assertEquals("Categoria não encontrada", exception.getMessage());
        verify(contaFixaRepository, never()).save(any(ContaFixa.class));
    }

    @Test
    void deveVerificarTiposDeRecorrencia() {
        // Verificar enum TipoRecorrencia
        assertEquals(1, ContaFixaRecorrenteDTO.TipoRecorrencia.MENSAL.getMeses());
        assertEquals(2, ContaFixaRecorrenteDTO.TipoRecorrencia.BIMENSAL.getMeses());
        assertEquals(3, ContaFixaRecorrenteDTO.TipoRecorrencia.TRIMESTRAL.getMeses());
        assertEquals(6, ContaFixaRecorrenteDTO.TipoRecorrencia.SEMESTRAL.getMeses());
        assertEquals(12, ContaFixaRecorrenteDTO.TipoRecorrencia.ANUAL.getMeses());

        assertEquals("Mensal", ContaFixaRecorrenteDTO.TipoRecorrencia.MENSAL.getDescricao());
        assertEquals("Anual", ContaFixaRecorrenteDTO.TipoRecorrencia.ANUAL.getDescricao());
    }
}
