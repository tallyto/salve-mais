package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.core.domain.entities.Compra;
import com.tallyto.gestorfinanceiro.core.infra.repositories.CompraRepository;
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

        // Verifica se a compra pode ser realizada sem exceder o limite
        if (!cartaoCreditoService.podeRealizarCompra(cartaoCredito.getId(), compra.getValor())) {
            throw new IllegalArgumentException("Compra excede o limite disponível do cartão de crédito");
        }

        return compraRepository.save(compra);
    }

    public Page<Compra> listarCompras(Pageable pageable) {
        return compraRepository.findAll(pageable);
    }

    public Page<Compra> listarComprasPorMesEAno(Pageable pageable, Integer mes, Integer ano) {
        return compraRepository.findByDataMesEAno(pageable, mes, ano);
    }

    public List<Compra> comprasPorCartaoAteData(Long cartaoId, LocalDate dataVencimento) {
        // Calcula a data de fechamento da fatura (último dia do mês anterior ao vencimento)
        LocalDate dataFechamentoFatura = dataVencimento.minusDays(10);

        // Calcula o primeiro dia do mês do fechamento da fatura
        LocalDate primeiroDiaMesFechamento = dataFechamentoFatura.withDayOfMonth(1);

        // Use o repositório para buscar as compras no intervalo desejado
        return compraRepository.findByCartaoCreditoIdAndDataBetween(cartaoId, primeiroDiaMesFechamento, dataFechamentoFatura);
    }

    public Compra buscarCompraPorId(Long id) {
        return compraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra não encontrada com ID: " + id));
    }

    public Compra atualizarCompra(Long id, Compra compraAtualizada) {
        Compra compraExistente = buscarCompraPorId(id);

        // Atualiza os dados da compra
        compraExistente.setDescricao(compraAtualizada.getDescricao());
        compraExistente.setValor(compraAtualizada.getValor());
        compraExistente.setData(compraAtualizada.getData());

        // Atualiza a categoria se foi alterada
        if (compraAtualizada.getCategoria() != null) {
            compraExistente.setCategoria(compraAtualizada.getCategoria());
        }

        // Atualiza o cartão se foi alterado
        if (compraAtualizada.getCartaoCredito() != null) {
            var cartaoCredito = cartaoCreditoService.findOrFail(compraAtualizada.getCartaoCredito().getId());
            compraExistente.setCartaoCredito(cartaoCredito);
        }

        return compraRepository.save(compraExistente);
    }

    public void excluirCompra(Long id) {
        Compra compra = buscarCompraPorId(id);
        compraRepository.delete(compra);
    }
}
