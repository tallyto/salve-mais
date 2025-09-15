package com.tallyto.gestorfinanceiro.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallyto.gestorfinanceiro.api.dto.AnexoDTO;
import com.tallyto.gestorfinanceiro.api.dto.UrlDownloadDTO;
import com.tallyto.gestorfinanceiro.core.application.services.ContaFixaService;
import com.tallyto.gestorfinanceiro.core.domain.entities.Anexo;
import com.tallyto.gestorfinanceiro.testsupport.ControllerSliceTest;
import com.tallyto.gestorfinanceiro.api.mappers.AnexoMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller slice tests for ComprovanteController.
 * - Uses @ControllerSliceTest for isolation and disables global filters.
 * - Mocks ContaFixaService and AnexoMapper for all endpoints.
 */
@ControllerSliceTest(controllers = ComprovanteController.class)
class ComprovanteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ContaFixaService contaFixaService;

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
    @DisplayName("POST /api/contas-fixas/{id}/comprovantes faz upload e retorna 201")
    void anexarComprovante_deveRetornar201() throws Exception {
        Long contaFixaId = 10L;
        MockMultipartFile file = new MockMultipartFile("arquivo", "comprovante.pdf", MediaType.APPLICATION_PDF_VALUE, "conteudo".getBytes());
        Anexo anexo = sampleAnexo(1L);
    AnexoDTO dto = new AnexoDTO(1L, "comprovante.pdf", "application/pdf", java.time.LocalDateTime.of(2025,9,14,12,0), null);

        Mockito.when(contaFixaService.adicionarComprovante(eq(contaFixaId), any())).thenReturn(anexo);
        Mockito.when(anexoMapper.toDTO(anexo)).thenReturn(dto);

        mockMvc.perform(multipart("/api/contas-fixas/{id}/comprovantes", contaFixaId)
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("comprovante.pdf"));
    }

    @Test
    @DisplayName("GET /api/contas-fixas/{id}/comprovantes retorna lista de comprovantes")
    void listarComprovantes_deveRetornarLista() throws Exception {
        Long contaFixaId = 10L;
        Anexo a1 = sampleAnexo(1L);
        Anexo a2 = sampleAnexo(2L);
    AnexoDTO dto1 = new AnexoDTO(1L, "comprovante.pdf", "application/pdf", java.time.LocalDateTime.of(2025,9,14,12,0), null);
    AnexoDTO dto2 = new AnexoDTO(2L, "comprovante.pdf", "application/pdf", java.time.LocalDateTime.of(2025,9,14,12,0), null);

        Mockito.when(contaFixaService.listarComprovantes(contaFixaId)).thenReturn(List.of(a1, a2));
        Mockito.when(anexoMapper.toDTO(a1)).thenReturn(dto1);
        Mockito.when(anexoMapper.toDTO(a2)).thenReturn(dto2);

        mockMvc.perform(get("/api/contas-fixas/{id}/comprovantes", contaFixaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @DisplayName("GET /api/contas-fixas/{id}/comprovantes/{anexoId}/download retorna URL")
    void gerarUrlDownload_deveRetornarUrl() throws Exception {
        Long contaFixaId = 10L;
        Long anexoId = 1L;
        Anexo anexo = sampleAnexo(anexoId);
    UrlDownloadDTO urlDto = new UrlDownloadDTO("https://url.com/download", "comprovante.pdf", "application/pdf");

        Mockito.when(contaFixaService.listarComprovantes(contaFixaId)).thenReturn(List.of(anexo));
        Mockito.when(contaFixaService.gerarUrlDownloadComprovante(anexoId)).thenReturn("https://url.com/download");
        Mockito.when(anexoMapper.toUrlDownloadDTO(anexo, "https://url.com/download")).thenReturn(urlDto);

        mockMvc.perform(get("/api/contas-fixas/{id}/comprovantes/{anexoId}/download", contaFixaId, anexoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://url.com/download"))
                .andExpect(jsonPath("$.nome").value("comprovante.pdf"));
    }

    @Test
    @DisplayName("DELETE /api/contas-fixas/{id}/comprovantes/{anexoId} remove comprovante e retorna 204")
    void removerComprovante_ok() throws Exception {
        Long contaFixaId = 10L;
        Long anexoId = 1L;
        Anexo anexo = sampleAnexo(anexoId);

        Mockito.when(contaFixaService.listarComprovantes(contaFixaId)).thenReturn(List.of(anexo));
        Mockito.doNothing().when(contaFixaService).removerComprovante(anexoId);

        mockMvc.perform(delete("/api/contas-fixas/{id}/comprovantes/{anexoId}", contaFixaId, anexoId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/contas-fixas/{id}/comprovantes/{anexoId} retorna 404 se não pertence à conta")
    void removerComprovante_naoPertence() throws Exception {
        Long contaFixaId = 10L;
        Long anexoId = 99L;
        Mockito.when(contaFixaService.listarComprovantes(contaFixaId)).thenReturn(List.of(sampleAnexo(1L)));

        mockMvc.perform(delete("/api/contas-fixas/{id}/comprovantes/{anexoId}", contaFixaId, anexoId))
                .andExpect(status().isNotFound());
    }
}