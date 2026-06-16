package br.com.salvemais.infrastructure.config.stripe;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.SubscriptionUpdateParams;
import com.stripe.param.checkout.SessionCreateParams;
import br.com.salvemais.domain.exceptions.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;

@Component
public class StripeGateway {

    private static final Logger log = LoggerFactory.getLogger(StripeGateway.class);

    private final StripeProperties props;

    public StripeGateway(StripeProperties props) {
        this.props = props;
        Stripe.apiKey = props.getSecretKey();
    }

    public Session createCheckoutSession(String stripePriceId, String customerEmail,
                                         String tenantId, String planoId) {
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setCustomerEmail(customerEmail)
                    .setSuccessUrl(props.getSuccessUrl())
                    .setCancelUrl(props.getCancelUrl())
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setPrice(stripePriceId)
                            .setQuantity(1L)
                            .build())
                    .putAllMetadata(Map.of(
                            "tenantId", tenantId,
                            "planoId",  planoId
                    ))
                    .build();

            return Session.create(params);
        } catch (StripeException ex) {
            log.error("Stripe createCheckoutSession error: {}", ex.getMessage());
            throw new BadRequestException("Erro ao criar sessão de pagamento: " + ex.getMessage());
        }
    }

    public LocalDate cancelSubscriptionAtPeriodEnd(String subscriptionId) {
        try {
            Subscription subscription = Subscription.retrieve(subscriptionId);
            SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                    .setCancelAtPeriodEnd(true)
                    .build();
            Subscription updated = subscription.update(params);
            return Instant.ofEpochSecond(updated.getCurrentPeriodEnd())
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate();
        } catch (StripeException ex) {
            log.error("Stripe cancelSubscription error: {}", ex.getMessage());
            throw new BadRequestException("Erro ao cancelar assinatura: " + ex.getMessage());
        }
    }

    public Event constructWebhookEvent(String rawBody, String signatureHeader) {
        try {
            return Webhook.constructEvent(rawBody, signatureHeader, props.getWebhookSecret());
        } catch (SignatureVerificationException ex) {
            throw new BadRequestException("Assinatura do webhook Stripe inválida.");
        }
    }
}
