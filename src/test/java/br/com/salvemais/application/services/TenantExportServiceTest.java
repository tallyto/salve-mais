package br.com.salvemais.application.services;

import br.com.salvemais.domain.entities.Tenant;
import br.com.salvemais.domain.entities.Usuario;
import br.com.salvemais.infrastructure.repositories.TenantRepository;
import br.com.salvemais.infrastructure.repositories.UsuarioRepository;
import br.com.salvemais.web.api.dto.TenantExportDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantExportServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private TenantExportService tenantExportService;

    @Test
    void deveExportarDadosDoTenantComUsuariosSemSenha() {
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = new Tenant();
        tenant.setId(tenantId);
        tenant.setName("Salve Mais");
        tenant.setDomain("salvemais.local");
        tenant.setEmail("contato@salvemais.com.br");
        tenant.setPhoneNumber("11999999999");
        tenant.setActive(true);
        tenant.setCreatedAt(LocalDateTime.of(2025, 9, 1, 10, 0));

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Administrador");
        usuario.setEmail("admin@salvemais.com.br");
        usuario.setAtivo(true);
        usuario.setCriadoEm(LocalDateTime.of(2025, 9, 2, 10, 0));
        usuario.setUltimoAcesso(LocalDateTime.of(2025, 9, 3, 10, 0));

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(usuarioRepository.findByTenantId(tenantId)).thenReturn(List.of(usuario));

        TenantExportDTO resultado = tenantExportService.exportarDadosTenant(tenantId);

        assertTrue(resultado.tenant() != null);
        assertEquals(1, resultado.totalUsuarios());
        assertEquals(1L, resultado.usuariosAtivos());

        assertEquals("Salve Mais", resultado.tenant().name());

        assertEquals("Administrador", resultado.usuarios().getFirst().nome());
    }
}
