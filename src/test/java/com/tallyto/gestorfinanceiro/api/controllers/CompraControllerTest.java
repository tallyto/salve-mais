package com.tallyto.gestorfinanceiro.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallyto.gestorfinanceiro.api.dto.CompraDTO;
import com.tallyto.gestorfinanceiro.core.application.services.CompraService;
import com.tallyto.gestorfinanceiro.core.domain.entities.CartaoCredito;
import com.tallyto.gestorfinanceiro.core.domain.entities.Categoria;
import com.tallyto.gestorfinanceiro.core.domain.entities.Compra;
import com.tallyto.gestorfinanceiro.testsupport.ControllerSliceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller slice tests for CompraController.
 * Notes:
 * - Uses @ControllerSliceTest to isolate MVC and disable security/tenant filters globally.
 * - Mocks CompraService interactions and verifies JSON contracts only at the controller boundary.
 */
@ControllerSliceTest(controllers = CompraController.class)
class CompraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompraService compraService;

    private Compra sampleCompra(Long id) {
        Compra c = new Compra();
        c.setId(id);
        c.setDescricao("Mercado");
        c.setValor(new BigDecimal("123.45"));
        c.setData(LocalDate.of(2025, 9, 10));
        Categoria categoria = new Categoria();
        categoria.setId(7L);
        c.setCategoria(categoria);
        CartaoCredito cartao = new CartaoCredito();
        cartao.setId(5L);
        c.setCartaoCredito(cartao);
        return c;
    }

    @Test
    @DisplayName("POST /api/compras cria compra e retorna 200 com entidade")
    void criarCompra_deveRetornarCompra() throws Exception {
        Compra created = sampleCompra(1L);
        Mockito.when(compraService.salvarCompra(Mockito.any(Compra.class))).thenReturn(created);

        CompraDTO input = new CompraDTO(
                null,
                "Mercado",
                new BigDecimal("123.45"),
                LocalDate.of(2025, 9, 10),
                7L,
                5L
        );

        mockMvc.perform(post("/api/compras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.descricao").value("Mercado"))
                .andExpect(jsonPath("$.valor").value(123.45))
                .andExpect(jsonPath("$.data").value("2025-09-10"))
                .andExpect(jsonPath("$.categoria.id").value(7))
                .andExpect(jsonPath("$.cartaoCredito.id").value(5));
    }

    @Test
    @DisplayName("GET /api/compras lista paginada sem filtros")
    void listarCompras_semFiltros() throws Exception {
        List<Compra> content = List.of(sampleCompra(1L), sampleCompra(2L));
        Page<Compra> page = new PageImpl<>(content, PageRequest.of(0, 10), 2);
        Mockito.when(compraService.listarCompras(any())).thenReturn(page);

        mockMvc.perform(get("/api/compras"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("GET /api/compras?mes=9&ano=2025 utiliza filtro por mês e ano")
    void listarCompras_comMesAno() throws Exception {
        List<Compra> content = List.of(sampleCompra(3L));
        Page<Compra> page = new PageImpl<>(content, PageRequest.of(0, 10), 1);
        Mockito.when(compraService.listarComprasPorMesEAno(any(), eq(9), eq(2025))).thenReturn(page);

        mockMvc.perform(get("/api/compras").param("mes", "9").param("ano", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(3))
                .andExpect(jsonPath("$.totalElements").value(1));

        Mockito.verify(compraService, Mockito.never()).listarCompras(any());
    }

    @Test
    @DisplayName("GET /api/compras/cartao/{id}?dataVencimento=YYYY-MM-DD retorna lista")
    void listarPorCartaoAteData() throws Exception {
        LocalDate vencimento = LocalDate.of(2025, 9, 20);
        Mockito.when(compraService.comprasPorCartaoAteData(eq(5L), eq(vencimento)))
                .thenReturn(List.of(sampleCompra(10L)));

        mockMvc.perform(get("/api/compras/cartao/{cartaoId}", 5L)
                        .param("dataVencimento", "2025-09-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    @DisplayName("GET /api/compras/{id} retorna 200 quando encontrado")
    void buscarPorId_encontrado() throws Exception {
        Mockito.when(compraService.buscarCompraPorId(1L)).thenReturn(sampleCompra(1L));

        mockMvc.perform(get("/api/compras/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /api/compras/{id} retorna 404 quando não encontrado")
    void buscarPorId_naoEncontrado() throws Exception {
        Mockito.when(compraService.buscarCompraPorId(99L)).thenThrow(new RuntimeException("not found"));

        mockMvc.perform(get("/api/compras/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/compras/{id} atualiza e retorna 200")
    void atualizarCompra_ok() throws Exception {
        Compra updated = sampleCompra(1L);
        updated.setDescricao("Padaria");
        Mockito.when(compraService.atualizarCompra(eq(1L), Mockito.any(Compra.class))).thenReturn(updated);

        CompraDTO input = new CompraDTO(
                null,
                "Padaria",
                new BigDecimal("50.00"),
                LocalDate.of(2025, 9, 11),
                7L,
                5L
        );

        mockMvc.perform(put("/api/compras/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.descricao").value("Padaria"));
    }

    @Test
    @DisplayName("PUT /api/compras/{id} retorna 404 quando não encontrado")
    void atualizarCompra_naoEncontrado() throws Exception {
        Mockito.when(compraService.atualizarCompra(eq(42L), Mockito.any(Compra.class)))
                .thenThrow(new RuntimeException("not found"));

        CompraDTO input = new CompraDTO(
                null,
                "Qualquer",
                new BigDecimal("10.00"),
                LocalDate.of(2025, 9, 1),
                7L,
                5L
        );

        mockMvc.perform(put("/api/compras/{id}", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/compras/{id} retorna 204 quando excluído")
    void excluirCompra_ok() throws Exception {
        Mockito.doNothing().when(compraService).excluirCompra(1L);

        mockMvc.perform(delete("/api/compras/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/compras/{id} retorna 404 quando não encontrado")
    void excluirCompra_naoEncontrado() throws Exception {
        Mockito.doThrow(new RuntimeException("not found")).when(compraService).excluirCompra(99L);

        mockMvc.perform(delete("/api/compras/{id}", 99L))
                .andExpect(status().isNotFound());
    }
}
