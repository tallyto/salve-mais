package com.tallyto.gestorfinanceiro.core.infra.repositories;

import com.tallyto.gestorfinanceiro.core.domain.entities.CompraParcelada;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CompraParceladaRepository extends JpaRepository<CompraParcelada, Long> {
    
    // Busca compras parceladas por cartão
    List<CompraParcelada> findByCartaoCreditoId(Long cartaoId);
    
    // Busca compras parceladas por cartão com paginação
    Page<CompraParcelada> findByCartaoCreditoId(Long cartaoId, Pageable pageable);
    
    // Busca compras parceladas por período
    @Query("SELECT cp FROM CompraParcelada cp WHERE cp.dataCompra BETWEEN :inicio AND :fim")
    List<CompraParcelada> findByDataCompraBetween(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
    
    // Busca compras parceladas por mês e ano
    @Query("SELECT cp FROM CompraParcelada cp WHERE MONTH(cp.dataCompra) = :mes AND YEAR(cp.dataCompra) = :ano")
    Page<CompraParcelada> findByDataCompraMesEAno(Pageable pageable, @Param("mes") Integer mes, @Param("ano") Integer ano);
}
