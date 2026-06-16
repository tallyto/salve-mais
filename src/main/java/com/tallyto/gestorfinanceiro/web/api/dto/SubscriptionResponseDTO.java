package com.tallyto.gestorfinanceiro.web.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta da assinatura com URL de checkout")
public record SubscriptionResponseDTO(
        @Schema(description = "URL para concluir o pagamento")
        String checkoutUrl
) {}
