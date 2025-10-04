package com.tallyto.gestorfinanceiro.core.infra.repositories;

import com.tallyto.gestorfinanceiro.core.domain.entities.Parcela;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ParcelaRepository extends JpaRepository<Parcela, Long> {
    
    // Busca parcelas de uma compra parcelada específica
    List<Parcela> findByCompraParceladaId(Long compraParceladaId);
    
    // Busca parcelas por cartão e período de vencimento
    @Query("SELECT p FROM Parcela p WHERE p.compraParcelada.cartaoCredito.id = :cartaoId " +
           "AND p.dataVencimento BETWEEN :inicio AND :fim")
    List<Parcela> findByCartaoAndPeriodo(@Param("cartaoId") Long cartaoId, 
                                         @Param("inicio") LocalDate inicio, 
                                         @Param("fim") LocalDate fim);
    
    // Busca parcelas não pagas de um cartão
    @Query("SELECT p FROM Parcela p WHERE p.compraParcelada.cartaoCredito.id = :cartaoId AND p.paga = false")
    List<Parcela> findParcelasNaoPagasByCartao(@Param("cartaoId") Long cartaoId);
    
    // Busca parcelas não pagas com vencimento até determinada data
    @Query("SELECT p FROM Parcela p WHERE p.paga = false AND p.dataVencimento <= :data")
    List<Parcela> findParcelasVencidas(@Param("data") LocalDate data);
    
    // Busca parcelas por status de pagamento
    List<Parcela> findByPaga(boolean paga);
    
    // Busca parcelas com paginação
    Page<Parcela> findByCompraParceladaCartaoCreditoId(Long cartaoId, Pageable pageable);
}
