package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.api.dto.NotificacaoDTO;
import com.tallyto.gestorfinanceiro.core.application.services.NotificacaoService;
import com.tallyto.gestorfinanceiro.testsupport.ControllerSliceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerSliceTest(controllers = NotificacaoController.class)
class NotificacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificacaoService notificacaoService;

    private NotificacaoDTO notif(String tipo, NotificacaoDTO.Prioridade prioridade, Long idEntidade) {
        return new NotificacaoDTO(
                tipo,
                prioridade,
                "Título",
                "Mensagem",
                idEntidade,
                "CONTA",
                3L
        );
    }

    @Test
    @DisplayName("GET /api/notificacoes retorna todas notificações")
    void obterNotificacoes() throws Exception {
        List<NotificacaoDTO> list = List.of(
                notif("CONTA_ATRASADA", NotificacaoDTO.Prioridade.CRITICA, 1L),
                notif("FATURA_ATRASADA", NotificacaoDTO.Prioridade.ALTA, 2L)
        );
        Mockito.when(notificacaoService.obterNotificacoes()).thenReturn(list);

        mockMvc.perform(get("/api/notificacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].tipo").value("CONTA_ATRASADA"))
                .andExpect(jsonPath("$[0].prioridade").value("CRITICA"));
    }

    @Test
    @DisplayName("GET /api/notificacoes/contas-atrasadas retorna notificações de contas atrasadas")
    void obterContasAtrasadas() throws Exception {
        Mockito.when(notificacaoService.obterNotificacaoContasAtrasadas())
                .thenReturn(List.of(notif("CONTA_ATRASADA", NotificacaoDTO.Prioridade.CRITICA, 1L)));

        mockMvc.perform(get("/api/notificacoes/contas-atrasadas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tipo").value("CONTA_ATRASADA"));
    }

    @Test
    @DisplayName("GET /api/notificacoes/contas-proximas-vencimento retorna notificações próximas ao vencimento")
    void obterContasProximasVencimento() throws Exception {
        Mockito.when(notificacaoService.obterNotificacaoContasProximasVencimento())
                .thenReturn(List.of(notif("CONTA_PROXIMA_VENCIMENTO", NotificacaoDTO.Prioridade.ALTA, 3L)));

        mockMvc.perform(get("/api/notificacoes/contas-proximas-vencimento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tipo").value("CONTA_PROXIMA_VENCIMENTO"));
    }

    @Test
    @DisplayName("GET /api/notificacoes/faturas-atrasadas retorna notificações de faturas atrasadas")
    void obterFaturasAtrasadas() throws Exception {
        Mockito.when(notificacaoService.obterNotificacaoFaturasAtrasadas())
                .thenReturn(List.of(notif("FATURA_ATRASADA", NotificacaoDTO.Prioridade.ALTA, 2L)));

        mockMvc.perform(get("/api/notificacoes/faturas-atrasadas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tipo").value("FATURA_ATRASADA"));
    }

    @Test
    @DisplayName("GET /api/notificacoes/resumo retorna resumo agregado de notificações")
    void obterResumoNotificacoes() throws Exception {
        List<NotificacaoDTO> list = List.of(
                notif("CONTA_ATRASADA", NotificacaoDTO.Prioridade.CRITICA, 1L),
                notif("FATURA_ATRASADA", NotificacaoDTO.Prioridade.ALTA, 2L),
                notif("CONTA_PROXIMA_VENCIMENTO", NotificacaoDTO.Prioridade.MEDIA, 3L)
        );
        Mockito.when(notificacaoService.obterNotificacoes()).thenReturn(list);

        mockMvc.perform(get("/api/notificacoes/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalNotificacoes").value(3))
                .andExpect(jsonPath("$.notificacoesCriticas").value(1))
                .andExpect(jsonPath("$.notificacoesAltas").value(1))
                .andExpect(jsonPath("$.contasAtrasadas").value(1))
                .andExpect(jsonPath("$.faturasAtrasadas").value(1))
                .andExpect(jsonPath("$.temNotificacoes").value(true));
    }
}
