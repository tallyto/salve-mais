package br.com.salvemais.infrastructure.repositories;

import br.com.salvemais.domain.entities.Anexo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnexoRepository extends JpaRepository<Anexo, Long> {
    @EntityGraph(attributePaths = "contaFixa")
    List<Anexo> findByContaFixaId(Long contaFixaId);

    @EntityGraph(attributePaths = "contaFixa")
    List<Anexo> findAllBy();
}
