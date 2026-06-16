package com.tallyto.gestorfinanceiro.infrastructure.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tallyto.gestorfinanceiro.domain.entities.Tenant;
import com.tallyto.gestorfinanceiro.domain.enums.SubscriptionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Tenant getTenantByDomain(String domain);
    Optional<Tenant> findByDomain(String domain);
    Optional<Tenant> findByEmail(String email);
    Optional<Tenant> findByConfirmationToken(String token);
    Optional<Tenant> findByCreateUserToken(String token);
    Optional<Tenant> findByStripeCustomerId(String stripeCustomerId);
    Optional<Tenant> findByStripeSubscriptionId(String stripeSubscriptionId);
    List<Tenant> findBySubscriptionStatusAndTrialEndDateBefore(SubscriptionStatus status, LocalDateTime date);
    List<Tenant> findBySubscriptionStatus(SubscriptionStatus status);
}
