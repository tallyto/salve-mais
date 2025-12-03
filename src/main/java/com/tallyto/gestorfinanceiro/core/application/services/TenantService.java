package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.api.dto.*;
import com.tallyto.gestorfinanceiro.context.TenantContext;
import com.tallyto.gestorfinanceiro.core.domain.entities.Tenant;
import com.tallyto.gestorfinanceiro.core.domain.entities.Usuario;
import com.tallyto.gestorfinanceiro.core.domain.exceptions.BadRequestException;
import com.tallyto.gestorfinanceiro.core.domain.exceptions.ResourceNotFoundException;
import com.tallyto.gestorfinanceiro.core.infra.repositories.TenantRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.UUID;

import com.tallyto.gestorfinanceiro.util.Utils;

@Service
public class TenantService {

    @Autowired
    private TenantRepository tenantRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private com.tallyto.gestorfinanceiro.core.infra.repositories.UsuarioRepository usuarioRepository;
    
    @Autowired
    private DataSource dataSource;
    
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
    
    public Tenant updateBasicInfo(UUID id, TenantBasicInfoDTO basicInfoDTO) {
        Tenant tenant = findById(id);
        
        if (basicInfoDTO.getName() != null) {
            tenant.setName(basicInfoDTO.getName());
        }
        if (basicInfoDTO.getEmail() != null) {
            tenant.setEmail(basicInfoDTO.getEmail());
        }
        if (basicInfoDTO.getPhoneNumber() != null) {
            tenant.setPhoneNumber(basicInfoDTO.getPhoneNumber());
        }
        if (basicInfoDTO.getAddress() != null) {
            tenant.setAddress(basicInfoDTO.getAddress());
        }
        if (basicInfoDTO.getDisplayName() != null) {
            tenant.setDisplayName(basicInfoDTO.getDisplayName());
        }
        if (basicInfoDTO.getLogoUrl() != null) {
            tenant.setLogoUrl(basicInfoDTO.getLogoUrl());
        }
        if (basicInfoDTO.getFaviconUrl() != null) {
            tenant.setFaviconUrl(basicInfoDTO.getFaviconUrl());
        }
        
        return tenantRepository.save(tenant);
    }
    
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

    public Tenant updateTenant(UUID id, TenantUpdateDTO updateDTO) {
        Tenant tenant = findById(id);
        
        if (updateDTO.getActive() != null) {
            tenant.setActive(updateDTO.getActive());
        }
        if (updateDTO.getName() != null) {
            tenant.setName(updateDTO.getName());
        }
        if (updateDTO.getEmail() != null) {
            tenant.setEmail(updateDTO.getEmail());
        }
        if (updateDTO.getPhoneNumber() != null) {
            tenant.setPhoneNumber(updateDTO.getPhoneNumber());
        }
        if (updateDTO.getAddress() != null) {
            tenant.setAddress(updateDTO.getAddress());
        }
        
        return tenantRepository.save(tenant);
    }

