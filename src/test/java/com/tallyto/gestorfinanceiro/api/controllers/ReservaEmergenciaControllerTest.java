package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.api.dto.ContribuicaoReservaDTO;
import com.tallyto.gestorfinanceiro.api.dto.ReservaEmergenciaDTO;
import com.tallyto.gestorfinanceiro.api.dto.ReservaEmergenciaDetalheDTO;
import com.tallyto.gestorfinanceiro.api.dto.ReservaEmergenciaInputDTO;
import com.tallyto.gestorfinanceiro.core.application.services.ReservaEmergenciaService;
import com.tallyto.gestorfinanceiro.testsupport.ControllerSliceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerSliceTest(controllers = ReservaEmergenciaController.class)
class ReservaEmergenciaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservaEmergenciaService reservaEmergenciaService;

    @Autowired
    private ObjectMapper objectMapper;

    private ReservaEmergenciaDTO sampleDTO() {
        return new ReservaEmergenciaDTO(
                1L, new BigDecimal("10000.00"), 6, new BigDecimal("2500.00"),
                new BigDecimal("25.0"), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 7, 1),
                new BigDecimal("500.00"), 2L
        );
    }

    private ReservaEmergenciaDetalheDTO sampleDetalheDTO() {
        return new ReservaEmergenciaDetalheDTO(
                1L, new BigDecimal("10000.00"), 6, new BigDecimal("2500.00"),
                new BigDecimal("25.0"), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 7, 1),
                new BigDecimal("500.00"), null, 6, new BigDecimal("4200.00")
        );
    }

    @Test
    @DisplayName("GET /api/reserva-emergencia retorna lista")
    void findAll() throws Exception {
        Mockito.when(reservaEmergenciaService.findAll()).thenReturn(List.of(sampleDTO()));
        mockMvc.perform(get("/api/reserva-emergencia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].objetivo").value(10000.00));
    }

    @Test
    @DisplayName("GET /api/reserva-emergencia/{id} retorna detalhe")
    void findById() throws Exception {
        Mockito.when(reservaEmergenciaService.findById(1L)).thenReturn(sampleDetalheDTO());
        mockMvc.perform(get("/api/reserva-emergencia/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.objetivo").value(10000.00))
                .andExpect(jsonPath("$.mesesRestantes").value(6));
    }

    @Test
    @DisplayName("POST /api/reserva-emergencia cria reserva")
    void create() throws Exception {
        ReservaEmergenciaInputDTO input = new ReservaEmergenciaInputDTO(
                new BigDecimal("10000.00"), 6, new BigDecimal("500.00"), 2L, new BigDecimal("0.05")
        );
        Mockito.when(reservaEmergenciaService.create(Mockito.any(ReservaEmergenciaInputDTO.class))).thenReturn(sampleDTO());
        mockMvc.perform(post("/api/reserva-emergencia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.objetivo").value(10000.00));
    }

    @Test
    @DisplayName("PUT /api/reserva-emergencia/{id} atualiza reserva")
    void update() throws Exception {
        ReservaEmergenciaInputDTO input = new ReservaEmergenciaInputDTO(
                new BigDecimal("12000.00"), 8, new BigDecimal("600.00"), 2L, new BigDecimal("0.05")
        );
        ReservaEmergenciaDTO updated = new ReservaEmergenciaDTO(
                1L, new BigDecimal("12000.00"), 8, new BigDecimal("3000.00"),
                new BigDecimal("25.0"), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 9, 1),
                new BigDecimal("600.00"), 2L
        );
        Mockito.when(reservaEmergenciaService.update(Mockito.eq(1L), Mockito.any(ReservaEmergenciaInputDTO.class))).thenReturn(updated);
        mockMvc.perform(put("/api/reserva-emergencia/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.objetivo").value(12000.00))
                .andExpect(jsonPath("$.multiplicadorDespesas").value(8));
    }

    @Test
    @DisplayName("DELETE /api/reserva-emergencia/{id} exclui reserva")
    void delete() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/reserva-emergencia/1"))
                .andExpect(status().isNoContent());
        Mockito.verify(reservaEmergenciaService).delete(1L);
    }

    @Test
    @DisplayName("POST /api/reserva-emergencia/{id}/saldo atualiza saldo")
    void atualizarSaldo() throws Exception {
        Mockito.when(reservaEmergenciaService.atualizarSaldo(1L, new BigDecimal("3000.00"))).thenReturn(sampleDTO());
        mockMvc.perform(post("/api/reserva-emergencia/{id}/saldo", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("3000.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoAtual").value(2500.00));
    }

    @Test
    @DisplayName("GET /api/reserva-emergencia/calcular-objetivo calcula objetivo")
    void calcularObjetivoAutomatico() throws Exception {
        Mockito.when(reservaEmergenciaService.calcularObjetivoAutomatico(6)).thenReturn(new BigDecimal("9000.00"));
        mockMvc.perform(get("/api/reserva-emergencia/calcular-objetivo").param("multiplicadorDespesas", "6"))
                .andExpect(status().isOk())
                .andExpect(content().string("9000.00"));
    }

    @Test
    @DisplayName("GET /api/reserva-emergencia/simulacao simula tempo para completar")
    void simularTempoParaCompletar() throws Exception {
        Mockito.when(reservaEmergenciaService.simularTempoParaCompletar(new BigDecimal("10000.00"), new BigDecimal("500.00"))).thenReturn(20);
        mockMvc.perform(get("/api/reserva-emergencia/simulacao")
                .param("objetivo", "10000.00")
                .param("valorContribuicaoMensal", "500.00"))
                .andExpect(status().isOk())
                .andExpect(content().string("20"));
    }

    @Test
    @DisplayName("POST /api/reserva-emergencia/{id}/contribuir contribui para reserva")
    void contribuirParaReserva() throws Exception {
        ContribuicaoReservaDTO contrib = new ContribuicaoReservaDTO(2L, new BigDecimal("200.00"));
        Mockito.when(reservaEmergenciaService.contribuirParaReserva(Mockito.eq(1L), Mockito.any(ContribuicaoReservaDTO.class))).thenReturn(sampleDTO());
        mockMvc.perform(post("/api/reserva-emergencia/{id}/contribuir", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contrib)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }
}