package com.tallyto.gestorfinanceiro.core.domain.exceptions;

public class CartaoCreditoException extends RuntimeException {
    public CartaoCreditoException(String message) {
        super(message);
    }


    public CartaoCreditoException(Long  id) {
        super("Cartão de crédito com id %d não encontrado".formatted(id));
    }

}
