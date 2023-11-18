package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.core.domain.entities.Compra;
import com.tallyto.gestorfinanceiro.core.domain.repositories.CompraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompraService {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private CartaoCreditoService cartaoCreditoService;

    public Compra salvarCompra(Compra compra) {
        var cartaoCredito = cartaoCreditoService.findOrFail(compra.getCartaoCredito().getId());
        compra.setCartaoCredito(cartaoCredito);

        return compraRepository.save(compra);
    }

    public List<Compra> listarCompras() {
        return compraRepository.findAll();
    }

}
