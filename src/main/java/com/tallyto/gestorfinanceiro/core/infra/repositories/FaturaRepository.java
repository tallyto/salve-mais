package com.tallyto.gestorfinanceiro.core.infra.repositories;

import com.tallyto.gestorfinanceiro.core.domain.entities.Fatura;
import org.springframework.data.jpa.repository.JpaRepository;


public interface FaturaRepository extends JpaRepository<Fatura, Long> {

}
