package com.tallyto.gestorfinanceiro.core.domain.repositories;

import com.tallyto.gestorfinanceiro.core.domain.entities.ContaFixa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContaFixaRepository extends JpaRepository<ContaFixa, Long> {

    List<ContaFixa> findByCategoria_Id(Long categoriaId);

    List<ContaFixa> findByVencimentoBeforeAndPagoIsFalse(LocalDate vencimento);
}