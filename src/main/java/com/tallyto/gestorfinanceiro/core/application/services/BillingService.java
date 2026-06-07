package com.tallyto.gestorfinanceiro.core.application.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.tallyto.gestorfinanceiro.api.dto.BillingStatusDTO;
import com.tallyto.gestorfinanceiro.api.dto.PlanoDTO;
import com.tallyto.gestorfinanceiro.api.dto.SubscriptionRequestDTO;
import com.tallyto.gestorfinanceiro.api.dto.SubscriptionResponseDTO;
import com.tallyto.gestorfinanceiro.config.stripe.StripeGateway;
import com.tallyto.gestorfinanceiro.core.domain.entities.Plano;
import com.tallyto.gestorfinanceiro.core.domain.entities.Tenant;
import com.tallyto.gestorfinanceiro.core.domain.enums.SubscriptionStatus;
import com.tallyto.gestorfinanceiro.core.domain.exceptions.BadRequestException;
import com.tallyto.gestorfinanceiro.core.domain.exceptions.ResourceNotFoundException;
import com.tallyto.gestorfinanceiro.core.infra.repositories.PlanoRepository;
import com.tallyto.gestorfinanceiro.core.infra.repositories.TenantRepository;
import com.tallyto.gestorfinanceiro.core.infra.repositories.TransacaoRepository;
import com.tallyto.gestorfinanceiro.core.infra.repositories.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class BillingService {

    private static final Logger log = LoggerFactory.getLogger(BillingService.class);

    @Autowired private TenantRepository tenantRepository;
    @Autowired private PlanoRepository planoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private TransacaoRepository transacaoRepository;
    @Autowired private StripeGateway stripeGateway;
    @Autowired private ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public java.util.List<PlanoDTO> listarPlanos() {
        return planoRepository.findByAtivoTrueOrderByPrecoMensalAsc().stream()
                .map(p -> new PlanoDTO(
                        p.getId(),
                        p.getNome(),
                        p.getDescricao(),
                        p.getTipo().name(),
                        p.getPrecoMensal(),
                        p.getMaxUsuarios(),
                        p.getMaxTransacoesMes(),
                        p.getMaxStorageGb()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public BillingStatusDTO getStatus(Tenant tenant) {
        Tenant fresh = tenantRepository.findById(tenant.getId()).orElse(tenant);
        Plano plano = planoRepository.findByTipo(fresh.getSubscriptionPlan()).orElse(null);

        Long diasRestantesTrial = fresh.getTrialEndDate() != null
                ? ChronoUnit.DAYS.between(LocalDateTime.now(), fresh.getTrialEndDate())
                : null;

        long usuariosAtivos = usuarioRepository.count();

        LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime fimMes    = inicioMes.plusMonths(1);
        long transacoesMes = transacaoRepository.countByDataBetween(inicioMes, fimMes);

        return new BillingStatusDTO(
                fresh.getSubscriptionStatus(),
                plano != null ? plano.getNome() : fresh.getSubscriptionPlan().name(),
                plano != null ? plano.getPrecoMensal() : null,
                fresh.getSubscriptionEndDate(),
                fresh.getTrialEndDate(),
                diasRestantesTrial,
                null,
                usuariosAtivos,
                plano != null ? plano.getMaxUsuarios() : fresh.getMaxUsers(),
                transacoesMes,
                plano != null ? plano.getMaxTransacoesMes() : null,
                plano != null ? plano.getMaxStorageGb() : fresh.getMaxStorageGb()
        );
    }

    @Transactional(readOnly = true)
    public SubscriptionResponseDTO assinar(Tenant tenant, SubscriptionRequestDTO dto) {
        Plano plano = planoRepository.findById(dto.planoId())
                .orElseThrow(() -> new ResourceNotFoundException("Plano não encontrado: " + dto.planoId()));

        if (!plano.getAtivo()) {
            throw new BadRequestException("Plano indisponível: " + plano.getNome());
        }

        if (plano.getStripePriceId() == null || plano.getStripePriceId().isBlank()) {
            throw new BadRequestException("Plano ainda não configurado no gateway de pagamento.");
        }

        Session session = stripeGateway.createCheckoutSession(
                plano.getStripePriceId(),
                tenant.getEmail(),
                tenant.getId().toString(),
                plano.getId().toString()
        );

        log.info("Stripe Checkout criado para tenant '{}' plano '{}' — session={}",
                tenant.getDomain(), plano.getNome(), session.getId());

        return new SubscriptionResponseDTO(session.getUrl());
    }

    @Transactional
    public void cancelar(Tenant tenant) {
        if (tenant.getStripeSubscriptionId() == null) {
            throw new BadRequestException("Tenant não possui assinatura ativa no gateway.");
        }

        stripeGateway.cancelSubscriptionAtPeriodEnd(tenant.getStripeSubscriptionId());

        log.info("Tenant '{}' agendou cancelamento — subscription={}",
                tenant.getDomain(), tenant.getStripeSubscriptionId());
    }

    @Transactional
    public void processarWebhook(String rawBody, String signatureHeader) {
        Event event = stripeGateway.constructWebhookEvent(rawBody, signatureHeader);
        log.info("Stripe webhook: type={} id={}", event.getType(), event.getId());

        switch (event.getType()) {
            case "checkout.session.completed"    -> processCheckoutCompleted(event);
            case "invoice.payment_succeeded"     -> processPaymentSucceeded(event);
            case "invoice.payment_failed"        -> processPaymentFailed(event);
            case "customer.subscription.deleted" -> processSubscriptionDeleted(event);
            default -> log.debug("Stripe webhook ignorado: {}", event.getType());
        }
    }

    private void processCheckoutCompleted(Event event) {
        Optional<StripeObject> obj = event.getDataObjectDeserializer().getObject();

        String tenantId, planoId, subscriptionId, customerId, sessionId;

        if (obj.isPresent()) {
            Session session = (Session) obj.get();
            tenantId       = session.getMetadata() != null ? session.getMetadata().get("tenantId") : null;
            planoId        = session.getMetadata() != null ? session.getMetadata().get("planoId")  : null;
            subscriptionId = session.getSubscription();
            customerId     = session.getCustomer();
            sessionId      = session.getId();
        } else {
            log.warn("checkout.session.completed: API version mismatch ({}), usando fallback JSON",
                    event.getApiVersion());
            try {
                JsonNode root = objectMapper.readTree(event.getDataObjectDeserializer().getRawJson());
                JsonNode meta = root.path("metadata");
                tenantId       = meta.path("tenantId").asText(null);
                planoId        = meta.path("planoId").asText(null);
                subscriptionId = root.path("subscription").asText(null);
                customerId     = root.path("customer").asText(null);
                sessionId      = root.path("id").asText(null);
            } catch (Exception e) {
                log.error("Falha no fallback de deserialização do checkout.session: {}", e.getMessage());
                return;
            }
        }

        if (tenantId == null) {
            log.warn("checkout.session.completed sem tenantId nos metadados — session={}", sessionId);
            return;
        }

        Tenant tenant = tenantRepository.findById(UUID.fromString(tenantId)).orElse(null);
        if (tenant == null) {
            log.warn("Tenant não encontrado para id={}", tenantId);
            return;
        }

        if (planoId != null) {
            planoRepository.findById(UUID.fromString(planoId)).ifPresent(plano -> {
                tenant.setSubscriptionPlan(plano.getTipo());
                tenant.setMaxUsers(plano.getMaxUsuarios());
                tenant.setMaxStorageGb(plano.getMaxStorageGb());
            });
        }

        tenant.setStripeSubscriptionId(subscriptionId);
        tenant.setStripeCustomerId(customerId);
        tenant.setSubscriptionStatus(SubscriptionStatus.ATIVO);
        tenant.setTrialEndDate(null);
        tenant.setSubscriptionStartDate(LocalDateTime.now());
        tenant.setSubscriptionEndDate(LocalDateTime.now().plusMonths(1));
        tenantRepository.save(tenant);

        log.info("Tenant '{}' ativado via checkout.session.completed", tenant.getDomain());
    }

    private void processPaymentSucceeded(Event event) {
        extractSubscriptionIdFromInvoice(event).ifPresent(subscriptionId ->
                tenantRepository.findByStripeSubscriptionId(subscriptionId).ifPresent(tenant -> {
                    tenant.setSubscriptionStatus(SubscriptionStatus.ATIVO);
                    tenant.setSubscriptionEndDate(LocalDateTime.now().plusMonths(1));
                    tenantRepository.save(tenant);
                    log.info("Tenant '{}' renovado via invoice.payment_succeeded", tenant.getDomain());
                })
        );
    }

    private void processPaymentFailed(Event event) {
        extractSubscriptionIdFromInvoice(event).ifPresent(subscriptionId ->
                tenantRepository.findByStripeSubscriptionId(subscriptionId).ifPresent(tenant -> {
                    tenant.setSubscriptionStatus(SubscriptionStatus.INADIMPLENTE);
                    tenantRepository.save(tenant);
                    log.warn("Tenant '{}' marcado INADIMPLENTE via invoice.payment_failed", tenant.getDomain());
                })
        );
    }

    private void processSubscriptionDeleted(Event event) {
        Optional<StripeObject> obj = event.getDataObjectDeserializer().getObject();
        if (obj.isEmpty()) return;

        Subscription subscription = (Subscription) obj.get();
        tenantRepository.findByStripeSubscriptionId(subscription.getId()).ifPresent(tenant -> {
            tenant.setSubscriptionStatus(SubscriptionStatus.CANCELADO);
            tenant.setStripeSubscriptionId(null);
            tenant.setSubscriptionEndDate(LocalDateTime.now());
            tenantRepository.save(tenant);
            log.warn("Tenant '{}' CANCELADO via customer.subscription.deleted", tenant.getDomain());
        });
    }

    private Optional<String> extractSubscriptionIdFromInvoice(Event event) {
        Optional<StripeObject> obj = event.getDataObjectDeserializer().getObject();
        if (obj.isEmpty()) return Optional.empty();
        Invoice invoice = (Invoice) obj.get();
        String subId = invoice.getSubscription();
        return (subId != null && !subId.isBlank()) ? Optional.of(subId) : Optional.empty();
    }
}
