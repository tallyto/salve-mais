package com.tallyto.gestorfinanceiro.core.infra.repositories;

import com.tallyto.gestorfinanceiro.core.domain.entities.Compra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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
    
    /**
     * Calcula o valor total utilizado no cartão em um período específico
     * @param cartaoId ID do cartão
     * @param inicio Data inicial
     * @param fim Data final
     * @return Soma dos valores das compras no período
     */
    @Query("SELECT COALESCE(SUM(c.valor), 0) FROM Compra c WHERE c.cartaoCredito.id = :cartaoId AND c.data BETWEEN :inicio AND :fim")
    BigDecimal calcularValorUtilizadoPeriodo(@Param("cartaoId") Long cartaoId, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
    
    @Query("SELECT c FROM Compra c WHERE MONTH(c.data) = :mes AND YEAR(c.data) = :ano")
    Page<Compra> findByDataMesEAno(Pageable pageable, @Param("mes") Integer mes, @Param("ano") Integer ano);
}
