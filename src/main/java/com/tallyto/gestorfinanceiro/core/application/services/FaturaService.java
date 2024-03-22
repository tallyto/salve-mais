package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.core.domain.entities.CartaoCredito;
import com.tallyto.gestorfinanceiro.core.domain.entities.Compra;
import com.tallyto.gestorfinanceiro.core.domain.entities.Fatura;
import com.tallyto.gestorfinanceiro.core.infra.repositories.FaturaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class FaturaService {


    @Autowired
    private FaturaRepository faturaRepository;

    @Autowired
    private CompraService compraService;

    @Autowired
    private CartaoCreditoService cartaoCreditoService;

    public void gerarFatura(Long cartaoCreditoId) {
        CartaoCredito cartaoCredito = cartaoCreditoService.findOrFail(cartaoCreditoId);
        Fatura fatura = new Fatura();

        List<Compra> compras = compraService.comprasPorCartaoAteData(cartaoCreditoId, cartaoCredito.getVencimento());


        fatura.setCartaoCredito(cartaoCredito);
        fatura.setCompras(compras);
        fatura.setValorTotal(compras.stream()
                .map(Compra::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        fatura.setPago(false);
        fatura.setDataVencimento(cartaoCredito.getVencimento());

        faturaRepository.save(fatura);


    }


    public List<Fatura> listar() {
        return faturaRepository.findAll();
    }
}
