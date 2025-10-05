package com.tallyto.gestorfinanceiro.core.infra.repositories;

import com.tallyto.gestorfinanceiro.core.domain.entities.Transacao;
import com.tallyto.gestorfinanceiro.core.domain.enums.TipoTransacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long>, JpaSpecificationExecutor<Transacao> {
    
    /**
     * Busca todas as transações de uma conta específica.
     */
    Page<Transacao> findByConta_Id(Long contaId, Pageable pageable);
    
    /**
     * Busca todas as transações de um tipo específico.
     */
    Page<Transacao> findByTipo(TipoTransacao tipo, Pageable pageable);
    
    /**
     * Busca todas as transações em um período específico.
     */
    Page<Transacao> findByDataBetween(LocalDateTime inicio, LocalDateTime fim, Pageable pageable);
    
    /**
     * Busca todas as transações de uma conta específica em um período específico.
     */
    Page<Transacao> findByConta_IdAndDataBetween(Long contaId, LocalDateTime inicio, LocalDateTime fim, Pageable pageable);
    
    /**
     * Busca todas as transações de uma conta específica de um tipo específico.
     */
    Page<Transacao> findByConta_IdAndTipo(Long contaId, TipoTransacao tipo, Pageable pageable);
    
    /**
     * Busca todas as transações de uma conta específica de um tipo específico em um período específico.
     */
    Page<Transacao> findByConta_IdAndTipoAndDataBetween(Long contaId, TipoTransacao tipo, LocalDateTime inicio, LocalDateTime fim, Pageable pageable);
    
    /**
     * Busca todas as transações relacionadas a uma categoria específica.
     */
    Page<Transacao> findByCategoria_Id(Long categoriaId, Pageable pageable);
    
    /**
     * Busca todas as transações relacionadas a uma conta fixa específica.
     */
    List<Transacao> findByContaFixa_Id(Long contaFixaId);
    
    /**
     * Busca todas as transações relacionadas a um provento específico.
     */
    List<Transacao> findByProvento_Id(Long proventoId);
    
    /**
     * Busca todas as transações relacionadas a uma fatura específica.
     */
    List<Transacao> findByFatura_Id(Long faturaId);
    
    /**
     * Busca todas as transações de transferência entre duas contas específicas.
     */
    @Query("SELECT t FROM transacao t WHERE " +
           "(t.conta.id = :contaOrigemId AND t.contaDestino.id = :contaDestinoId) OR " +
           "(t.conta.id = :contaDestinoId AND t.contaDestino.id = :contaOrigemId)")
    Page<Transacao> findTransferenciasByContas(
            @Param("contaOrigemId") Long contaOrigemId,
            @Param("contaDestinoId") Long contaDestinoId,
            Pageable pageable
    );
    
 
    
    /**
     * Busca as últimas transações de uma conta.
     */
    Page<Transacao> findByConta_IdOrderByDataDesc(Long contaId, Pageable pageable);
    
    /**
     * Busca as últimas transações (global).
     */
    Page<Transacao> findAllByOrderByDataDesc(Pageable pageable);
    
    
    /**
     * Busca uma transação de transferência de entrada com base nos dados da transferência de saída.
     */
    Optional<Transacao> findByConta_IdAndContaDestino_IdAndTipoAndValor(
            Long contaId, 
            Long contaDestinoId, 
            TipoTransacao tipo, 
            BigDecimal valor
    );
}
