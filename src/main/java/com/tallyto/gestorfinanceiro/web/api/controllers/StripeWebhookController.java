package com.tallyto.gestorfinanceiro.web.api.controllers;

import com.tallyto.gestorfinanceiro.application.services.BillingService;
import com.tallyto.gestorfinanceiro.infrastructure.config.openapi.OpenApiPublic;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Webhooks", description = "Recepção de eventos do gateway de pagamento")
@OpenApiPublic
@RestController
@RequestMapping("/api/webhook/stripe")
public class StripeWebhookController {

    @Autowired
    private BillingService billingService;

    @PostMapping
    @Operation(summary = "Receber evento da Stripe")
    public ResponseEntity<Void> receberEvento(
            @RequestBody String rawBody,
            @RequestHeader("Stripe-Signature") String signature) {

        billingService.processarWebhook(rawBody, signature);
        return ResponseEntity.ok().build();
    }
}
