package com.tallyto.gestorfinanceiro.core.domain.repositories;

import com.tallyto.gestorfinanceiro.core.domain.entities.Provento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProventoRepository extends JpaRepository<Provento, Long> {
}
