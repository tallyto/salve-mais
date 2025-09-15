package com.tallyto.gestorfinanceiro.api.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.tallyto.gestorfinanceiro.api.dto.AnexoDTO;
import com.tallyto.gestorfinanceiro.api.dto.UrlDownloadDTO;
import com.tallyto.gestorfinanceiro.api.mappers.AnexoMapper;
import com.tallyto.gestorfinanceiro.core.application.services.AnexoService;
import com.tallyto.gestorfinanceiro.core.domain.entities.Anexo;
import com.tallyto.gestorfinanceiro.testsupport.ControllerSliceTest;

/**
 * Controller slice tests para ComprovantesGlobaisController.
 * - Usa @ControllerSliceTest para isolamento e mocks de AnexoService/AnexoMapper.
 */
@ControllerSliceTest(controllers = ComprovantesGlobaisController.class)
class ComprovantesGlobaisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnexoService anexoService;

    @MockBean
    private AnexoMapper anexoMapper;

    private Anexo sampleAnexo(Long id) {
        Anexo a = new Anexo();
        a.setId(id);
        a.setNome("comprovante.pdf");
        a.setTipo("application/pdf");
        a.setDataUpload(java.time.LocalDateTime.of(2025,9,14,12,0));
        a.setContaFixa(null);
        return a;
    }

    @Test
    @DisplayName("GET /api/comprovantes retorna lista de comprovantes globais")
    void listarTodosComprovantes_deveRetornarLista() throws Exception {
        Anexo a1 = sampleAnexo(1L);
        Anexo a2 = sampleAnexo(2L);
        AnexoDTO dto1 = new AnexoDTO(1L, "comprovante.pdf", "application/pdf", java.time.LocalDateTime.of(2025,9,14,12,0), null);
        AnexoDTO dto2 = new AnexoDTO(2L, "comprovante.pdf", "application/pdf", java.time.LocalDateTime.of(2025,9,14,12,0), null);

        Mockito.when(anexoService.listarTodosAnexos()).thenReturn(List.of(a1, a2));
        Mockito.when(anexoMapper.toDTO(a1)).thenReturn(dto1);
        Mockito.when(anexoMapper.toDTO(a2)).thenReturn(dto2);

        mockMvc.perform(get("/api/comprovantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @DisplayName("GET /api/comprovantes/{anexoId}/download retorna URL quando encontrado")
    void gerarUrlDownload_ok() throws Exception {
        Long anexoId = 1L;
        Anexo anexo = sampleAnexo(anexoId);
        UrlDownloadDTO urlDto = new UrlDownloadDTO("https://url.com/download", "comprovante.pdf", "application/pdf");

        Mockito.when(anexoService.listarTodosAnexos()).thenReturn(List.of(anexo));
        Mockito.when(anexoService.gerarUrlDownload(anexoId)).thenReturn("https://url.com/download");
        Mockito.when(anexoMapper.toUrlDownloadDTO(anexo, "https://url.com/download")).thenReturn(urlDto);

        mockMvc.perform(get("/api/comprovantes/{anexoId}/download", anexoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://url.com/download"))
                .andExpect(jsonPath("$.nome").value("comprovante.pdf"));
    }

    @Test
    @DisplayName("GET /api/comprovantes/{anexoId}/download retorna 404 quando não encontrado")
    void gerarUrlDownload_naoEncontrado() throws Exception {
        Long anexoId = 99L;
        Mockito.when(anexoService.listarTodosAnexos()).thenReturn(List.of());

        mockMvc.perform(get("/api/comprovantes/{anexoId}/download", anexoId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/comprovantes/{anexoId} remove comprovante e retorna 204")
    void removerComprovante_ok() throws Exception {
        Long anexoId = 1L;
        Mockito.doNothing().when(anexoService).excluirAnexo(anexoId);

        mockMvc.perform(delete("/api/comprovantes/{anexoId}", anexoId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/comprovantes/{anexoId} retorna 404 quando não encontrado")
    void removerComprovante_naoEncontrado() throws Exception {
        Long anexoId = 99L;
        Mockito.doThrow(new IllegalArgumentException("not found")).when(anexoService).excluirAnexo(anexoId);

        mockMvc.perform(delete("/api/comprovantes/{anexoId}", anexoId))
                .andExpect(status().isNotFound());
    }
}