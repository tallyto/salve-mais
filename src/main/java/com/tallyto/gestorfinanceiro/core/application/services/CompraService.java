package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.core.domain.entities.Compra;
import com.tallyto.gestorfinanceiro.core.domain.repositories.CompraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    public Page<Compra> listarCompras(Pageable pageable) {
        return compraRepository.findAll(pageable);
    }


    public List<Compra> comprasPorCartaoAteData(Long cartaoId, LocalDate dataVencimento) {
        // Calcula a data de fechamento da fatura (último dia do mês anterior ao vencimento)
        LocalDate dataFechamentoFatura = dataVencimento.minusDays(10);

        // Calcula o primeiro dia do mês do fechamento da fatura
        LocalDate primeiroDiaMesFechamento = dataFechamentoFatura.withDayOfMonth(1);

        // Use o repositório para buscar as compras no intervalo desejado
        return compraRepository.findByCartaoCreditoIdAndDataBetween(cartaoId, primeiroDiaMesFechamento, dataFechamentoFatura);
    }

}
