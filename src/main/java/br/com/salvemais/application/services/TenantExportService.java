package br.com.salvemais.application.services;

import br.com.salvemais.domain.entities.Tenant;
import br.com.salvemais.domain.entities.Usuario;
import br.com.salvemais.infrastructure.repositories.TenantRepository;
import br.com.salvemais.infrastructure.repositories.UsuarioRepository;
import br.com.salvemais.web.api.dto.TenantExportDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TenantExportService {

    private final TenantRepository tenantRepository;
    private final UsuarioRepository usuarioRepository;

    public TenantExportService(TenantRepository tenantRepository, UsuarioRepository usuarioRepository) {
        this.tenantRepository = tenantRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public TenantExportDTO exportarDadosTenant(UUID tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found with id " + tenantId));

        List<Usuario> usuarios = usuarioRepository.findByTenantId(tenant.getId());
        List<TenantExportDTO.UsuarioExportDTO> usuariosExportados = usuarios.stream()
                .map(u -> new TenantExportDTO.UsuarioExportDTO(
                        u.getId(),
                        u.getNome(),
                        u.getEmail(),
                        u.getAtivo(),
                        u.getCriadoEm(),
                        u.getUltimoAcesso()
                ))
                .collect(Collectors.toList());

        TenantExportDTO.TenantInfoDTO tenantInfo = new TenantExportDTO.TenantInfoDTO(
                tenant.getId(),
                tenant.getName(),
                tenant.getDomain(),
                tenant.getEmail(),
                tenant.getPhoneNumber(),
                tenant.getActive(),
                tenant.getSubscriptionPlan(),
                tenant.getCreatedAt()
        );

        return new TenantExportDTO(
                tenantInfo,
                usuariosExportados,
                usuarios.size(),
                usuarios.stream().filter(Usuario::getAtivo).count(),
                LocalDateTime.now()
        );
    }
}
