package com.tallyto.gestorfinanceiro.core.domain.exceptions;

/**
 * Exceção lançada quando ocorre algum erro relacionado a transações.
 */
public class TransacaoException extends RuntimeException {
    
    public TransacaoException(String message) {
        super(message);
    }
    
    public TransacaoException(String message, Throwable cause) {
        super(message, cause);
    }
}