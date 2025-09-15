package com.tallyto.gestorfinanceiro.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallyto.gestorfinanceiro.api.dto.CategoriaDTO;
import com.tallyto.gestorfinanceiro.core.application.services.CategoriaService;
import com.tallyto.gestorfinanceiro.core.domain.entities.Categoria;
import com.tallyto.gestorfinanceiro.testsupport.ControllerSliceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerSliceTest(controllers = CategoriaController.class)
public class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoriaService categoriaService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listarCategorias() throws Exception {
        Categoria c = new Categoria();
        c.setId(1L);
        c.setNome("Transporte");
        c.setTipo(Categoria.TipoCategoria.NECESSIDADE);
        when(categoriaService.listarCategorias()).thenReturn(List.of(c));
        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nome").value("Transporte"))
                .andExpect(jsonPath("$[0].tipo").value("NECESSIDADE"));
    }

    @Test
    void listarCategoriasPorTipo() throws Exception {
        Categoria necessidade = new Categoria();
        necessidade.setId(1L);
        necessidade.setNome("Aluguel");
        necessidade.setTipo(Categoria.TipoCategoria.NECESSIDADE);

        Categoria desejo = new Categoria();
        desejo.setId(2L);
        desejo.setNome("Cinema");
        desejo.setTipo(Categoria.TipoCategoria.DESEJO);

        when(categoriaService.listarCategoriasPorTipo()).thenReturn(Map.of(
                Categoria.TipoCategoria.NECESSIDADE, List.of(necessidade),
                Categoria.TipoCategoria.DESEJO, List.of(desejo)
        ));

        mockMvc.perform(get("/api/categorias/por-tipo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.NECESSIDADE[0].nome").value("Aluguel"))
                .andExpect(jsonPath("$.DESEJO[0].nome").value("Cinema"));
    }

    @Test
    void criarCategoria() throws Exception {
        CategoriaDTO dto = new CategoriaDTO(null, "Investimentos", Categoria.TipoCategoria.ECONOMIA);
        Categoria salvo = new Categoria();
        salvo.setId(10L);
        salvo.setNome("Investimentos");
        salvo.setTipo(Categoria.TipoCategoria.ECONOMIA);

        when(categoriaService.salvarCategoria(any(Categoria.class))).thenReturn(salvo);

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(".id").value(10))
                .andExpect(jsonPath(".nome").value("Investimentos"))
                .andExpect(jsonPath(".tipo").value("ECONOMIA"));
    }

    @Test
    void buscarCategoriaPorNome() throws Exception {
        Categoria c = new Categoria();
        c.setId(5L);
        c.setNome("Mercado");
        c.setTipo(Categoria.TipoCategoria.NECESSIDADE);

        when(categoriaService.buscarCategoriaPorNome("Mercado")).thenReturn(c);

        mockMvc.perform(get("/api/categorias/Mercado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(".id").value(5))
                .andExpect(jsonPath(".nome").value("Mercado"));
    }

    @Test
    void buscarCategoriaPorId() throws Exception {
        Categoria c = new Categoria();
        c.setId(6L);
        c.setNome("Lazer");
        c.setTipo(Categoria.TipoCategoria.DESEJO);

        when(categoriaService.buscaCategoriaPorId(6L)).thenReturn(c);

        mockMvc.perform(get("/api/categorias/id/6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(".id").value(6))
                .andExpect(jsonPath(".tipo").value("DESEJO"));
    }

    @Test
    void atualizarCategoria() throws Exception {
        CategoriaDTO dto = new CategoriaDTO(null, "Saúde", Categoria.TipoCategoria.NECESSIDADE);
        Categoria atualizado = new Categoria();
        atualizado.setId(7L);
        atualizado.setNome("Saúde");
        atualizado.setTipo(Categoria.TipoCategoria.NECESSIDADE);

        when(categoriaService.atualizarCategoria(eq(7L), any(Categoria.class))).thenReturn(atualizado);

        mockMvc.perform(put("/api/categorias/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(".id").value(7))
                .andExpect(jsonPath(".nome").value("Saúde"));
    }

    @Test
    void excluirCategoria() throws Exception {
        mockMvc.perform(delete("/api/categorias/8"))
                .andExpect(status().isNoContent());
    }
}
