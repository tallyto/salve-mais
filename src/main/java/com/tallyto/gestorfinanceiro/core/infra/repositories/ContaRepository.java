package com.tallyto.gestorfinanceiro.core.infra.repositories;

import com.tallyto.gestorfinanceiro.core.domain.entities.Conta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContaRepository extends JpaRepository<Conta, Long> {
}
