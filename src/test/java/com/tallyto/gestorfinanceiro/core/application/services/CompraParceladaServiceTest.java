package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.core.domain.entities.CartaoCredito;
import com.tallyto.gestorfinanceiro.core.domain.entities.Categoria;
import com.tallyto.gestorfinanceiro.core.domain.entities.CompraParcelada;
import com.tallyto.gestorfinanceiro.core.domain.entities.Parcela;
import com.tallyto.gestorfinanceiro.core.infra.repositories.CompraParceladaRepository;
import com.tallyto.gestorfinanceiro.core.infra.repositories.ParcelaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompraParceladaServiceTest {

    @Mock
    private CompraParceladaRepository compraParceladaRepository;

    @Mock
    private ParcelaRepository parcelaRepository;

    @Mock
    private CartaoCreditoService cartaoCreditoService;

    @Mock
    private CategoriaService categoriaService;

    @InjectMocks
    private CompraParceladaService compraParceladaService;

    private CartaoCredito cartaoCredito;
    private Categoria categoria;
    private CompraParcelada compraParcelada;

    @BeforeEach
    void setUp() {
        cartaoCredito = new CartaoCredito();
        cartaoCredito.setId(1L);
        cartaoCredito.setNome("Cartão Teste");

        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNome("Eletrônicos");

        compraParcelada = new CompraParcelada();
        compraParcelada.setDescricao("Notebook");
        compraParcelada.setValorTotal(new BigDecimal("3000.00"));
        compraParcelada.setDataCompra(LocalDate.now());
        compraParcelada.setParcelaInicial(1);
        compraParcelada.setTotalParcelas(10);
        compraParcelada.setCartaoCredito(cartaoCredito);
        compraParcelada.setCategoria(categoria);
        compraParcelada.setParcelas(new ArrayList<>());
    }

    @Test
    @DisplayName("Deve criar compra parcelada com sucesso - 10 parcelas de 1/10")
    void deveCriarCompraParceladaComSucesso() {
        // Arrange
        when(cartaoCreditoService.findOrFail(1L)).thenReturn(cartaoCredito);
        when(categoriaService.buscaCategoriaPorId(1L)).thenReturn(categoria);
        when(compraParceladaRepository.save(any(CompraParcelada.class))).thenReturn(compraParcelada);
        when(parcelaRepository.save(any(Parcela.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CompraParcelada resultado = compraParceladaService.criarCompraParcelada(compraParcelada);

        // Assert
        assertNotNull(resultado);
        assertEquals("Notebook", resultado.getDescricao());
        assertEquals(10, resultado.getParcelas().size());
        verify(compraParceladaRepository, times(1)).save(any(CompraParcelada.class));
        verify(parcelaRepository, times(10)).save(any(Parcela.class));
    }

    @Test
    @DisplayName("Deve criar compra parcelada começando da parcela 2 de 5 (3 parcelas restantes)")
    void deveCriarCompraParceladaComParcelaInicial() {
        // Arrange
        compraParcelada.setParcelaInicial(2);
        compraParcelada.setTotalParcelas(5);
        compraParcelada.setValorTotal(new BigDecimal("900.00")); // 300 por parcela

        when(cartaoCreditoService.findOrFail(1L)).thenReturn(cartaoCredito);
        when(categoriaService.buscaCategoriaPorId(1L)).thenReturn(categoria);
        when(compraParceladaRepository.save(any(CompraParcelada.class))).thenReturn(compraParcelada);
        when(parcelaRepository.save(any(Parcela.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CompraParcelada resultado = compraParceladaService.criarCompraParcelada(compraParcelada);

        // Assert
        assertNotNull(resultado);
        assertEquals(4, resultado.getParcelas().size()); // Parcelas 2, 3, 4, 5
        
        // Verifica se as parcelas foram criadas corretamente
        assertEquals(2, resultado.getParcelas().get(0).getNumeroParcela());
        assertEquals(3, resultado.getParcelas().get(1).getNumeroParcela());
        assertEquals(4, resultado.getParcelas().get(2).getNumeroParcela());
        assertEquals(5, resultado.getParcelas().get(3).getNumeroParcela());
        
        verify(parcelaRepository, times(4)).save(any(Parcela.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando parcela inicial for menor que 1")
    void deveLancarExcecaoQuandoParcelaInicialInvalida() {
        // Arrange
        compraParcelada.setParcelaInicial(0);
        when(cartaoCreditoService.findOrFail(1L)).thenReturn(cartaoCredito);
        when(categoriaService.buscaCategoriaPorId(1L)).thenReturn(categoria);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            compraParceladaService.criarCompraParcelada(compraParcelada);
        });
    }

    @Test
    @DisplayName("Deve lançar exceção quando parcela inicial for maior que total de parcelas")
    void deveLancarExcecaoQuandoParcelaInicialMaiorQueTotalParcelas() {
        // Arrange
        compraParcelada.setParcelaInicial(11);
        compraParcelada.setTotalParcelas(10);
        when(cartaoCreditoService.findOrFail(1L)).thenReturn(cartaoCredito);
        when(categoriaService.buscaCategoriaPorId(1L)).thenReturn(categoria);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            compraParceladaService.criarCompraParcelada(compraParcelada);
        });
    }

    @Test
    @DisplayName("Deve marcar parcela como paga")
    void deveMarcarParcelaComoPaga() {
        // Arrange
        Parcela parcela = new Parcela();
        parcela.setId(1L);
        parcela.setPaga(false);
        
        when(parcelaRepository.findById(1L)).thenReturn(Optional.of(parcela));
        when(parcelaRepository.save(any(Parcela.class))).thenReturn(parcela);

        // Act
        Parcela resultado = compraParceladaService.marcarParcelaComoPaga(1L);

        // Assert
        assertTrue(resultado.isPaga());
        verify(parcelaRepository, times(1)).save(parcela);
    }

    @Test
    @DisplayName("Deve buscar compra parcelada por ID")
    void deveBuscarCompraPorId() {
        // Arrange
        compraParcelada.setId(1L);
        when(compraParceladaRepository.findById(1L)).thenReturn(Optional.of(compraParcelada));

        // Act
        CompraParcelada resultado = compraParceladaService.buscarPorId(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Notebook", resultado.getDescricao());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar compra inexistente")
    void deveLancarExcecaoAoBuscarCompraInexistente() {
        // Arrange
        when(compraParceladaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            compraParceladaService.buscarPorId(999L);
        });
    }

    @Test
    @DisplayName("Deve excluir compra parcelada")
    void deveExcluirCompraParcelada() {
        // Arrange
        compraParcelada.setId(1L);
        when(compraParceladaRepository.findById(1L)).thenReturn(Optional.of(compraParcelada));
        doNothing().when(compraParceladaRepository).delete(compraParcelada);

        // Act
        compraParceladaService.excluirCompraParcelada(1L);

        // Assert
        verify(compraParceladaRepository, times(1)).delete(compraParcelada);
    }
}
