package com.tallyto.gestorfinanceiro.core.domain.repositories;

import com.tallyto.gestorfinanceiro.core.domain.PlanoCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanoCompraRepository extends JpaRepository<PlanoCompra, Long> {

    List<PlanoCompra> findAllByOrderByPrioridadeAscDataPrevistaAsc();

    List<PlanoCompra> findByStatusOrderByPrioridadeAscDataPrevistaAsc(PlanoCompra.StatusPlano status);
}
