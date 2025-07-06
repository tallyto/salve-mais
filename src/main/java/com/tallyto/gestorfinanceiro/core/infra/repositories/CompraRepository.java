package com.tallyto.gestorfinanceiro.core.infra.repositories;

import com.tallyto.gestorfinanceiro.core.domain.entities.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface CompraRepository extends JpaRepository<Compra, Long>  {

    List<Compra> findByCartaoCreditoIdAndDataBetween(Long cartaoId, LocalDate inicio, LocalDate fim);
    
    /**
     * Busca compras com data entre os parâmetros fornecidos
     * @param inicio Data inicial
     * @param fim Data final
     * @return Lista de compras no período
     */
    List<Compra> findByDataBetween(LocalDate inicio, LocalDate fim);
}
