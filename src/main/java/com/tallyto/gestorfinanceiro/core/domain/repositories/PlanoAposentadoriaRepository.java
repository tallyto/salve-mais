package com.tallyto.gestorfinanceiro.core.domain.repositories;

import com.tallyto.gestorfinanceiro.core.domain.PlanoAposentadoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlanoAposentadoriaRepository extends JpaRepository<PlanoAposentadoria, Long> {

    Optional<PlanoAposentadoria> findFirstByAtivoTrue();

    boolean existsByAtivoTrue();
}
