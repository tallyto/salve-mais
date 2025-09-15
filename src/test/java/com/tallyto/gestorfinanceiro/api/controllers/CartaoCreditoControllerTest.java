package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.core.application.services.CartaoCreditoService;
import com.tallyto.gestorfinanceiro.core.domain.entities.CartaoCredito;
import com.tallyto.gestorfinanceiro.api.dto.CartaoLimiteDTO;
import com.tallyto.gestorfinanceiro.api.dto.CartaoLimiteStatusDTO;
import com.tallyto.gestorfinanceiro.testsupport.ControllerSliceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.databind.ObjectMapper;

@ControllerSliceTest(controllers = CartaoCreditoController.class)
public class CartaoCreditoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartaoCreditoService cartaoCreditoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testListarCartaoCredito() throws Exception {
        CartaoCredito cartao = new CartaoCredito();
        cartao.setId(1L);
        cartao.setNome("Cartão Teste");
        when(cartaoCreditoService.listarCartoesCredito()).thenReturn(List.of(cartao));
        mockMvc.perform(get("/api/cartao-credito"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nome").value("Cartão Teste"));
    }

    @Test
    void testBuscarCartaoCredito() throws Exception {
        CartaoCredito cartao = new CartaoCredito();
        cartao.setId(1L);
        cartao.setNome("Cartão Teste");
        when(cartaoCreditoService.findOrFail(1L)).thenReturn(cartao);
        mockMvc.perform(get("/api/cartao-credito/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(".id").value(1))
                .andExpect(jsonPath(".nome").value("Cartão Teste"));
    }

    @Test
    void testSalvarCartaoCredito() throws Exception {
        CartaoCredito cartao = new CartaoCredito();
        cartao.setId(1L);
        cartao.setNome("Novo Cartão");
        when(cartaoCreditoService.salvarCartaoCredito(any(CartaoCredito.class))).thenReturn(cartao);
        mockMvc.perform(post("/api/cartao-credito")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(".id").value(1))
                .andExpect(jsonPath(".nome").value("Novo Cartão"));
    }

    @Test
    void testExcluirCartaoCredito() throws Exception {
        mockMvc.perform(delete("/api/cartao-credito/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testConfigurarLimite() throws Exception {
    // Corrigido: record CartaoLimiteDTO exige três campos
    CartaoLimiteDTO dto = new CartaoLimiteDTO(1L, new BigDecimal("5000.00"), 80);
        CartaoCredito cartao = new CartaoCredito();
        cartao.setId(1L);
    // Corrigido: o campo correto é limiteTotal
    cartao.setLimiteTotal(new BigDecimal("5000.00"));
        when(cartaoCreditoService.configurarLimite(any(CartaoLimiteDTO.class))).thenReturn(cartao);
        mockMvc.perform(put("/api/cartao-credito/1/limite")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(".id").value(1))
                .andExpect(jsonPath(".limiteTotal").value(5000.00));
    }

    @Test
    void testVerificarStatusLimite() throws Exception {
        // Corrigido: record CartaoLimiteStatusDTO exige nove campos
        CartaoLimiteStatusDTO status = new CartaoLimiteStatusDTO(
            1L,
            "Cartão Teste",
            new BigDecimal("5000.00"),
            new BigDecimal("1000.00"),
            new BigDecimal("4000.00"),
            new BigDecimal("20.00"),
            false,
            false,
            80
        );
        when(cartaoCreditoService.verificarStatusLimite(1L)).thenReturn(status);
        mockMvc.perform(get("/api/cartao-credito/1/limite/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(".cartaoId").value(1))
                .andExpect(jsonPath(".nomeCartao").value("Cartão Teste"))
                .andExpect(jsonPath(".limiteTotal").value(5000.00))
                .andExpect(jsonPath(".alertaAtivado").value(false));
    }

    @Test
    void testListarStatusLimiteTodos() throws Exception {
        CartaoLimiteStatusDTO status = new CartaoLimiteStatusDTO(
            1L,
            "Cartão Teste",
            new BigDecimal("5000.00"),
            new BigDecimal("1000.00"),
            new BigDecimal("4000.00"),
            new BigDecimal("20.00"),
            false,
            false,
            80
        );
        when(cartaoCreditoService.listarStatusLimiteTodos()).thenReturn(List.of(status));
        mockMvc.perform(get("/api/cartao-credito/limite/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cartaoId").value(1))
                .andExpect(jsonPath("$[0].nomeCartao").value("Cartão Teste"))
                .andExpect(jsonPath("$[0].limiteTotal").value(5000.00))
                .andExpect(jsonPath("$[0].alertaAtivado").value(false));
    }

    @Test
    void testVerificarAlertas() throws Exception {
        CartaoLimiteStatusDTO alerta = new CartaoLimiteStatusDTO(
            1L,
            "Cartão Teste",
            new BigDecimal("5000.00"),
            new BigDecimal("4800.00"),
            new BigDecimal("200.00"),
            new BigDecimal("96.00"),
            true,
            true,
            80
        );
        when(cartaoCreditoService.verificarAlertas()).thenReturn(List.of(alerta));
        mockMvc.perform(get("/api/cartao-credito/limite/alertas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cartaoId").value(1))
                .andExpect(jsonPath("$[0].nomeCartao").value("Cartão Teste"))
                .andExpect(jsonPath("$[0].limiteTotal").value(5000.00))
                .andExpect(jsonPath("$[0].alertaAtivado").value(true));
    }

    @Test
    void testCalcularLimiteDisponivel() throws Exception {
        when(cartaoCreditoService.calcularLimiteDisponivel(1L)).thenReturn(new BigDecimal("1200.00"));
        mockMvc.perform(get("/api/cartao-credito/1/limite/disponivel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(".limiteDisponivel").value(1200.00));
    }

    @Test
    void testVerificarCompra() throws Exception {
        when(cartaoCreditoService.podeRealizarCompra(1L, new BigDecimal("100.00"))).thenReturn(true);
        when(cartaoCreditoService.calcularLimiteDisponivel(1L)).thenReturn(new BigDecimal("1200.00"));
        mockMvc.perform(post("/api/cartao-credito/1/limite/verificar-compra")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("valor", new BigDecimal("100.00")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath(".podeRealizar").value(true))
                .andExpect(jsonPath(".valorCompra").value(100.00))
                .andExpect(jsonPath(".limiteDisponivel").value(1200.00));
    }
}
