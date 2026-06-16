package br.com.salvemais.infrastructure.repositories;

import br.com.salvemais.domain.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByEmail(String email);

    Optional<Usuario> findByEmail(String email);

    List<Usuario> findByTenantId(UUID tenantId);
}
