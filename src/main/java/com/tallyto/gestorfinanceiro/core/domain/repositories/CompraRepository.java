package com.tallyto.gestorfinanceiro.core.domain.repositories;

import com.tallyto.gestorfinanceiro.core.domain.entities.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface CompraRepository extends JpaRepository<Compra, Long>  {

    List<Compra> findByCartaoCreditoIdAndDataBetween(Long cartaoId, LocalDate inicio, LocalDate fim);
}
