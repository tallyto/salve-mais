package com.tallyto.gestorfinanceiro.core.infra.repositories;

import com.tallyto.gestorfinanceiro.core.domain.entities.ReservaEmergencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservaEmergenciaRepository extends JpaRepository<ReservaEmergencia, Long> {
    // Métodos personalizados podem ser adicionados aqui, se necessário
}
