package com.tallyto.gestorfinanceiro.core.infra.repositories.specifications;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.tallyto.gestorfinanceiro.api.dto.TransacaoFiltroDTO;
import com.tallyto.gestorfinanceiro.core.domain.entities.Transacao;

import jakarta.persistence.criteria.Predicate;

public class TransacaoSpecification {

    public static Specification<Transacao> comFiltro(TransacaoFiltroDTO filtro) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro por conta
            if (filtro.contaId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("conta").get("id"), filtro.contaId()));
            }

            // Filtro por tipo
            if (filtro.tipo() != null) {
                predicates.add(criteriaBuilder.equal(root.get("tipo"), filtro.tipo()));
            }

            // Filtro por categoria
            if (filtro.categoriaId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("categoria").get("id"), filtro.categoriaId()));
            }

            // Filtro por data início
            if (filtro.dataInicio() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("data"), filtro.dataInicio()));
            }

            // Filtro por data fim
            if (filtro.dataFim() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("data"), filtro.dataFim()));
            }

            // Filtro por fatura
            if (filtro.faturaId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("fatura").get("id"), filtro.faturaId()));
            }

            // Filtro por conta fixa
            if (filtro.contaFixaId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("contaFixa").get("id"), filtro.contaFixaId()));
            }

            // Filtro por provento
            if (filtro.proventoId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("provento").get("id"), filtro.proventoId()));
            }

            // Ordenar por data decrescente
            query.orderBy(criteriaBuilder.desc(root.get("data")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
