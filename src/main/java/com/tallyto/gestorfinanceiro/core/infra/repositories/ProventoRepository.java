package com.tallyto.gestorfinanceiro.core.infra.repositories;

import com.tallyto.gestorfinanceiro.core.domain.entities.Provento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProventoRepository extends JpaRepository<Provento, Long> {
    
    /**
     * Busca proventos com data entre os parâmetros fornecidos
     * @param inicio Data inicial
     * @param fim Data final
     * @return Lista de proventos no período
     */
    List<Provento> findByDataBetween(LocalDate inicio, LocalDate fim);
}
