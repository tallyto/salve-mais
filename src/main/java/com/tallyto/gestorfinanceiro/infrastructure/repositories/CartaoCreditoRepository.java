package com.tallyto.gestorfinanceiro.infrastructure.repositories;

import com.tallyto.gestorfinanceiro.domain.entities.CartaoCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartaoCreditoRepository extends JpaRepository<CartaoCredito, Long>
{
}
