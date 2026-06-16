package com.tallyto.gestorfinanceiro.infrastructure.repositories;

import com.tallyto.gestorfinanceiro.domain.entities.Anexo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnexoRepository extends JpaRepository<Anexo, Long> {
    List<Anexo> findByContaFixaId(Long contaFixaId);
}
