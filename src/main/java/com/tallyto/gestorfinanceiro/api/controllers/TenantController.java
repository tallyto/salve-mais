package com.tallyto.gestorfinanceiro.api.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tallyto.gestorfinanceiro.api.dto.*;
import com.tallyto.gestorfinanceiro.core.application.services.TenantService;
import com.tallyto.gestorfinanceiro.core.domain.entities.Tenant;
import com.tallyto.gestorfinanceiro.mappers.TenantMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Tenants", description = "API de Tenants")
@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    @Autowired
    private TenantMapper tenantMapper;

    @Autowired
    private TenantService tenantService;

    @GetMapping
    public List<TenantResponseDTO> getAllTenants() {
        var tenants = tenantService.findAll();
        return tenantMapper.toListDTO(tenants);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TenantResponseDTO> getTenantById(@PathVariable UUID id) {
        Tenant tenant = tenantService.findById(id);
        return ResponseEntity.ok(tenantMapper.toDTO(tenant));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTenant(@PathVariable UUID id) {
        tenantService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cadastro")
    public ResponseEntity<Map<String, String>> cadastrarTenant(
            @Valid @RequestBody TenantCadastroDTO tenantCadastroDTO) {
        tenantService.cadastrarTenant(tenantCadastroDTO);

        Map<String, String> response = new HashMap<>();
        response.put("message",
                "Solicitação de cadastro enviada! Por favor, verifique seu e-mail para confirmar o registro.");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/verificar")
    public ResponseEntity<?> verificarToken(@RequestParam String token) {
        boolean valido = tenantService.verificarToken(token);

        if (!valido) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Token inválido ou expirado");
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirmar")
    public ResponseEntity<Map<String, Object>> confirmarTenant(@RequestBody Map<String, String> requestBody) {
        String token = requestBody.get("token");

        if (token == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Token não fornecido");
            return ResponseEntity.badRequest().body(response);
        }

        Tenant tenant = tenantService.confirmarTenant(token);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Tenant confirmado com sucesso!");
        response.put("dominio", tenant.getDomain());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/verificar-dominio")
    public ResponseEntity<Boolean> verificarDominioDisponivel(@RequestParam String dominio) {
        boolean disponivel = tenantService.verificarDominioDisponivel(dominio);
        return ResponseEntity.ok(disponivel);
    }
    
    // Endpoints de customização do tenant
    
    @PutMapping("/{id}/subscription")
    @Operation(summary = "Atualizar plano de assinatura do tenant")
    public ResponseEntity<TenantResponseDTO> updateSubscription(
            @PathVariable UUID id,
            @Valid @RequestBody TenantSubscriptionDTO subscriptionDTO) {
        Tenant tenant = tenantService.updateSubscription(id, subscriptionDTO);
        return ResponseEntity.ok(tenantMapper.toDTO(tenant));
    }
    
    @PutMapping("/{id}/smtp")
    @Operation(summary = "Atualizar configurações SMTP do tenant")
    public ResponseEntity<TenantResponseDTO> updateSmtpConfig(
            @PathVariable UUID id,
            @Valid @RequestBody TenantSmtpConfigDTO smtpConfigDTO) {
        Tenant tenant = tenantService.updateSmtpConfig(id, smtpConfigDTO);
        return ResponseEntity.ok(tenantMapper.toDTO(tenant));
    }
    
    @PutMapping("/{id}/regional-settings")
    @Operation(summary = "Atualizar configurações regionais do tenant")
    public ResponseEntity<TenantResponseDTO> updateRegionalSettings(
            @PathVariable UUID id,
            @Valid @RequestBody TenantRegionalSettingsDTO regionalSettingsDTO) {
        Tenant tenant = tenantService.updateRegionalSettings(id, regionalSettingsDTO);
        return ResponseEntity.ok(tenantMapper.toDTO(tenant));
    }
    
    @GetMapping("/domain/{domain}")
    @Operation(summary = "Buscar tenant por domínio")
    public ResponseEntity<TenantResponseDTO> getTenantByDomain(@PathVariable String domain) {
        Tenant tenant = tenantService.findByDomain(domain);
        return ResponseEntity.ok(tenantMapper.toDTO(tenant));
    }
}
