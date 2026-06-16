package br.com.salvemais.application.services;

import br.com.salvemais.domain.entities.Tenant;
import br.com.salvemais.domain.entities.Usuario;
import br.com.salvemais.domain.exceptions.ResourceNotFoundException;
import br.com.salvemais.infrastructure.context.TenantContext;
import br.com.salvemais.infrastructure.repositories.TenantRepository;
import br.com.salvemais.infrastructure.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TenantUserService {

    private final TenantRepository tenantRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;

    public TenantUserService(TenantRepository tenantRepository,
                             UsuarioRepository usuarioRepository,
                             EmailService emailService) {
        this.tenantRepository = tenantRepository;
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
    }

    public void toggleUsuarioStatus(UUID tenantId, Long usuarioId) {
        Tenant tenant = findTenant(tenantId);
        TenantContext.runWithTenant(tenant.getDomain(), () -> {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
            usuario.setAtivo(!usuario.getAtivo());
            usuarioRepository.save(usuario);
        });
    }

    public void enviarResetSenhaUsuario(UUID tenantId, Long usuarioId) {
        Tenant tenant = findTenant(tenantId);
        TenantContext.runWithTenant(tenant.getDomain(), () -> {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

            String token = UUID.randomUUID().toString();
            usuario.setResetPasswordToken(token);
            usuario.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(24));
            usuarioRepository.save(usuario);

            String resetLink = "https://www.salvemais.com.br/#/resetar-senha?token=" + token;
            emailService.enviarEmailRecuperacaoSenha(
                    usuario.getEmail(),
                    usuario.getNome(),
                    resetLink
            );
        });
    }

    public void resetarTodasSenhas(UUID tenantId) {
        Tenant tenant = findTenant(tenantId);
        TenantContext.runWithTenant(tenant.getDomain(), () -> {
            List<Usuario> usuarios = usuarioRepository.findAll();

            for (Usuario usuario : usuarios) {
                if (usuario.getAtivo()) {
                    String token = UUID.randomUUID().toString();
                    usuario.setResetPasswordToken(token);
                    usuario.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(24));
                    usuarioRepository.save(usuario);

                    String resetLink = "https://www.salvemais.com.br/#/resetar-senha?token=" + token;
                    emailService.enviarEmailRecuperacaoSenha(
                            usuario.getEmail(),
                            usuario.getNome(),
                            resetLink
                    );
                }
            }
        });
    }

    public void desativarTodosUsuarios(UUID tenantId) {
        Tenant tenant = findTenant(tenantId);
        TenantContext.runWithTenant(tenant.getDomain(), () -> {
            List<Usuario> usuarios = usuarioRepository.findAll();
            for (Usuario usuario : usuarios) {
                usuario.setAtivo(false);
                usuarioRepository.save(usuario);
            }
        });
    }

    public void ativarTodosUsuarios(UUID tenantId) {
        Tenant tenant = findTenant(tenantId);
        TenantContext.runWithTenant(tenant.getDomain(), () -> {
            List<Usuario> usuarios = usuarioRepository.findAll();
            for (Usuario usuario : usuarios) {
                usuario.setAtivo(true);
                usuarioRepository.save(usuario);
            }
        });
    }

    private Tenant findTenant(UUID tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id " + tenantId));
    }
}
