package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.api.dto.NotificacaoEmailRequestDTO;
import com.tallyto.gestorfinanceiro.api.dto.NotificacaoEmailResponseDTO;
import com.tallyto.gestorfinanceiro.core.domain.entities.NotificacaoEmail;
import com.tallyto.gestorfinanceiro.core.infra.repositories.NotificacaoEmailRepository;
import com.tallyto.gestorfinanceiro.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificacaoEmailService {

    @Autowired
    private NotificacaoEmailRepository notificacaoEmailRepository;

    @Lazy
    @Autowired
    private NotificacaoEmailSchedulerService notificacaoEmailSchedulerService;

    @Transactional
    public NotificacaoEmailResponseDTO habilitarNotificacao(NotificacaoEmailRequestDTO request) {
        String currentTenantDomain = getCurrentTenantDomain();
        
        // Validar que o usuário está tentando criar/atualizar notificação para seu próprio tenant
        if (!currentTenantDomain.equals(request.getDomain())) {
            throw new IllegalArgumentException("Você não pode criar notificações para outros tenants");
        }
        
        Optional<NotificacaoEmail> notificacaoExistente = notificacaoEmailRepository.findByDomain(request.getDomain());
        
        NotificacaoEmail notificacao;
        if (notificacaoExistente.isPresent()) {
            notificacao = notificacaoExistente.get();
            notificacao.setHorario(request.getHorario());
            notificacao.setAtivo(request.getAtivo());
            notificacao.setUpdatedBy(getCurrentUser());
        } else {
            notificacao = new NotificacaoEmail();
            notificacao.setDomain(request.getDomain());
            notificacao.setHorario(request.getHorario());
            notificacao.setAtivo(request.getAtivo());
            notificacao.setCreatedBy(getCurrentUser());
            notificacao.setUpdatedBy(getCurrentUser());
        }
        
        notificacao = notificacaoEmailRepository.save(notificacao);
        return toResponseDTO(notificacao);
    }

    public NotificacaoEmailResponseDTO obterNotificacaoPorDomain(String domain) {
        String currentTenantDomain = getCurrentTenantDomain();
        
        // Validar que o usuário está tentando acessar notificação do seu próprio tenant
        if (!currentTenantDomain.equals(domain)) {
            throw new IllegalArgumentException("Você não pode acessar notificações de outros tenants");
        }
        
        Optional<NotificacaoEmail> notificacao = notificacaoEmailRepository.findByDomain(domain);
        return notificacao.map(this::toResponseDTO).orElse(null);
    }
    
    public NotificacaoEmailResponseDTO obterNotificacaoDoTenantAtual() {
        String currentTenantDomain = getCurrentTenantDomain();
        Optional<NotificacaoEmail> notificacao = notificacaoEmailRepository.findByDomain(currentTenantDomain);
        return notificacao.map(this::toResponseDTO).orElse(null);
    }

    public List<NotificacaoEmailResponseDTO> obterTodasNotificacoesAtivas() {
        return notificacaoEmailRepository.findByAtivoTrue()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<NotificacaoEmailResponseDTO> obterTodasNotificacoes() {
        return notificacaoEmailRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void desabilitarNotificacao(String domain) {
        String currentTenantDomain = getCurrentTenantDomain();
        
        // Validar que o usuário está tentando desabilitar notificação do seu próprio tenant
        if (!currentTenantDomain.equals(domain)) {
            throw new IllegalArgumentException("Você não pode desabilitar notificações de outros tenants");
        }
        
        Optional<NotificacaoEmail> notificacao = notificacaoEmailRepository.findByDomain(domain);
        if (notificacao.isPresent()) {
            NotificacaoEmail n = notificacao.get();
            n.setAtivo(false);
            n.setUpdatedBy(getCurrentUser());
            notificacaoEmailRepository.save(n);
        }
    }
    
    @Transactional
    public void desabilitarNotificacaoDoTenantAtual() {
        String currentTenantDomain = getCurrentTenantDomain();
        Optional<NotificacaoEmail> notificacao = notificacaoEmailRepository.findByDomain(currentTenantDomain);
        if (notificacao.isPresent()) {
            NotificacaoEmail n = notificacao.get();
            n.setAtivo(false);
            n.setUpdatedBy(getCurrentUser());
            notificacaoEmailRepository.save(n);
        }
    }

    public void enviarNotificacaoTeste() {
        String domain = getCurrentTenantDomain();
        
        NotificacaoEmail notificacao = notificacaoEmailRepository.findByDomain(domain)
                .orElseThrow(() -> new IllegalArgumentException("Configuração de notificação não encontrada para o tenant"));

        NotificacaoEmailResponseDTO notificacaoDTO = toResponseDTO(notificacao);
        notificacaoEmailSchedulerService.enviarNotificacaoTeste(notificacaoDTO);
    }

    @Transactional
    public void deletarNotificacao(UUID id) {
        notificacaoEmailRepository.deleteById(id);
    }

    private NotificacaoEmailResponseDTO toResponseDTO(NotificacaoEmail notificacao) {
        NotificacaoEmailResponseDTO dto = new NotificacaoEmailResponseDTO();
        dto.setId(notificacao.getId());
        dto.setDomain(notificacao.getDomain());
        dto.setHorario(notificacao.getHorario());
        dto.setAtivo(notificacao.getAtivo());
        dto.setCreatedAt(notificacao.getCreatedAt());
        dto.setUpdatedAt(notificacao.getUpdatedAt());
        return dto;
    }

    private String getCurrentUser() {
        String domain = TenantContext.getCurrentTenant();
        return domain != null ? domain : "system";
    }
    
    private String getCurrentTenantDomain() {
        String domain = TenantContext.getCurrentTenant();
        if (domain == null || "public".equals(domain)) {
            throw new IllegalStateException("Tenant não identificado");
        }
        return domain;
    }
}
