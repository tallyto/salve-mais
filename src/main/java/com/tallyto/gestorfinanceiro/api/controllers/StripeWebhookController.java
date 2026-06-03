package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.core.application.services.BillingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Webhooks", description = "Recepção de eventos do gateway de pagamento")
@RestController
@RequestMapping("/api/webhook/stripe")
public class StripeWebhookController {

    @Autowired
    private BillingService billingService;

    @PostMapping
    public ResponseEntity<Void> receberEvento(
            @RequestBody String rawBody,
            @RequestHeader("Stripe-Signature") String signature) {

        billingService.processarWebhook(rawBody, signature);
        return ResponseEntity.ok().build();
    }
}
