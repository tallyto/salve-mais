package br.com.salvemais.infrastructure.repositories;

import br.com.salvemais.domain.entities.CartaoCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartaoCreditoRepository extends JpaRepository<CartaoCredito, Long>
{
}
