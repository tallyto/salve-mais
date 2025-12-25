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
import java.time.LocalDateTime;
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
        return tenantRepository.findByDomain(dominio).isEmpty();
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
            
            String query = "SELECT id, email, nome, criado_em, ultimo_acesso FROM \"%s\".usuario ORDER BY criado_em DESC".formatted(
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
    
    private boolean schemaExists(String schemaName) {
        try (Connection connection = dataSource.getConnection();
             var statement = connection.prepareStatement(
                 "SELECT EXISTS(SELECT 1 FROM information_schema.schemata WHERE schema_name = ?)")) {
            
            statement.setString(1, schemaName);
            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBoolean(1);
                }
            }
            return false;
        } catch (java.sql.SQLException e) {
            return false;
        }
    }
    
    public void enviarLembreteCriarUsuario(UUID tenantId) {
        Tenant tenant = findById(tenantId);
        
        // Verificar se o tenant já tem usuários
        List<UsuarioTenantDTO> usuarios = getUsuariosByTenant(tenantId);
        
        if (!usuarios.isEmpty()) {
            throw new BadRequestException("Este tenant já possui usuários cadastrados");
        }
        
        // Verificar se o tenant está ativo
        if (!tenant.getActive()) {
            throw new BadRequestException("O tenant precisa estar ativo para receber o lembrete");
        }
        
        // Gerar token para criação de usuário (válido por 24 horas)
        String token = UUID.randomUUID().toString();
        tenant.setCreateUserToken(token);
        tenant.setCreateUserTokenExpiry(LocalDateTime.now().plusHours(24));
        tenantRepository.save(tenant);
        
        // Construir link com token
        String createUserLink = "https://www.salvemais.com.br/#/criar-usuario?token=" + token;
        
        // Enviar email de lembrete
        emailService.enviarEmailLembreteCriarUsuario(
            tenant.getEmail(),
            tenant.getName(),
            tenant.getDomain(),
            createUserLink
        );
    }
    
    public Tenant verificarTokenCriarUsuario(String token) {
        Tenant tenant = tenantRepository.findByCreateUserToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token inválido ou expirado"));
        
        // Verificar se o token expirou
        if (tenant.getCreateUserTokenExpiry() == null || 
            tenant.getCreateUserTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Token expirado. Solicite um novo link através do suporte.");
        }
        
        // Verificar se já existe usuário (somente se o schema existir)
        if (schemaExists(tenant.getDomain())) {
            List<UsuarioTenantDTO> usuarios = getUsuariosByTenant(tenant.getId());
            if (!usuarios.isEmpty()) {
                throw new BadRequestException("Este tenant já possui usuários cadastrados");
            }
        }
        
        return tenant;
    }
    
    public void invalidarTokenCriarUsuario(UUID tenantId) {
        Tenant tenant = findById(tenantId);
        tenant.setCreateUserToken(null);
        tenant.setCreateUserTokenExpiry(null);
        tenantRepository.save(tenant);
    }

    public void toggleTenantStatus(UUID tenantId) {
        Tenant tenant = findById(tenantId);
        tenant.setActive(!tenant.getActive());
        tenantRepository.save(tenant);
    }

    public void toggleUsuarioStatus(UUID tenantId, Long usuarioId) {
        Tenant tenant = findById(tenantId);
        TenantContext.setCurrentTenant(tenant.getDomain());
        
        try {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
            
            usuario.setAtivo(!usuario.getAtivo());
            usuarioRepository.save(usuario);
        } finally {
            TenantContext.clear();
        }
    }

    public void enviarResetSenhaUsuario(UUID tenantId, Long usuarioId) {
        Tenant tenant = findById(tenantId);
        TenantContext.setCurrentTenant(tenant.getDomain());
        
        try {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
            
            // Gerar token de reset
            String token = UUID.randomUUID().toString();
            usuario.setResetPasswordToken(token);
            usuario.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(24));
            usuarioRepository.save(usuario);
            
            // Enviar email
            String resetLink = "https://www.salvemais.com.br/#/resetar-senha?token=" + token;
            emailService.enviarEmailRecuperacaoSenha(
                usuario.getEmail(),
                usuario.getNome(),
                resetLink
            );
        } finally {
            TenantContext.clear();
        }
    }

    public void enviarEmailBoasVindas(UUID tenantId) {
        Tenant tenant = findById(tenantId);
        
        // Reutilizar o template de confirmação de tenant
        String link = "https://www.salvemais.com.br";
        emailService.enviarEmailHtml(
            tenant.getEmail(),
            "Bem-vindo ao Salve Mais!",
            "confirmacao-tenant.html",
            tenant.getName(),
            link
        );
    }

    public void resetarTodasSenhas(UUID tenantId) {
        Tenant tenant = findById(tenantId);
        TenantContext.setCurrentTenant(tenant.getDomain());
        
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            
            for (Usuario usuario : usuarios) {
                if (usuario.getAtivo()) {
                    // Gerar token de reset
                    String token = UUID.randomUUID().toString();
                    usuario.setResetPasswordToken(token);
                    usuario.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(24));
                    usuarioRepository.save(usuario);
                    
                    // Enviar email
                    String resetLink = "https://www.salvemais.com.br/#/resetar-senha?token=" + token;
                    emailService.enviarEmailRecuperacaoSenha(
                        usuario.getEmail(),
                        usuario.getNome(),
                        resetLink
                    );
                }
            }
        } finally {
            TenantContext.clear();
        }
    }

    public void desativarTodosUsuarios(UUID tenantId) {
        Tenant tenant = findById(tenantId);
        TenantContext.setCurrentTenant(tenant.getDomain());
        
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            
            for (Usuario usuario : usuarios) {
                usuario.setAtivo(false);
                usuarioRepository.save(usuario);
            }
        } finally {
            TenantContext.clear();
        }
    }

    public void ativarTodosUsuarios(UUID tenantId) {
        Tenant tenant = findById(tenantId);
        TenantContext.setCurrentTenant(tenant.getDomain());
        
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            
            for (Usuario usuario : usuarios) {
                usuario.setAtivo(true);
                usuarioRepository.save(usuario);
            }
        } finally {
            TenantContext.clear();
        }
    }

    public java.util.Map<String, Object> exportarDadosTenant(UUID tenantId) {
        Tenant tenant = findById(tenantId);
        
        java.util.Map<String, Object> dados = new java.util.HashMap<>();
        
        // Informações do tenant (sem campos sensíveis)
        java.util.Map<String, Object> tenantInfo = new java.util.HashMap<>();
        tenantInfo.put("id", tenant.getId());
        tenantInfo.put("name", tenant.getName());
        tenantInfo.put("domain", tenant.getDomain());
        tenantInfo.put("email", tenant.getEmail());
        tenantInfo.put("phoneNumber", tenant.getPhoneNumber());
        tenantInfo.put("active", tenant.getActive());
        tenantInfo.put("subscriptionPlan", tenant.getSubscriptionPlan());
        tenantInfo.put("createdAt", tenant.getCreatedAt());
        dados.put("tenant", tenantInfo);
        
        // Buscar usuários do tenant usando tenantId ao invés de contexto
        List<Usuario> usuarios = usuarioRepository.findByTenantId(tenant.getId());
        
        // Usuários sem senhas
        List<java.util.Map<String, Object>> usuariosSemSenha = usuarios.stream()
            .map(u -> {
                java.util.Map<String, Object> usuarioMap = new java.util.HashMap<>();
                usuarioMap.put("id", u.getId());
                usuarioMap.put("nome", u.getNome());
                usuarioMap.put("email", u.getEmail());
                usuarioMap.put("ativo", u.getAtivo());
                usuarioMap.put("criadoEm", u.getCriadoEm());
                usuarioMap.put("ultimoAcesso", u.getUltimoAcesso());
                return usuarioMap;
            })
            .collect(java.util.stream.Collectors.toList());
        
        dados.put("usuarios", usuariosSemSenha);
        dados.put("totalUsuarios", usuarios.size());
        dados.put("usuariosAtivos", usuarios.stream().filter(Usuario::getAtivo).count());
        dados.put("dataExportacao", LocalDateTime.now());
        
        return dados;
    }
}

