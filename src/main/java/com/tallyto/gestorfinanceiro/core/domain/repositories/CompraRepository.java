package com.tallyto.gestorfinanceiro.core.domain.repositories;

import com.tallyto.gestorfinanceiro.core.domain.entities.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {
}
