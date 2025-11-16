package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.api.dto.FaturaManualDTO;
import com.tallyto.gestorfinanceiro.core.application.services.FaturaService;
import com.tallyto.gestorfinanceiro.core.domain.entities.CartaoCredito;
import com.tallyto.gestorfinanceiro.core.domain.entities.Fatura;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.tallyto.gestorfinanceiro.testsupport.ControllerSliceTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Usa a anotação composta para isolar o slice MVC, excluir filtros globais e aplicar mocks de segurança.
@ControllerSliceTest(controllers = FaturaController.class)
public class FaturaControllerTest {

    @Autowired
    private MockMvc mockMvc;

        // Mock do serviço de Fatura para controlar os retornos no teste.
        @MockBean
        private FaturaService faturaService;

                        // Mocks de segurança agora são fornecidos globalmente por TestSecurityMocks via @ControllerSliceTest.

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testListarFaturas() throws Exception {
        // Arrange
        CartaoCredito cartao = new CartaoCredito();
        cartao.setId(1L);
        cartao.setNome("Cartão Teste");

        Fatura fatura = new Fatura();
        fatura.setId(1L);
        fatura.setCartaoCredito(cartao);
        fatura.setValorTotal(new BigDecimal("1500.00"));
        fatura.setDataVencimento(LocalDate.of(2024, 12, 10));
        fatura.setPago(false);
        fatura.setCompras(new ArrayList<>());

        Page<Fatura> page = new PageImpl<>(List.of(fatura));
        when(faturaService.listar(any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/faturas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].nomeCartao").value("Cartão Teste"))
                .andExpect(jsonPath("$.content[0].valorTotal").value(1500.00))
                .andExpect(jsonPath("$.content[0].pago").value(false))
                .andExpect(jsonPath("$.content[0].totalCompras").value(0));
    }

    @Test
    public void testBuscarFatura() throws Exception {
        // Arrange
        CartaoCredito cartao = new CartaoCredito();
        cartao.setId(1L);
        cartao.setNome("Cartão Teste");

        Fatura fatura = new Fatura();
        fatura.setId(1L);
        fatura.setCartaoCredito(cartao);
        fatura.setValorTotal(new BigDecimal("1500.00"));
        fatura.setDataVencimento(LocalDate.of(2024, 12, 10));
        fatura.setPago(false);
        fatura.setCompras(new ArrayList<>());

        when(faturaService.findOrFail(1L)).thenReturn(fatura);

        // Act & Assert
        mockMvc.perform(get("/api/faturas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nomeCartao").value("Cartão Teste"))
                .andExpect(jsonPath("$.valorTotal").value(1500.00));
    }

    @Test
    public void testCriarFaturaManual() throws Exception {
        // Arrange
        FaturaManualDTO faturaManualDTO = new FaturaManualDTO(
                1L,
                new BigDecimal("2500.00"),
                LocalDate.of(2024, 12, 15)
        );

        CartaoCredito cartao = new CartaoCredito();
        cartao.setId(1L);
        cartao.setNome("Cartão Teste");

        Fatura faturaCriada = new Fatura();
        faturaCriada.setId(1L);
        faturaCriada.setCartaoCredito(cartao);
        faturaCriada.setValorTotal(new BigDecimal("2500.00"));
        faturaCriada.setDataVencimento(LocalDate.of(2024, 12, 15));
        faturaCriada.setPago(false);
        faturaCriada.setCompras(new ArrayList<>());

        when(faturaService.criarFaturaManual(anyLong(), any(BigDecimal.class), any(LocalDate.class)))
                .thenReturn(faturaCriada);

        // Act & Assert
        mockMvc.perform(post("/api/faturas/manual")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(faturaManualDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.valorTotal").value(2500.00))
                .andExpect(jsonPath("$.pago").value(false));
    }

    @Test
    public void testCriarFaturaManualComDadosInvalidos() throws Exception {
        // Arrange
        FaturaManualDTO faturaManualDTO = new FaturaManualDTO(
                null, // ID do cartão nulo
                new BigDecimal("-100.00"), // Valor negativo
                null // Data nula
        );

        // Act & Assert
        mockMvc.perform(post("/api/faturas/manual")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(faturaManualDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testMarcarComoPaga() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/api/faturas/1/pagar"))
                .andExpect(status().isOk());

        verify(faturaService).marcarComoPaga(1L);
    }

    @Test
    public void testExcluirFatura() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/faturas/1"))
                .andExpect(status().isNoContent());

        verify(faturaService).excluirFatura(1L);
    }

    @Test
    public void testGerarFaturaAutomatica() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/faturas/gerar/1"))
                .andExpect(status().isCreated());

        verify(faturaService).gerarFatura(1L, null);
    }

        @Test
        public void testListarFaturasPendentes() throws Exception {
                // Arrange
                CartaoCredito cartao = new CartaoCredito();
                cartao.setId(2L);
                cartao.setNome("Cartão Pendente");

                Fatura pendente = new Fatura();
                pendente.setId(2L);
                pendente.setCartaoCredito(cartao);
                pendente.setValorTotal(new BigDecimal("999.99"));
                pendente.setDataVencimento(LocalDate.of(2024, 12, 20));
                pendente.setPago(false);
                pendente.setCompras(new ArrayList<>());

                when(faturaService.listarNaoPagas()).thenReturn(List.of(pendente));

                // Act & Assert
                mockMvc.perform(get("/api/faturas/pendentes"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(2))
                                .andExpect(jsonPath("$[0].nomeCartao").value("Cartão Pendente"))
                                .andExpect(jsonPath("$[0].pago").value(false));
        }

        @Test
        public void testListarFaturasPorConta() throws Exception {
                // Arrange
                CartaoCredito cartao = new CartaoCredito();
                cartao.setId(3L);
                cartao.setNome("Cartão Conta");

                Fatura f = new Fatura();
                f.setId(3L);
                f.setCartaoCredito(cartao);
                f.setValorTotal(new BigDecimal("123.45"));
                f.setDataVencimento(LocalDate.of(2024, 11, 5));
                f.setPago(true);
                f.setCompras(new ArrayList<>());

                when(faturaService.listarPorConta(10L)).thenReturn(List.of(f));

                // Act & Assert
                mockMvc.perform(get("/api/faturas/conta/{contaId}", 10L))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(3))
                                .andExpect(jsonPath("$[0].nomeCartao").value("Cartão Conta"))
                                .andExpect(jsonPath("$[0].pago").value(true));
        }

        @Test
        public void testPagarFaturaComConta() throws Exception {
                // Act & Assert
                mockMvc.perform(patch("/api/faturas/{faturaId}/pagar/{contaId}", 5L, 20L))
                                .andExpect(status().isOk());

                verify(faturaService).marcarComoPaga(5L, 20L);
        }

        @Test
        public void testGerarFaturaLegacy() throws Exception {
                // Act
                mockMvc.perform(post("/api/faturas/{cardId}", 7L))
                                .andExpect(status().isOk());

                // Assert
                verify(faturaService).gerarFatura(7L);
        }
}
