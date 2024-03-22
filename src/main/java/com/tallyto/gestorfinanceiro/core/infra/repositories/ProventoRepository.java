package com.tallyto.gestorfinanceiro.core.infra.repositories;

import com.tallyto.gestorfinanceiro.core.domain.entities.Provento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProventoRepository extends JpaRepository<Provento, Long> {
}
