package com.tallyto.gestorfinanceiro.core.infra.repositories;

import com.tallyto.gestorfinanceiro.core.domain.entities.UsuarioGlobal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioGlobalRepository extends JpaRepository<UsuarioGlobal, Long> {
    /**
     * Busca um usuário global pelo email
     * @param email email do usuário
     * @return Optional contendo o usuário se encontrado
     */
    Optional<UsuarioGlobal> findByEmail(String email);

    /**
     * Busca usuarios globais por tenant_id
     * @param tenantId ID do tenant
     * @return Lista de usuários do tenant
     */
    java.util.List<UsuarioGlobal> findByTenantId(UUID tenantId);

    /**
     * Verifica se existe usuário com determinado email
     * @param email email do usuário
     * @return true se existe, false caso contrário
     */
    boolean existsByEmail(String email);

    /**
     * Busca usuários ativos por tenant ID
     * @param tenantId ID do tenant
     * @param ativo status do usuário
     * @return Lista de usuários ativos
     */
    java.util.List<UsuarioGlobal> findByTenantIdAndAtivo(UUID tenantId, Boolean ativo);
}
