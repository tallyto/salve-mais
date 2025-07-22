package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.core.domain.entities.CartaoCredito;
import com.tallyto.gestorfinanceiro.core.domain.entities.Compra;
import com.tallyto.gestorfinanceiro.core.domain.entities.Fatura;
import com.tallyto.gestorfinanceiro.core.domain.entities.Conta;
import com.tallyto.gestorfinanceiro.core.infra.repositories.FaturaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private ContaService contaService;

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
        fatura.setDataPagamento(LocalDate.now());
        faturaRepository.save(fatura);
    }

    @Transactional
    public void marcarComoPaga(Long faturaId, Long contaId) {
        Fatura fatura = findOrFail(faturaId);
        
        if (fatura.isPago()) {
            throw new RuntimeException("Fatura já foi paga");
        }

        Conta conta = contaService.findOrFail(contaId);
        
        // Verificar se a conta tem saldo suficiente
        if (conta.getSaldo().compareTo(fatura.getValorTotal()) < 0) {
            throw new RuntimeException("Saldo insuficiente na conta para pagar a fatura");
        }

        // Debitar o valor da conta
        contaService.debitar(contaId, fatura.getValorTotal());
        
        // Marcar fatura como paga
        fatura.setPago(true);
        fatura.setDataPagamento(LocalDate.now());
        fatura.setContaPagamento(conta);
        
        faturaRepository.save(fatura);
    }

    public void excluirFatura(Long faturaId) {
        Fatura fatura = findOrFail(faturaId);
        
        if (fatura.isPago()) {
            throw new RuntimeException("Não é possível excluir uma fatura já paga");
        }
        
        faturaRepository.delete(fatura);
    }

    public List<Fatura> listarPorConta(Long contaId) {
        return faturaRepository.findByContaPagamentoId(contaId);
    }

    public List<Fatura> listarNaoPagas() {
        return faturaRepository.findByPagoFalse();
    }

    public BigDecimal calcularTotalFaturasPendentes(Long cartaoCreditoId) {
        return faturaRepository.findByCartaoCreditoIdAndPagoFalse(cartaoCreditoId)
                .stream()
                .map(Fatura::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
