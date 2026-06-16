package br.com.salvemais.infrastructure.repositories;

import br.com.salvemais.domain.entities.NotificacaoEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificacaoEmailRepository extends JpaRepository<NotificacaoEmail, UUID> {
    Optional<NotificacaoEmail> findByDomain(String domain);
    List<NotificacaoEmail> findByAtivoTrue();
}
