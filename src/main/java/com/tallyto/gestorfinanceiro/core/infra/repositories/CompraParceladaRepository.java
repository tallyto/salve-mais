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
    
    // Busca compras parceladas por cartão (não arquivadas)
    List<CompraParcelada> findByCartaoCreditoIdAndArquivadoFalse(Long cartaoId);
    
    // Busca compras parceladas por cartão com paginação (não arquivadas)
    Page<CompraParcelada> findByCartaoCreditoIdAndArquivadoFalse(Long cartaoId, Pageable pageable);
    
    // Busca compras parceladas por período
    @Query("SELECT cp FROM CompraParcelada cp WHERE cp.dataCompra BETWEEN :inicio AND :fim AND cp.arquivado = false")
    List<CompraParcelada> findByDataCompraBetween(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
    
    // Busca compras parceladas por mês e ano (não arquivadas)
    @Query("SELECT cp FROM CompraParcelada cp WHERE MONTH(cp.dataCompra) = :mes AND YEAR(cp.dataCompra) = :ano AND cp.arquivado = false")
    Page<CompraParcelada> findByDataCompraMesEAno(Pageable pageable, @Param("mes") Integer mes, @Param("ano") Integer ano);
    
    // Busca compras parceladas com parcelas pendentes (não pagas e não arquivadas)
    @Query("SELECT DISTINCT cp FROM CompraParcelada cp " +
           "LEFT JOIN cp.parcelas p " +
           "WHERE p.paga = false AND cp.arquivado = false")
    Page<CompraParcelada> findComprasComParcelasPendentes(Pageable pageable);
    
    // Busca compras parceladas ordenadas por data mais recente (não arquivadas)
    Page<CompraParcelada> findAllByArquivadoFalseOrderByDataCompraDesc(Pageable pageable);
    
    // Busca compras parceladas com parcelas pendentes por cartão (não arquivadas)
    @Query("SELECT DISTINCT cp FROM CompraParcelada cp " +
           "LEFT JOIN cp.parcelas p " +
           "WHERE cp.cartaoCredito.id = :cartaoId AND p.paga = false AND cp.arquivado = false")
    Page<CompraParcelada> findComprasComParcelasPendentesPorCartao(@Param("cartaoId") Long cartaoId, Pageable pageable);
    
    // Busca compras parceladas com parcelas pendentes por categoria (não arquivadas)
    @Query("SELECT DISTINCT cp FROM CompraParcelada cp " +
           "LEFT JOIN cp.parcelas p " +
           "WHERE cp.categoria.id = :categoriaId AND p.paga = false AND cp.arquivado = false")
    Page<CompraParcelada> findComprasComParcelasPendentesPorCategoria(@Param("categoriaId") Long categoriaId, Pageable pageable);
}
