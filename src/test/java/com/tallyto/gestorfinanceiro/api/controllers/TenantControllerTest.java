package com.tallyto.gestorfinanceiro.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallyto.gestorfinanceiro.api.dto.TenantCadastroDTO;
import com.tallyto.gestorfinanceiro.api.dto.TenantResponseDTO;
import com.tallyto.gestorfinanceiro.core.application.services.TenantService;
import com.tallyto.gestorfinanceiro.core.domain.entities.Tenant;
import com.tallyto.gestorfinanceiro.mappers.TenantMapper;
import com.tallyto.gestorfinanceiro.testsupport.ControllerSliceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerSliceTest(controllers = TenantController.class)
class TenantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TenantService tenantService;

    @MockBean
    private TenantMapper tenantMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private Tenant tenant(UUID id) {
        Tenant t = new Tenant();
        t.setId(id);
        t.setDomain("empresa.com");
        t.setName("Empresa XYZ");
        t.setEmail("contato@empresa.com");
        t.setPhoneNumber("+5511999999999");
        t.setAddress("Rua Exemplo, 123");
        return t;
    }

    private TenantResponseDTO dto(UUID id) {
        return new TenantResponseDTO(
                id.toString(), 
                "empresa.com", 
                "Empresa XYZ", 
                "contato@empresa.com", 
                "+5511999999999", 
                "Rua Exemplo, 123",
                "Empresa XYZ",
                null,
                null,
                Tenant.SubscriptionPlan.FREE,
                null,
                null,
                null,
                null,
                null,
                null,
                "America/Sao_Paulo",
                "pt_BR",
                "BRL",
                "dd/MM/yyyy",
                true
        );
    }

    @Test
    @DisplayName("GET /api/tenants retorna lista de tenants")
    void getAllTenants() throws Exception {
        UUID id = UUID.randomUUID();
        List<Tenant> entities = List.of(tenant(id));
        List<TenantResponseDTO> dtos = List.of(dto(id));

        Mockito.when(tenantService.findAll()).thenReturn(entities);
        Mockito.when(tenantMapper.toListDTO(entities)).thenReturn(dtos);

        mockMvc.perform(get("/api/tenants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].domain").value("empresa.com"));
    }

    @Test
    @DisplayName("GET /api/tenants/{id} retorna tenant por id")
    void getTenantById() throws Exception {
        UUID id = UUID.randomUUID();
        Tenant entity = tenant(id);
        TenantResponseDTO dto = dto(id);

        Mockito.when(tenantService.findById(id)).thenReturn(entity);
        Mockito.when(tenantMapper.toDTO(entity)).thenReturn(dto);

        mockMvc.perform(get("/api/tenants/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Empresa XYZ"));
    }

    @Test
    @DisplayName("DELETE /api/tenants/{id} exclui tenant")
    void deleteTenant() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/tenants/{id}", id))
                .andExpect(status().isNoContent());
        Mockito.verify(tenantService).delete(id);
    }

    @Test
    @DisplayName("POST /api/tenants/cadastro envia cadastro e retorna mensagem")
    void cadastrarTenant() throws Exception {
        TenantCadastroDTO cadastro = new TenantCadastroDTO("Empresa XYZ", "empresa.com", "contato@empresa.com");

        mockMvc.perform(post("/api/tenants/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cadastro)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("Solicitação de cadastro enviada")));

        Mockito.verify(tenantService).cadastrarTenant(Mockito.any(TenantCadastroDTO.class));
    }

    @Test
    @DisplayName("GET /api/tenants/verificar com token invalido retorna 400 e erro")
    void verificarTokenInvalido() throws Exception {
        Mockito.when(tenantService.verificarToken("bad")).thenReturn(false);
        mockMvc.perform(get("/api/tenants/verificar").param("token", "bad"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Token inválido ou expirado"));
    }

    @Test
    @DisplayName("GET /api/tenants/verificar com token valido retorna 200")
    void verificarTokenValido() throws Exception {
        Mockito.when(tenantService.verificarToken("ok")).thenReturn(true);
        mockMvc.perform(get("/api/tenants/verificar").param("token", "ok"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/tenants/confirmar confirma tenant com token")
    void confirmarTenant() throws Exception {
        Tenant entity = tenant(UUID.randomUUID());
        entity.setDomain("empresa.com");
        Mockito.when(tenantService.confirmarTenant("tok"))
                .thenReturn(entity);

        Map<String, String> body = Map.of("token", "tok");
        mockMvc.perform(post("/api/tenants/confirmar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tenant confirmado com sucesso!"))
                .andExpect(jsonPath("$.dominio").value("empresa.com"));
    }

    @Test
    @DisplayName("POST /api/tenants/confirmar sem token retorna 400")
    void confirmarTenantSemToken() throws Exception {
        Map<String, String> body = Map.of();
        mockMvc.perform(post("/api/tenants/confirmar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Token não fornecido"));
    }

    @Test
    @DisplayName("GET /api/tenants/verificar-dominio retorna disponibilidade")
    void verificarDominioDisponivel() throws Exception {
        Mockito.when(tenantService.verificarDominioDisponivel("empresa.com")).thenReturn(true);
        mockMvc.perform(get("/api/tenants/verificar-dominio").param("dominio", "empresa.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
