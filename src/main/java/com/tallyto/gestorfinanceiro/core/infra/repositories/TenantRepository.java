package com.tallyto.gestorfinanceiro.core.infra.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tallyto.gestorfinanceiro.core.domain.entities.Tenant;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Tenant getTenantByDomain(String domain);
    Optional<Tenant> findByDomain(String domain);
    Optional<Tenant> findByEmail(String email);
    Optional<Tenant> findByConfirmationToken(String token);
}
