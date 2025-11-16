package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.core.domain.entities.CartaoCredito;
import com.tallyto.gestorfinanceiro.core.domain.entities.Compra;
import com.tallyto.gestorfinanceiro.core.domain.entities.Fatura;
import com.tallyto.gestorfinanceiro.core.domain.entities.Conta;
import com.tallyto.gestorfinanceiro.core.domain.entities.Parcela;
import com.tallyto.gestorfinanceiro.core.infra.repositories.FaturaRepository;
import com.tallyto.gestorfinanceiro.core.infra.repositories.ParcelaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
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

    @Autowired
    private ParcelaRepository parcelaRepository;

    /**
     * Gera fatura para um cartão de crédito com data de vencimento específica ou usando a data do cartão.
     * @param cartaoCreditoId ID do cartão
     * @param dataVencimento Data de vencimento (opcional). Se null, usa a data do cartão
     */
    public void gerarFatura(Long cartaoCreditoId, LocalDate dataVencimento) {
        CartaoCredito cartaoCredito = cartaoCreditoService.findOrFail(cartaoCreditoId);
        
        // Se não foi fornecida data de vencimento, usa a do cartão
        LocalDate dataVencimentoFatura = dataVencimento != null ? dataVencimento : cartaoCredito.getVencimento();
        
        Fatura fatura = new Fatura();

        // Busca compras à vista do período
        List<Compra> compras = compraService.comprasPorCartaoAteData(cartaoCreditoId, dataVencimentoFatura);

        // Busca parcelas que vencem no período da fatura
        LocalDate dataFechamentoFatura = dataVencimentoFatura.minusDays(10);
        LocalDate primeiroDiaMesFechamento = dataFechamentoFatura.withDayOfMonth(1);
        
        List<Parcela> parcelas = parcelaRepository.findByCartaoAndPeriodo(
            cartaoCreditoId, 
            primeiroDiaMesFechamento, 
            dataFechamentoFatura
        );

        // Calcula valor total: compras à vista + parcelas
        BigDecimal valorCompras = compras.stream()
                .map(Compra::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal valorParcelas = parcelas.stream()
                .map(Parcela::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        fatura.setCartaoCredito(cartaoCredito);
        fatura.setCompras(compras);
        fatura.setValorTotal(valorCompras.add(valorParcelas));
        fatura.setPago(false);
        fatura.setDataVencimento(dataVencimentoFatura);

        faturaRepository.save(fatura);
    }
    
    /**
     * Gera fatura usando a data de vencimento do cartão (método de conveniência)
     */
    public void gerarFatura(Long cartaoCreditoId) {
        gerarFatura(cartaoCreditoId, null);
    }


    public Page<Fatura> listar(Pageable pageable) {
        return faturaRepository.findAll(pageable);
    }

    /**
     * Lista faturas filtradas por mês e ano
     */
    public Page<Fatura> listarPorMesEAno(Pageable pageable, Integer mes, Integer ano) {
        YearMonth mesAtual = YearMonth.of(ano, mes);
        LocalDate inicioMes = mesAtual.atDay(1);
        LocalDate fimMes = mesAtual.atEndOfMonth();
        
        return faturaRepository.findByDataVencimentoBetween(inicioMes, fimMes, pageable);
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

        // Usar o método pagarFatura ao invés de debitar
        // Esse método cria transação do tipo PAGAMENTO_FATURA que não exige categoria ou conta fixa
        contaService.pagarFatura(contaId, faturaId, fatura.getValorTotal(), "Pagamento de fatura via UI");
        
        // Já não é necessário marcar a fatura como paga aqui, pois o método pagarFatura já faz isso
        // O código abaixo foi mantido apenas para garantir a consistência dos dados
        if (!fatura.isPago()) {
            fatura.setPago(true);
            fatura.setDataPagamento(LocalDate.now());
            fatura.setContaPagamento(conta);
            faturaRepository.save(fatura);
        }
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

    /**
     * Gera preview da fatura mostrando quais compras e parcelas seriam incluídas
     * @param cartaoCreditoId ID do cartão
     * @param dataVencimento Data de vencimento da fatura
     * @return Preview com compras e parcelas
     */
    public PreviewResult gerarPreviewFatura(Long cartaoCreditoId, LocalDate dataVencimento) {
        CartaoCredito cartaoCredito = cartaoCreditoService.findOrFail(cartaoCreditoId);

        // Calcula período de fechamento (10 dias antes do vencimento)
        LocalDate dataFechamento = dataVencimento.minusDays(10);
        LocalDate primeiroDiaMesFechamento = dataFechamento.withDayOfMonth(1);

        // Busca compras à vista do período
        List<Compra> compras = compraService.comprasPorCartaoAteData(cartaoCreditoId, dataVencimento);

        // Busca parcelas que vencem no período
        List<Parcela> parcelas = parcelaRepository.findByCartaoAndPeriodo(
                cartaoCreditoId,
                primeiroDiaMesFechamento,
                dataFechamento
        );

        // Calcula valores
        BigDecimal valorCompras = compras.stream()
                .map(Compra::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal valorParcelas = parcelas.stream()
                .map(Parcela::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PreviewResult(cartaoCredito, compras, parcelas, valorCompras, valorParcelas);
    }

    /**
     * Classe para retornar resultado do preview
     */
    public static class PreviewResult {
        private final CartaoCredito cartaoCredito;
        private final List<Compra> compras;
        private final List<Parcela> parcelas;
        private final BigDecimal valorCompras;
        private final BigDecimal valorParcelas;

        public PreviewResult(CartaoCredito cartaoCredito, List<Compra> compras, List<Parcela> parcelas,
                           BigDecimal valorCompras, BigDecimal valorParcelas) {
            this.cartaoCredito = cartaoCredito;
            this.compras = compras;
            this.parcelas = parcelas;
            this.valorCompras = valorCompras;
            this.valorParcelas = valorParcelas;
        }

        public CartaoCredito getCartaoCredito() { return cartaoCredito; }
        public List<Compra> getCompras() { return compras; }
        public List<Parcela> getParcelas() { return parcelas; }
        public BigDecimal getValorCompras() { return valorCompras; }
        public BigDecimal getValorParcelas() { return valorParcelas; }
        public BigDecimal getValorTotal() { return valorCompras.add(valorParcelas); }
    }
}
