package com.tallyto.gestorfinanceiro.application.services;

import com.tallyto.gestorfinanceiro.domain.entities.Plano;
import com.tallyto.gestorfinanceiro.domain.entities.Tenant;
import com.tallyto.gestorfinanceiro.domain.entities.Tenant.SubscriptionPlan;
import com.tallyto.gestorfinanceiro.domain.enums.SubscriptionStatus;
import com.tallyto.gestorfinanceiro.domain.exceptions.BadRequestException;
import com.tallyto.gestorfinanceiro.domain.exceptions.ResourceNotFoundException;
import com.tallyto.gestorfinanceiro.infrastructure.repositories.PlanoRepository;
import com.tallyto.gestorfinanceiro.infrastructure.repositories.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SubscriptionService {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private PlanoRepository planoRepository;

    @Autowired
    private EmailService emailService;

    public List<Plano> listarPlanos() {
        return planoRepository.findAll().stream()
                .filter(Plano::getAtivo)
                .toList();
    }

    public Plano buscarPlanoPorTipo(SubscriptionPlan tipo) {
        return planoRepository.findByTipo(tipo)
                .orElseThrow(() -> new ResourceNotFoundException("Plano não encontrado: " + tipo));
    }

    @Transactional
    public Tenant ativarAssinatura(UUID tenantId, SubscriptionPlan plano, String stripeSubscriptionId, String stripeCustomerId) {
        Tenant tenant = buscarTenant(tenantId);

        tenant.setSubscriptionStatus(SubscriptionStatus.ATIVO);
        tenant.setSubscriptionPlan(plano);
        tenant.setSubscriptionStartDate(LocalDateTime.now());
        tenant.setSubscriptionEndDate(LocalDateTime.now().plusMonths(1));
        tenant.setStripeSubscriptionId(stripeSubscriptionId);
        tenant.setStripeCustomerId(stripeCustomerId);
        tenant.setTrialEndDate(null);

        return tenantRepository.save(tenant);
    }

    @Transactional
    public Tenant marcarComoInadimplente(UUID tenantId) {
        Tenant tenant = buscarTenant(tenantId);

        if (tenant.getSubscriptionStatus() == SubscriptionStatus.CANCELADO) {
            throw new BadRequestException("Assinatura já cancelada.");
        }

        tenant.setSubscriptionStatus(SubscriptionStatus.INADIMPLENTE);
        Tenant saved = tenantRepository.save(tenant);

        emailService.enviarEmailHtml(
                tenant.getEmail(),
                "Salve Mais - Problema com seu pagamento",
                "inadimplente.html",
                tenant.getName(),
                "https://www.salvemais.com.br/#/billing"
        );

        return saved;
    }

    @Transactional
    public Tenant cancelarAssinatura(UUID tenantId) {
        Tenant tenant = buscarTenant(tenantId);

        tenant.setSubscriptionStatus(SubscriptionStatus.CANCELADO);
        tenant.setSubscriptionEndDate(LocalDateTime.now());
        tenant.setStripeSubscriptionId(null);

        return tenantRepository.save(tenant);
    }

    @Transactional
    public Tenant reativarAssinatura(UUID tenantId) {
        Tenant tenant = buscarTenant(tenantId);

        if (tenant.getSubscriptionStatus() != SubscriptionStatus.INADIMPLENTE) {
            throw new BadRequestException("Só é possível reativar assinaturas inadimplentes.");
        }

        tenant.setSubscriptionStatus(SubscriptionStatus.ATIVO);
        tenant.setSubscriptionEndDate(LocalDateTime.now().plusMonths(1));

        return tenantRepository.save(tenant);
    }

    @Transactional
    public void expirarTrialsVencidos() {
        List<Tenant> trialsVencidos = tenantRepository
                .findBySubscriptionStatusAndTrialEndDateBefore(SubscriptionStatus.TRIAL, LocalDateTime.now());

        for (Tenant tenant : trialsVencidos) {
            tenant.setSubscriptionStatus(SubscriptionStatus.INADIMPLENTE);
            tenantRepository.save(tenant);

            emailService.enviarEmailHtml(
                    tenant.getEmail(),
                    "Salve Mais - Seu período de teste encerrou",
                    "trial-expirado.html",
                    tenant.getName(),
                    "https://www.salvemais.com.br/#/billing"
            );
        }
    }

    public boolean tenantEstaAtivo(String domain) {
        Tenant tenant = tenantRepository.findByDomain(domain)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant não encontrado: " + domain));

        SubscriptionStatus status = tenant.getSubscriptionStatus();
        return status == SubscriptionStatus.TRIAL || status == SubscriptionStatus.ATIVO;
    }

    public Tenant buscarTenantPorStripeCustomerId(String stripeCustomerId) {
        return tenantRepository.findByStripeCustomerId(stripeCustomerId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant não encontrado para o customer: " + stripeCustomerId));
    }

    public Tenant buscarTenantPorStripeSubscriptionId(String stripeSubscriptionId) {
        return tenantRepository.findByStripeSubscriptionId(stripeSubscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant não encontrado para a subscription: " + stripeSubscriptionId));
    }

    private Tenant buscarTenant(UUID tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant não encontrado: " + tenantId));
    }
}
