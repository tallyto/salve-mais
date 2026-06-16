package br.com.salvemais.application.services;

import br.com.salvemais.domain.entities.Tenant;
import br.com.salvemais.domain.exceptions.ResourceNotFoundException;
import br.com.salvemais.infrastructure.repositories.TenantRepository;
import br.com.salvemais.web.api.dto.UsuarioTenantDTO;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TenantSchemaUserService {

    private final TenantRepository tenantRepository;
    private final DataSource dataSource;

    public TenantSchemaUserService(TenantRepository tenantRepository, DataSource dataSource) {
        this.tenantRepository = tenantRepository;
        this.dataSource = dataSource;
    }

    public List<UsuarioTenantDTO> getUsuariosByTenant(UUID tenantId) {
        Tenant tenant = findTenant(tenantId);
        String schema = tenant.getDomain();

        List<UsuarioTenantDTO> usuarios = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             var statement = connection.createStatement()) {

            String query = "SELECT id, email, nome, criado_em, ultimo_acesso FROM \"%s\".usuario ORDER BY criado_em DESC".formatted(schema);

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

    private Tenant findTenant(UUID tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id " + tenantId));
    }
}
