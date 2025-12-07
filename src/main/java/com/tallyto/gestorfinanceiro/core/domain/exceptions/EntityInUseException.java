package com.tallyto.gestorfinanceiro.core.domain.exceptions;

import java.io.Serial;

/**
 * Exceção lançada quando uma tentativa de excluir uma entidade falha porque ela está sendo referenciada por outras entidades.
 */
public class EntityInUseException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public EntityInUseException(String message) {
        super(message);
    }

    public EntityInUseException(String entidadeNome, Long id) {
        this("Não é possível excluir %s com código %d pois está em uso".formatted(entidadeNome, id));
    }

    public EntityInUseException(String entidadeNome, Long id, String entidadeReferenciadora) {
        this("Não é possível excluir %s com código %d pois está sendo utilizado em %s".formatted(
                entidadeNome, id, entidadeReferenciadora));
    }
}
