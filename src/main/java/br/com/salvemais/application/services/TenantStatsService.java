package br.com.salvemais.application.services;

import br.com.salvemais.domain.entities.Tenant;
import br.com.salvemais.infrastructure.context.TenantContext;
import br.com.salvemais.infrastructure.repositories.TenantRepository;
import br.com.salvemais.infrastructure.repositories.UsuarioRepository;
import br.com.salvemais.web.api.dto.TenantStatsDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TenantStatsService {

    private final TenantRepository tenantRepository;
    private final UsuarioRepository usuarioRepository;

    public TenantStatsService(TenantRepository tenantRepository, UsuarioRepository usuarioRepository) {
        this.tenantRepository = tenantRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public TenantStatsDTO getStats() {
        List<Tenant> allTenants = tenantRepository.findAll();

        long totalTenants = allTenants.size();
        long activeTenants = allTenants.stream().filter(Tenant::getActive).count();
        long inactiveTenants = totalTenants - activeTenants;

        long totalUsers = 0;
        for (Tenant tenant : allTenants) {
            try {
                totalUsers += TenantContext.withTenant(tenant.getDomain(), usuarioRepository::count);
            } catch (Exception ignored) {
                // Se o schema não existir, segue para o próximo tenant.
            }
        }

        return new TenantStatsDTO(totalTenants, activeTenants, inactiveTenants, totalUsers);
    }
}
