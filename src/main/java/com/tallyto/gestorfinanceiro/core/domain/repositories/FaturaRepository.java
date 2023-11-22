package com.tallyto.gestorfinanceiro.core.domain.repositories;

import com.tallyto.gestorfinanceiro.core.domain.entities.Fatura;
import org.springframework.data.jpa.repository.JpaRepository;


public interface FaturaRepository extends JpaRepository<Fatura, Long> {

}
