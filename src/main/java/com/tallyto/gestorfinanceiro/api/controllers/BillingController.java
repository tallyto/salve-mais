package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.api.dto.BillingStatusDTO;
import com.tallyto.gestorfinanceiro.api.dto.SubscriptionRequestDTO;
import com.tallyto.gestorfinanceiro.api.dto.SubscriptionResponseDTO;
import com.tallyto.gestorfinanceiro.context.TenantContext;
import com.tallyto.gestorfinanceiro.core.application.services.BillingService;
import com.tallyto.gestorfinanceiro.core.application.services.TenantService;
import com.tallyto.gestorfinanceiro.core.domain.entities.Tenant;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @GetMapping("/status")
    public ResponseEntity<BillingStatusDTO> status() {
        Tenant tenant = tenantService.findByDomain(TenantContext.getCurrentTenant());
        return ResponseEntity.ok(billingService.getStatus(tenant));
    }

    @PostMapping("/assinar")
    public ResponseEntity<SubscriptionResponseDTO> assinar(@Valid @RequestBody SubscriptionRequestDTO dto) {
        Tenant tenant = tenantService.findByDomain(TenantContext.getCurrentTenant());
        return ResponseEntity.ok(billingService.assinar(tenant, dto));
    }

    @DeleteMapping("/cancelar")
    public ResponseEntity<Void> cancelar() {
        Tenant tenant = tenantService.findByDomain(TenantContext.getCurrentTenant());
        billingService.cancelar(tenant);
        return ResponseEntity.noContent().build();
    }
}
