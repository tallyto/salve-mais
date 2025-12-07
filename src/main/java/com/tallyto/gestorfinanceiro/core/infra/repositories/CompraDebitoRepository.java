package com.tallyto.gestorfinanceiro.core.infra.repositories;

import com.tallyto.gestorfinanceiro.core.domain.entities.CompraDebito;
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
public interface CompraDebitoRepository extends JpaRepository<CompraDebito, Long> {

    List<CompraDebito> findByCategoria_Id(Long categoriaId);
    
    List<CompraDebito> findByDataCompraBetween(LocalDate dataInicio, LocalDate dataFim);
    
    @Query("SELECT cd FROM CompraDebito cd WHERE MONTH(cd.dataCompra) = :mes AND YEAR(cd.dataCompra) = :ano")
    Page<CompraDebito> findByDataCompraMesEAno(Pageable pageable, @Param("mes") Integer mes, @Param("ano") Integer ano);
    
    @Query("SELECT SUM(cd.valor) FROM CompraDebito cd WHERE cd.dataCompra BETWEEN :dataInicio AND :dataFim")
    BigDecimal calcularTotalPorPeriodo(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);
}
