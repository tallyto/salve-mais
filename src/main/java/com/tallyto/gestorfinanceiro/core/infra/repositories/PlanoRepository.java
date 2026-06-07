package com.tallyto.gestorfinanceiro.core.infra.repositories;

import com.tallyto.gestorfinanceiro.core.domain.entities.Plano;
import com.tallyto.gestorfinanceiro.core.domain.entities.Tenant.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlanoRepository extends JpaRepository<Plano, UUID> {
    Optional<Plano> findByTipo(SubscriptionPlan tipo);
    Optional<Plano> findByStripePriceId(String stripePriceId);
    List<Plano> findByAtivoTrueOrderByPrecoMensalAsc();
}
