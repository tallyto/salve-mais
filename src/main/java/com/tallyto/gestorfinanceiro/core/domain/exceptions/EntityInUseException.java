package com.tallyto.gestorfinanceiro.core.domain.exceptions;

/**
 * Exceção lançada quando uma tentativa de excluir uma entidade falha porque ela está sendo referenciada por outras entidades.
 */
public class EntityInUseException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EntityInUseException(String message) {
        super(message);
    }

    public EntityInUseException(String entidadeNome, Long id) {
        this(String.format("Não é possível excluir %s com código %d pois está em uso", entidadeNome, id));
    }

    public EntityInUseException(String entidadeNome, Long id, String entidadeReferenciadora) {
        this(String.format("Não é possível excluir %s com código %d pois está sendo utilizado em %s", 
                entidadeNome, id, entidadeReferenciadora));
    }
}
