package br.com.salvemais.application.services;

import br.com.salvemais.domain.entities.Tenant;
import br.com.salvemais.domain.entities.Usuario;
import br.com.salvemais.infrastructure.context.TenantContext;
import br.com.salvemais.infrastructure.repositories.TenantRepository;
import br.com.salvemais.infrastructure.repositories.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantUserServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private TenantUserService tenantUserService;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void deveAlternarStatusDoUsuarioERestaurarTenantOriginal() {
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = new Tenant();
        tenant.setId(tenantId);
        tenant.setDomain("tenant-1");

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setAtivo(true);

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        TenantContext.setCurrentTenant("original");

        tenantUserService.toggleUsuarioStatus(tenantId, 1L);

        assertEquals(false, usuario.getAtivo());
        assertEquals("original", TenantContext.getCurrentTenant());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deveResetarTodasAsSenhasDosUsuariosAtivos() {
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = new Tenant();
        tenant.setId(tenantId);
        tenant.setDomain("tenant-1");

        Usuario ativo = new Usuario();
        ativo.setId(1L);
        ativo.setAtivo(true);
        ativo.setEmail("ativo@salve.com");
        ativo.setNome("Ativo");

        Usuario inativo = new Usuario();
        inativo.setId(2L);
        inativo.setAtivo(false);
        inativo.setEmail("inativo@salve.com");
        inativo.setNome("Inativo");

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(usuarioRepository.findAll()).thenReturn(List.of(ativo, inativo));

        TenantContext.setCurrentTenant("original");

        tenantUserService.resetarTodasSenhas(tenantId);

        assertEquals("original", TenantContext.getCurrentTenant());
        verify(usuarioRepository).save(ativo);
        verify(usuarioRepository, never()).save(inativo);
        verify(emailService).enviarEmailRecuperacaoSenha(eq("ativo@salve.com"), eq("Ativo"), contains("resetar-senha?token="));
        verify(emailService, never()).enviarEmailRecuperacaoSenha(eq("inativo@salve.com"), eq("Inativo"), anyString());
    }

    @Test
    void deveDesativarTodosUsuariosDoTenant() {
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = new Tenant();
        tenant.setId(tenantId);
        tenant.setDomain("tenant-1");

        Usuario usuario1 = new Usuario();
        usuario1.setId(1L);
        usuario1.setAtivo(true);

        Usuario usuario2 = new Usuario();
        usuario2.setId(2L);
        usuario2.setAtivo(true);

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario1, usuario2));

        tenantUserService.desativarTodosUsuarios(tenantId);

        assertEquals(false, usuario1.getAtivo());
        assertEquals(false, usuario2.getAtivo());
        verify(usuarioRepository, times(2)).save(any(Usuario.class));
    }
}
