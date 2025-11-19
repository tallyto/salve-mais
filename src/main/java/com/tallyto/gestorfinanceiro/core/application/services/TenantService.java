package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.api.dto.*;
import com.tallyto.gestorfinanceiro.core.domain.entities.Tenant;
import com.tallyto.gestorfinanceiro.core.domain.exceptions.BadRequestException;
import com.tallyto.gestorfinanceiro.core.domain.exceptions.ResourceNotFoundException;
import com.tallyto.gestorfinanceiro.core.infra.repositories.TenantRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import com.tallyto.gestorfinanceiro.util.Utils;

@Service
public class TenantService {

    @Autowired
    private TenantRepository tenantRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Value("${app.confirmation.url}")
    private String confirmationUrl;

    public Tenant cadastrarTenant(TenantCadastroDTO dto) {
        // Validar se o domínio já existe
        if (tenantRepository.findByDomain(dto.domain()).isPresent()) {
            throw new BadRequestException("Domínio já está em uso");
        }
        
        // Validar se o email já existe
        if (tenantRepository.findByEmail(dto.email()).isPresent()) {
            throw new BadRequestException("Email já está em uso");
        }
        
        // Criar o tenant
        Tenant tenant = new Tenant();
        tenant.setDomain(dto.domain());
        tenant.setName(dto.name());
        tenant.setEmail(dto.email());
        tenant.setActive(false);
        
        // Gerar token de confirmação
        String token = UUID.randomUUID().toString();
        tenant.setConfirmationToken(token);
        
        // Salvar o tenant
        tenant = tenantRepository.save(tenant);
        
        // Enviar email de confirmação
        String link = confirmationUrl + "?token=" + token;
        emailService.enviarEmailHtml(
            tenant.getEmail(), 
            "Confirme seu Tenant no Salve Mais", 
            "confirmacao-tenant.html",
            tenant.getName(),
            link
        );
        
        return tenant;
    }
    
    public boolean verificarToken(String token) {
        return tenantRepository.findByConfirmationToken(token).isPresent();
    }
    
    public Tenant confirmarTenant(String token) {
        Tenant tenant = tenantRepository.findByConfirmationToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token inválido ou expirado"));
        
        tenant.setActive(true);
        tenant.setConfirmationToken(null); // Invalidar o token após o uso
        
        return tenantRepository.save(tenant);
    }
    
    public boolean verificarDominioDisponivel(String dominio) {
        return !tenantRepository.findByDomain(dominio).isPresent();
    }
    
    // Novos métodos para substituir o acesso direto ao repository
    
    public List<Tenant> findAll() {
        return tenantRepository.findAll();
    }
    
    public Tenant findById(UUID id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id " + id));
    }
    
    public Tenant save(Tenant tenant) {
        return tenantRepository.save(tenant);
    }
    
    public Tenant update(UUID id, TenantDTO tenantDTO) {
        Tenant tenant = findById(id);
        BeanUtils.copyProperties(tenantDTO, tenant, Utils.getNullPropertyNames(tenantDTO));
        return tenantRepository.save(tenant);
    }
    
    public void delete(UUID id) {
        Tenant tenant = findById(id);
        tenantRepository.delete(tenant);
    }
    
    // Métodos para customização do tenant
    
    public Tenant updateSubscription(UUID id, TenantSubscriptionDTO subscriptionDTO) {
        Tenant tenant = findById(id);
        
        if (subscriptionDTO.getSubscriptionPlan() != null) {
            tenant.setSubscriptionPlan(subscriptionDTO.getSubscriptionPlan());
        }
        if (subscriptionDTO.getMaxUsers() != null) {
            tenant.setMaxUsers(subscriptionDTO.getMaxUsers());
        }
        if (subscriptionDTO.getMaxStorageGb() != null) {
            tenant.setMaxStorageGb(subscriptionDTO.getMaxStorageGb());
        }
        if (subscriptionDTO.getTrialEndDate() != null) {
            tenant.setTrialEndDate(subscriptionDTO.getTrialEndDate());
        }
        if (subscriptionDTO.getSubscriptionStartDate() != null) {
            tenant.setSubscriptionStartDate(subscriptionDTO.getSubscriptionStartDate());
        }
        if (subscriptionDTO.getSubscriptionEndDate() != null) {
            tenant.setSubscriptionEndDate(subscriptionDTO.getSubscriptionEndDate());
        }
        if (subscriptionDTO.getEnabledFeatures() != null) {
            tenant.setEnabledFeatures(subscriptionDTO.getEnabledFeatures());
        }
        
        return tenantRepository.save(tenant);
    }
    
    public Tenant updateSmtpConfig(UUID id, TenantSmtpConfigDTO smtpConfigDTO) {
        Tenant tenant = findById(id);
        
        tenant.setCustomSmtpHost(smtpConfigDTO.getHost());
        tenant.setCustomSmtpPort(smtpConfigDTO.getPort());
        tenant.setCustomSmtpUser(smtpConfigDTO.getUser());
        tenant.setCustomSmtpPassword(smtpConfigDTO.getPassword());
        tenant.setCustomSmtpFromEmail(smtpConfigDTO.getFromEmail());
        tenant.setCustomSmtpFromName(smtpConfigDTO.getFromName());
        
        return tenantRepository.save(tenant);
    }
    
    public Tenant updateRegionalSettings(UUID id, TenantRegionalSettingsDTO regionalSettingsDTO) {
        Tenant tenant = findById(id);
        
        if (regionalSettingsDTO.getTimezone() != null) {
            tenant.setTimezone(regionalSettingsDTO.getTimezone());
        }
        if (regionalSettingsDTO.getLocale() != null) {
            tenant.setLocale(regionalSettingsDTO.getLocale());
        }
        if (regionalSettingsDTO.getCurrencyCode() != null) {
            tenant.setCurrencyCode(regionalSettingsDTO.getCurrencyCode());
        }
        if (regionalSettingsDTO.getDateFormat() != null) {
            tenant.setDateFormat(regionalSettingsDTO.getDateFormat());
        }
        
        return tenantRepository.save(tenant);
    }
    
    public Tenant findByDomain(String domain) {
        return tenantRepository.findByDomain(domain)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with domain " + domain));
    }
}
