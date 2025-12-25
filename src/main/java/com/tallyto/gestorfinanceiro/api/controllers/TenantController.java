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
    
    @PutMapping("/{id}/basic-info")
    @Operation(summary = "Atualizar informações básicas do tenant")
    public ResponseEntity<TenantResponseDTO> updateBasicInfo(
            @PathVariable UUID id,
            @Valid @RequestBody TenantBasicInfoDTO basicInfoDTO) {
        Tenant tenant = tenantService.updateBasicInfo(id, basicInfoDTO);
        return ResponseEntity.ok(tenantMapper.toDTO(tenant));
    }
    
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

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar tenant (ativar/desativar)")
    public ResponseEntity<TenantResponseDTO> updateTenant(
            @PathVariable UUID id,
            @Valid @RequestBody TenantUpdateDTO updateDTO) {
        Tenant tenant = tenantService.updateTenant(id, updateDTO);
        return ResponseEntity.ok(tenantMapper.toDTO(tenant));
    }

    @GetMapping("/stats")
    @Operation(summary = "Obter estatísticas dos tenants")
    public ResponseEntity<TenantStatsDTO> getStats() {
        TenantStatsDTO stats = tenantService.getStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{id}/usuarios")
    @Operation(summary = "Listar usuários do tenant")
    public ResponseEntity<List<UsuarioTenantDTO>> getUsuariosByTenant(@PathVariable UUID id) {
        List<UsuarioTenantDTO> usuarios = tenantService.getUsuariosByTenant(id);
        return ResponseEntity.ok(usuarios);
    }
    
    @PostMapping("/{id}/enviar-lembrete-usuario")
    @Operation(summary = "Enviar email lembrando o tenant de criar um usuário")
    public ResponseEntity<Map<String, String>> enviarLembreteCriarUsuario(@PathVariable UUID id) {
        tenantService.enviarLembreteCriarUsuario(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Email de lembrete enviado com sucesso!");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/reenviar-token-usuario")
    @Operation(summary = "Reenviar token para criação de usuário (caso tenha expirado)")
    public ResponseEntity<Map<String, String>> reenviarTokenCriarUsuario(@PathVariable UUID id) {
        tenantService.reenviarTokenCriarUsuario(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Novo token gerado e email enviado com sucesso!");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/verificar-token-usuario")
    @Operation(summary = "Verificar token para criação de usuário")
    public ResponseEntity<TenantResponseDTO> verificarTokenCriarUsuario(@RequestParam String token) {
        Tenant tenant = tenantService.verificarTokenCriarUsuario(token);
        return ResponseEntity.ok(tenantMapper.toDTO(tenant));
    }

    @PutMapping("/{id}/toggle-status")
    @Operation(summary = "Alternar status (ativo/inativo) do tenant")
    public ResponseEntity<Map<String, String>> toggleTenantStatus(@PathVariable UUID id) {
        tenantService.toggleTenantStatus(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Status do tenant alterado com sucesso");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{tenantId}/usuarios/{usuarioId}/toggle-status")
    @Operation(summary = "Alternar status (ativo/inativo) de um usuário")
    public ResponseEntity<Map<String, String>> toggleUsuarioStatus(
            @PathVariable UUID tenantId,
            @PathVariable Long usuarioId) {
        tenantService.toggleUsuarioStatus(tenantId, usuarioId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Status do usuário alterado com sucesso");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{tenantId}/usuarios/{usuarioId}/reset-senha")
    @Operation(summary = "Enviar email para resetar senha de um usuário específico")
    public ResponseEntity<Map<String, String>> enviarResetSenha(
            @PathVariable UUID tenantId,
            @PathVariable Long usuarioId) {
        tenantService.enviarResetSenhaUsuario(tenantId, usuarioId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Email de reset de senha enviado com sucesso");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/email-boas-vindas")
    @Operation(summary = "Enviar email de boas-vindas para o tenant")
    public ResponseEntity<Map<String, String>> enviarEmailBoasVindas(@PathVariable UUID id) {
        tenantService.enviarEmailBoasVindas(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Email de boas-vindas enviado com sucesso");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/usuarios/reset-todas-senhas")
    @Operation(summary = "Enviar email de reset de senha para todos os usuários ativos")
    public ResponseEntity<Map<String, String>> resetarTodasSenhas(@PathVariable UUID id) {
        tenantService.resetarTodasSenhas(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Emails de reset enviados para todos os usuários ativos");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/usuarios/desativar-todos")
    @Operation(summary = "Desativar todos os usuários do tenant")
    public ResponseEntity<Map<String, String>> desativarTodosUsuarios(@PathVariable UUID id) {
        tenantService.desativarTodosUsuarios(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Todos os usuários foram desativados");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/usuarios/ativar-todos")
    @Operation(summary = "Ativar todos os usuários do tenant")
    public ResponseEntity<Map<String, String>> ativarTodosUsuarios(@PathVariable UUID id) {
        tenantService.ativarTodosUsuarios(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Todos os usuários foram ativados");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/exportar")
    @Operation(summary = "Exportar todos os dados do tenant")
    public ResponseEntity<Map<String, Object>> exportarDados(@PathVariable UUID id) {
        Map<String, Object> dados = tenantService.exportarDadosTenant(id);
        return ResponseEntity.ok(dados);
    }
}

