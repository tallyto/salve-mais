package com.tallyto.gestorfinanceiro.web.api.controllers;

import com.tallyto.gestorfinanceiro.web.api.dto.BillingStatusDTO;
import com.tallyto.gestorfinanceiro.web.api.dto.PlanoDTO;
import com.tallyto.gestorfinanceiro.web.api.dto.SubscriptionRequestDTO;
import com.tallyto.gestorfinanceiro.web.api.dto.SubscriptionResponseDTO;
import com.tallyto.gestorfinanceiro.infrastructure.context.TenantContext;
import com.tallyto.gestorfinanceiro.application.services.BillingService;
import com.tallyto.gestorfinanceiro.application.services.TenantService;
import com.tallyto.gestorfinanceiro.domain.entities.Tenant;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Billing", description = "Gestão de assinatura do tenant")
@RestController
@RequestMapping("/api/billing")
public class BillingController {

    @Autowired private BillingService billingService;
    @Autowired private TenantService tenantService;

    @GetMapping("/planos")
    @Operation(summary = "Listar planos disponíveis")
    public ResponseEntity<java.util.List<PlanoDTO>> planos() {
        return ResponseEntity.ok(billingService.listarPlanos());
    }

    @GetMapping("/status")
    @Operation(summary = "Obter status de billing do tenant")
    public ResponseEntity<BillingStatusDTO> status() {
        Tenant tenant = tenantService.findByDomain(TenantContext.getCurrentTenant());
        return ResponseEntity.ok(billingService.getStatus(tenant));
    }

    @PostMapping("/assinar")
    @Operation(summary = "Assinar um plano")
    public ResponseEntity<SubscriptionResponseDTO> assinar(@Valid @RequestBody SubscriptionRequestDTO dto) {
        Tenant tenant = tenantService.findByDomain(TenantContext.getCurrentTenant());
        return ResponseEntity.ok(billingService.assinar(tenant, dto));
    }

    @DeleteMapping("/cancelar")
    @Operation(summary = "Cancelar assinatura do tenant")
    public ResponseEntity<Void> cancelar() {
        Tenant tenant = tenantService.findByDomain(TenantContext.getCurrentTenant());
        billingService.cancelar(tenant);
        return ResponseEntity.noContent().build();
    }
}