    /**
     * Calcula estatísticas gerais dos tenants.
     * 
     * IMPORTANTE: O cálculo de totalUsers itera por TODOS os schemas.
     * 
     * Fluxo:
     * 1. Busca todos os tenants no schema público
     * 2. Calcula totais simples (total, ativos, inativos)
     * 3. Para cada tenant:
     *    - Troca para o schema do tenant
     *    - Conta os usuários naquele schema
     *    - Acumula no total
     *    - Restaura o contexto (sempre no finally)
     * 
     * PERFORMANCE: Com muitos tenants, considere:
     * - Cache das estatísticas
     * - Cálculo assíncrono em background
     * - Atualização periódica ao invés de tempo real
     * 
     * @return DTO com estatísticas dos tenants e total de usuários
     */
    /**
     * Calcula estatísticas gerais dos tenants.
     * 
     * IMPORTANTE: O cálculo de totalUsers itera por TODOS os schemas.
     * 
     * Fluxo:
     * 1. Busca todos os tenants no schema público
     * 2. Calcula totais simples (total, ativos, inativos)
     * 3. Para cada tenant:
     *    - Troca para o schema do tenant
     *    - Conta os usuários naquele schema
     *    - Acumula no total
     *    - Restaura o contexto (sempre no finally)
     * 
     * PERFORMANCE: Com muitos tenants, considere:
     * - Cache das estatísticas
     * - Cálculo assíncrono em background
     * - Atualização periódica ao invés de tempo real
     * 
     * @return DTO com estatísticas dos tenants e total de usuários
     */
    public TenantStatsDTO getStats() {
        List<Tenant> allTenants = tenantRepository.findAll();
        
        long totalTenants = allTenants.size();
        long activeTenants = allTenants.stream().filter(Tenant::getActive).count();
        long inactiveTenants = totalTenants - activeTenants;
        
        // Contar total de usuários de todos os schemas/tenants
        String currentTenant = TenantContext.getCurrentTenant();
        long totalUsers = 0;
        
        try {
            for (Tenant tenant : allTenants) {
                try {
                    // Trocar para o schema do tenant
                    TenantContext.setCurrentTenant(tenant.getDomain());
                    // Contar usuários deste schema
                    totalUsers += usuarioRepository.count();
                } catch (Exception e) {
                    // Se houver erro ao acessar algum schema, apenas loga e continua
                    // (pode ser que o schema ainda não exista)
                    continue;
                }
            }
        } finally {
            // Restaurar o tenant original
            TenantContext.setCurrentTenant(currentTenant);
        }
        
        return new TenantStatsDTO(totalTenants, activeTenants, inactiveTenants, totalUsers);
    }

    /**
     * Busca todos os usuários de um tenant específico.
     * 
     * IMPORTANTE: Este método utiliza troca de schema para buscar usuários.
     * Cada tenant tem seu próprio schema de banco de dados (schema-per-tenant).
     * 
     * Fluxo:
     * 1. Busca o tenant no schema público
     * 2. Salva o contexto atual do tenant
     * 3. Troca temporariamente para o schema do tenant (usando TenantContext)
     * 4. Busca todos os usuários naquele schema
     * 5. Restaura o contexto original (CRÍTICO: sempre no finally)
     * 
     * @param tenantId ID do tenant
     * @return Lista de DTOs com informações dos usuários
     * @throws ResourceNotFoundException se o tenant não for encontrado
     */
    public List<UsuarioTenantDTO> getUsuariosByTenant(UUID tenantId) {
        Tenant tenant = findById(tenantId);
        String schema = tenant.getDomain();
        
        List<UsuarioTenantDTO> usuarios = new java.util.ArrayList<>();
        
        try (Connection connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            
            String query = String.format(
                "SELECT id, email, nome, criado_em, ultimo_acesso FROM \"%s\".usuario ORDER BY criado_em DESC", 
                schema
            );
            
            try (var resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    Long usuarioId = resultSet.getLong("id");
                    String email = resultSet.getString("email");
                    String nome = resultSet.getString("nome");
                    java.time.LocalDateTime criadoEm = resultSet.getTimestamp("criado_em") != null 
                        ? resultSet.getTimestamp("criado_em").toLocalDateTime() 
                        : null;
                    java.time.LocalDateTime ultimoAcesso = resultSet.getTimestamp("ultimo_acesso") != null 
                        ? resultSet.getTimestamp("ultimo_acesso").toLocalDateTime() 
                        : null;
                    
                    usuarios.add(new UsuarioTenantDTO(
                        usuarioId,
                        email,
                        nome,
                        tenant.getId(),
                        criadoEm,
                        ultimoAcesso
                    ));
                }
            }
            
            return usuarios;
            
        } catch (java.sql.SQLException e) {
            throw new RuntimeException(
                String.format("Erro ao buscar usuários do tenant '%s' (schema: %s): %s", 
                    tenant.getName(), schema, e.getMessage()),
                e
            );
        }
    }
}
