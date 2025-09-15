package com.tallyto.gestorfinanceiro.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallyto.gestorfinanceiro.api.dto.ProventoDTO;
import com.tallyto.gestorfinanceiro.core.application.services.ProventoService;
import com.tallyto.gestorfinanceiro.core.domain.entities.Conta;
import com.tallyto.gestorfinanceiro.core.domain.entities.Provento;
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

@ControllerSliceTest(controllers = ProventoController.class)
class ProventoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProventoService proventoService;

    private Provento sample(Long id) {
        Provento p = new Provento();
        p.setId(id);
        p.setDescricao("Salário");
        p.setValor(new BigDecimal("1000.00"));
        p.setData(LocalDate.of(2025, 9, 1));
        Conta c = new Conta();
        c.setId(1L);
        p.setConta(c);
        return p;
    }

    @Test
    @DisplayName("POST /api/proventos cria provento e retorna 200")
    void criarProvento_ok() throws Exception {
        Provento salvo = sample(1L);
        Mockito.when(proventoService.salvarProvento(Mockito.any(Provento.class))).thenReturn(salvo);

        ProventoDTO dto = new ProventoDTO("Salário", new BigDecimal("1000.00"), LocalDate.of(2025,9,1), 1L);

        mockMvc.perform(post("/api/proventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.descricao").value("Salário"));
    }

    @Test
    @DisplayName("GET /api/proventos lista paginada")
    void listarProventos() throws Exception {
        List<Provento> content = List.of(sample(1L), sample(2L));
        Page<Provento> page = new PageImpl<>(content, PageRequest.of(0, 10), 2);
        Mockito.when(proventoService.listarProventos(any())).thenReturn(page);

        mockMvc.perform(get("/api/proventos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("PUT /api/proventos/{id} atualiza e retorna 200")
    void atualizarProvento_ok() throws Exception {
        Provento atualizado = sample(1L);
        atualizado.setDescricao("Ajuste");
        Mockito.when(proventoService.atualizarProvento(Mockito.any(Provento.class))).thenReturn(atualizado);

        ProventoDTO dto = new ProventoDTO("Ajuste", new BigDecimal("1100.00"), LocalDate.of(2025,9,2), 1L);

        mockMvc.perform(put("/api/proventos/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.descricao").value("Ajuste"));
    }

    @Test
    @DisplayName("DELETE /api/proventos/{id} retorna 204 quando excluído")
    void excluirProvento_ok() throws Exception {
        Mockito.doNothing().when(proventoService).excluirProvento(1L);

        mockMvc.perform(delete("/api/proventos/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/proventos/{id} retorna 404 quando não encontrado")
    void excluirProvento_notFound() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("Provento não encontrado")).when(proventoService).excluirProvento(99L);

        mockMvc.perform(delete("/api/proventos/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/proventos falha com validation error (400)")
    void criarProvento_validationError() throws Exception {
        ProventoDTO dto = new ProventoDTO("", null, null, null);

        mockMvc.perform(post("/api/proventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}
