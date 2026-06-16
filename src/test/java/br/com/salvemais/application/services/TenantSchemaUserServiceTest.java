package br.com.salvemais.application.services;

import br.com.salvemais.domain.entities.Tenant;
import br.com.salvemais.infrastructure.repositories.TenantRepository;
import br.com.salvemais.web.api.dto.UsuarioTenantDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantSchemaUserServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private Statement statement;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @Mock
    private ResultSet usuariosResultSet;

    @InjectMocks
    private TenantSchemaUserService tenantSchemaUserService;

    @Test
    void deveListarUsuariosDoTenantPorSchema() throws Exception {
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = new Tenant();
        tenant.setId(tenantId);
        tenant.setName("Salve Mais");
        tenant.setDomain("tenant_schema");

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(contains("FROM \"tenant_schema\".usuario"))).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("id")).thenReturn(10L);
        when(resultSet.getString("email")).thenReturn("usuario@salve.com");
        when(resultSet.getString("nome")).thenReturn("Usuário");
        when(resultSet.getTimestamp("criado_em")).thenReturn(Timestamp.valueOf(LocalDateTime.of(2025, 6, 1, 10, 0)));
        when(resultSet.getTimestamp("ultimo_acesso")).thenReturn(Timestamp.valueOf(LocalDateTime.of(2025, 6, 2, 11, 0)));

        List<UsuarioTenantDTO> usuarios = tenantSchemaUserService.getUsuariosByTenant(tenantId);

        assertEquals(1, usuarios.size());
        UsuarioTenantDTO usuario = usuarios.get(0);
        assertNotNull(usuario);
        assertEquals(10L, usuario.getId());
        assertEquals("usuario@salve.com", usuario.getEmail());
        assertEquals("Usuário", usuario.getNome());
        assertEquals(tenantId, usuario.getTenantId());
        assertEquals(LocalDateTime.of(2025, 6, 1, 10, 0), usuario.getCriadoEm());
        assertEquals(LocalDateTime.of(2025, 6, 2, 11, 0), usuario.getUltimoAcesso());

        verify(statement).executeQuery(contains("FROM \"tenant_schema\".usuario"));
        verify(connection).close();
        verify(statement).close();
        verify(resultSet).close();
    }

    @Test
    void deveVerificarSeExisteUsuarioNoSchema() throws Exception {
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = new Tenant();
        tenant.setId(tenantId);
        tenant.setName("Salve Mais");
        tenant.setDomain("tenant_schema");

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(contains("information_schema.schemata"))).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getBoolean(1)).thenReturn(true);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(contains("FROM \"tenant_schema\".usuario"))).thenReturn(usuariosResultSet);
        when(usuariosResultSet.next()).thenReturn(true);
        when(usuariosResultSet.getBoolean(1)).thenReturn(true);

        boolean possuiUsuarios = tenantSchemaUserService.hasUsuarios(tenantId);

        assertEquals(true, possuiUsuarios);
        verify(preparedStatement).setString(1, "tenant_schema");
        verify(preparedStatement).close();
        verify(statement).executeQuery(contains("FROM \"tenant_schema\".usuario"));
    }
}
