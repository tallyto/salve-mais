package com.tallyto.gestorfinanceiro.core.infra.repositories;

import com.tallyto.gestorfinanceiro.core.domain.entities.Conta;
import com.tallyto.gestorfinanceiro.core.domain.enums.TipoConta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContaRepository extends JpaRepository<Conta, Long> {
    
    /**
     * Busca contas por tipo
     * @param tipo O tipo de conta
     * @return Lista de contas do tipo especificado
     */
    List<Conta> findByTipo(TipoConta tipo);
    
    /**
     * Busca contas por uma lista de tipos
     * @param tipos Lista de tipos de conta
     * @return Lista de contas dos tipos especificados
     */
    List<Conta> findByTipoIn(List<TipoConta> tipos);
}
