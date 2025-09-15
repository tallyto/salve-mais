package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.core.application.services.ContaService;
import com.tallyto.gestorfinanceiro.core.application.services.RendimentoService;
import com.tallyto.gestorfinanceiro.core.domain.entities.Conta;
import com.tallyto.gestorfinanceiro.core.domain.enums.TipoConta;
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
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller slice tests para ContaController.
 * - Usa @ControllerSliceTest para isolamento e mocks de ContaService/RendimentoService.
 */
@ControllerSliceTest(controllers = ContaController.class)
class ContaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContaService contaService;

    @MockBean
    private RendimentoService rendimentoService;

    private Conta sampleConta(Long id, TipoConta tipo) {
        Conta c = new Conta();
        c.setId(id);
        c.setTitular("João");
        c.setTipo(tipo);
        c.setSaldo(new BigDecimal("1000.00"));
        c.setTaxaRendimento(new BigDecimal("12.5"));
        c.setDescricao("Conta de teste");
        return c;
    }

    @Test
    @DisplayName("POST /api/contas cria conta e retorna entidade")
    void criarConta_deveRetornarConta() throws Exception {
        Conta conta = sampleConta(1L, TipoConta.CORRENTE);
        Mockito.when(contaService.create(Mockito.any(Conta.class))).thenReturn(conta);

        String json = "{" +
                "\"titular\":\"João\"," +
                "\"tipo\":\"CORRENTE\"," +
                "\"saldo\":1000.00," +
                "\"taxaRendimento\":12.5," +
                "\"descricao\":\"Conta de teste\"}";

        mockMvc.perform(post("/api/contas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titular").value("João"));
    }

    @Test
    @DisplayName("GET /api/contas lista paginada")
    void listarContas() throws Exception {
        List<Conta> content = List.of(sampleConta(1L, TipoConta.CORRENTE), sampleConta(2L, TipoConta.INVESTIMENTO));
        Page<Conta> page = new PageImpl<>(content, PageRequest.of(0, 10), 2);
        Mockito.when(contaService.findAllAccounts(any())).thenReturn(page);

        mockMvc.perform(get("/api/contas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[1].id").value(2));
    }

    @Test
    @DisplayName("GET /api/contas/{id} retorna conta por id")
    void buscarPorId() throws Exception {
        Conta conta = sampleConta(1L, TipoConta.CORRENTE);
        Mockito.when(contaService.findOrFail(1L)).thenReturn(conta);

        mockMvc.perform(get("/api/contas/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titular").value("João"));
    }

    @Test
    @DisplayName("PUT /api/contas/{id} atualiza conta")
    void atualizarConta() throws Exception {
        Conta conta = sampleConta(1L, TipoConta.CORRENTE);
        Mockito.when(contaService.update(eq(1L), Mockito.any(Conta.class))).thenReturn(conta);

        String json = "{" +
                "\"titular\":\"João\"," +
                "\"tipo\":\"CORRENTE\"," +
                "\"saldo\":1000.00," +
                "\"taxaRendimento\":12.5," +
                "\"descricao\":\"Conta de teste\"}";

        mockMvc.perform(put("/api/contas/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titular").value("João"));
    }

    @Test
    @DisplayName("DELETE /api/contas/{id} exclui conta")
    void deletarConta() throws Exception {
        Mockito.doNothing().when(contaService).excluirConta(1L);

        mockMvc.perform(delete("/api/contas/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/contas/tipos retorna tipos de conta")
    void listarTiposConta() throws Exception {
        mockMvc.perform(get("/api/contas/tipos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(TipoConta.values().length)))
                .andExpect(jsonPath("$[0].tipo").value(TipoConta.CORRENTE.name()));
    }

    @Test
    @DisplayName("GET /api/contas/tipo/{tipo} lista contas por tipo")
    void listarPorTipo() throws Exception {
        List<Conta> contas = List.of(sampleConta(1L, TipoConta.INVESTIMENTO));
        Mockito.when(contaService.findByTipo(TipoConta.INVESTIMENTO)).thenReturn(contas);

        mockMvc.perform(get("/api/contas/tipo/{tipo}", TipoConta.INVESTIMENTO.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tipo").value(TipoConta.INVESTIMENTO.name()));
    }

    @Test
    @DisplayName("GET /api/contas/{id}/projetar-rendimento retorna valor projetado")
    void projetarRendimento() throws Exception {
        Conta conta = sampleConta(1L, TipoConta.INVESTIMENTO);
        Mockito.when(contaService.findOrFail(1L)).thenReturn(conta);
        Mockito.when(rendimentoService.projetarRendimento(conta, 12)).thenReturn(new BigDecimal("1120.00"));

        mockMvc.perform(get("/api/contas/{id}/projetar-rendimento", 1L).param("meses", "12"))
                .andExpect(status().isOk())
                .andExpect(content().string("1120.00"));
    }

    @Test
    @DisplayName("POST /api/contas/transferir realiza transferência entre contas")
    void transferirEntreContas() throws Exception {
        Mockito.doNothing().when(contaService).transferir(1L, 2L, new BigDecimal("100.00"));

        String json = "{" +
                "\"contaOrigemId\":1," +
                "\"contaDestinoId\":2," +
                "\"valor\":100.00}";

        mockMvc.perform(post("/api/contas/transferir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }
}