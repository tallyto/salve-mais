package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.core.domain.entities.CartaoCredito;
import com.tallyto.gestorfinanceiro.core.domain.exceptions.CartaoCreditoException;
import com.tallyto.gestorfinanceiro.core.infra.repositories.CartaoCreditoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartaoCreditoService {

    @Autowired
    private CartaoCreditoRepository cartaoCreditoRepository;

    public CartaoCredito findOrFail(Long cartaoId){
        return cartaoCreditoRepository.findById(cartaoId).orElseThrow(() -> new CartaoCreditoException(cartaoId));
    }

    public CartaoCredito salvarCartaoCredito(CartaoCredito  cartaoCredito) {
        return cartaoCreditoRepository.save(cartaoCredito);
    }

    public List<CartaoCredito> listarCartoesCredito() {
        return cartaoCreditoRepository.findAll();
    }
}
