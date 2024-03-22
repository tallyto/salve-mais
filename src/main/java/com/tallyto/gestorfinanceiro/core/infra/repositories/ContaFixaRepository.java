package com.tallyto.gestorfinanceiro.core.infra.repositories;

import com.tallyto.gestorfinanceiro.core.domain.entities.ContaFixa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContaFixaRepository extends JpaRepository<ContaFixa, Long> {

    List<ContaFixa> findByCategoria_Id(Long categoriaId);

    @Query("SELECT SUM(cf.valor) FROM ContaFixa cf WHERE cf.vencimento < :hoje AND cf.pago = false")
    BigDecimal calcularTotalContasFixasNaoPagas(@Param("hoje") LocalDate hoje);
    List<ContaFixa> findByVencimentoBeforeAndPagoIsFalse(LocalDate vencimento);
}