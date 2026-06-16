package br.com.salvemais.application.services;

import br.com.salvemais.domain.entities.Tenant;
import br.com.salvemais.infrastructure.context.TenantContext;
import br.com.salvemais.infrastructure.repositories.TenantRepository;
import br.com.salvemais.infrastructure.repositories.UsuarioRepository;
import br.com.salvemais.web.api.dto.TenantStatsDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantStatsServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private TenantStatsService tenantStatsService;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void deveCalcularEstatisticasERestaurarTenantOriginal() {
        Tenant tenant1 = new Tenant();
        tenant1.setDomain("tenant-1");
        tenant1.setActive(true);

        Tenant tenant2 = new Tenant();
        tenant2.setDomain("tenant-2");
        tenant2.setActive(false);

        when(tenantRepository.findAll()).thenReturn(List.of(tenant1, tenant2));
        when(usuarioRepository.count()).thenReturn(2L, 3L);

        TenantContext.setCurrentTenant("original");

        TenantStatsDTO stats = tenantStatsService.getStats();

        assertEquals(2L, stats.getTotalTenants());
        assertEquals(1L, stats.getActiveTenants());
        assertEquals(1L, stats.getInactiveTenants());
        assertEquals(5L, stats.getTotalUsers());
        assertEquals("original", TenantContext.getCurrentTenant());
    }
}
