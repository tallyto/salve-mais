package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.core.domain.entities.CartaoCredito;
import com.tallyto.gestorfinanceiro.core.domain.entities.Compra;
import com.tallyto.gestorfinanceiro.core.domain.entities.Fatura;
import com.tallyto.gestorfinanceiro.core.infra.repositories.FaturaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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

    public Fatura criarFaturaManual(Long cartaoCreditoId, BigDecimal valorTotal, LocalDate dataVencimento) {
        CartaoCredito cartaoCredito = cartaoCreditoService.findOrFail(cartaoCreditoId);
        
        Fatura fatura = new Fatura();
        fatura.setCartaoCredito(cartaoCredito);
        fatura.setCompras(new ArrayList<>()); // Lista vazia para fatura manual
        fatura.setValorTotal(valorTotal);
        fatura.setPago(false);
        fatura.setDataVencimento(dataVencimento);
        
        return faturaRepository.save(fatura);
    }

    public Fatura findOrFail(Long id) {
        return faturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fatura não encontrada com ID: " + id));
    }

    public void marcarComoPaga(Long faturaId) {
        Fatura fatura = findOrFail(faturaId);
        fatura.setPago(true);
        faturaRepository.save(fatura);
    }

    public void excluirFatura(Long faturaId) {
        Fatura fatura = findOrFail(faturaId);
        faturaRepository.delete(fatura);
    }
}
