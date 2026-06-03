package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.core.domain.entities.Plano;
import com.tallyto.gestorfinanceiro.core.domain.entities.Tenant;
import com.tallyto.gestorfinanceiro.core.domain.exceptions.PaymentRequiredException;
import com.tallyto.gestorfinanceiro.core.domain.exceptions.ResourceNotFoundException;
import com.tallyto.gestorfinanceiro.core.infra.repositories.PlanoRepository;
import com.tallyto.gestorfinanceiro.core.infra.repositories.TenantRepository;
import com.tallyto.gestorfinanceiro.core.infra.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlanLimitService {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private PlanoRepository planoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public void verificarLimiteUsuarios(String tenantDomain) {
        Tenant tenant = tenantRepository.findByDomain(tenantDomain)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant não encontrado: " + tenantDomain));

        Plano plano = planoRepository.findByTipo(tenant.getSubscriptionPlan())
                .orElse(null);

        int maxUsuarios = resolverMaxUsuarios(tenant, plano);

        // TenantContext já está definido pelo JwtAuthenticationFilter — conta no schema correto
        long totalUsuarios = usuarioRepository.count();

        if (totalUsuarios >= maxUsuarios) {
            String nomePlano = plano != null ? plano.getNome() : tenant.getSubscriptionPlan().name();
            throw new PaymentRequiredException(
                    "Limite de usuários do plano %s atingido (%d/%d). Faça upgrade para adicionar mais usuários."
                            .formatted(nomePlano, totalUsuarios, maxUsuarios)
            );
        }
    }

    private int resolverMaxUsuarios(Tenant tenant, Plano plano) {
        // Prioridade: campo do tenant (configuração manual) → plano cadastrado → fallback 1
        if (tenant.getMaxUsers() != null && tenant.getMaxUsers() > 0) {
            return tenant.getMaxUsers();
        }
        if (plano != null) {
            return plano.getMaxUsuarios();
        }
        return 1;
    }
}
