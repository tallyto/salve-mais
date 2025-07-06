package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.api.dto.TenantCadastroDTO;
import com.tallyto.gestorfinanceiro.api.dto.TenantDTO;
import com.tallyto.gestorfinanceiro.core.domain.entities.Tenant;
import com.tallyto.gestorfinanceiro.core.domain.exceptions.BadRequestException;
import com.tallyto.gestorfinanceiro.core.domain.exceptions.ResourceNotFoundException;
import com.tallyto.gestorfinanceiro.core.infra.repositories.TenantRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
        String link = "http://localhost:4200/#/register?token=" + token;
        String mensagem = "Olá " + tenant.getName() + ",\n\n" +
                "Obrigado por se cadastrar no Gestor Financeiro. Para confirmar seu tenant, clique no link abaixo:\n\n" +
                link + "\n\n" +
                "Atenciosamente,\n" +
                "Equipe Gestor Financeiro";
        
        emailService.enviarEmail(tenant.getEmail(), "Confirme seu Tenant no Gestor Financeiro", mensagem);
        
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
}
