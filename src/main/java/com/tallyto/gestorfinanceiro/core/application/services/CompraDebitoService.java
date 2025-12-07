package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.core.domain.entities.CompraDebito;
import com.tallyto.gestorfinanceiro.core.infra.repositories.CompraDebitoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class CompraDebitoService {

    @Autowired
    private CompraDebitoRepository compraDebitoRepository;

    @Autowired
    private ContaService contaService;

    @Autowired
    private CategoriaService categoriaService;
    
    @Autowired
    private TransacaoService transacaoService;

    /**
     * Salva uma nova compra em débito.
     * Ao salvar, automaticamente debita o valor da conta vinculada e cria a transação.
     */
    @org.springframework.transaction.annotation.Transactional
    public CompraDebito salvarCompraDebito(CompraDebito compraDebito) {
        var conta = contaService.getOne(compraDebito.getConta().getId());
        if (conta == null) {
            throw new IllegalArgumentException("Conta não encontrada");
        }
        
        // Verifica se a conta tem saldo suficiente
        if (conta.getSaldo().compareTo(compraDebito.getValor()) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente na conta para realizar esta compra");
        }
        
        // Atualiza o saldo da conta
        conta.setSaldo(conta.getSaldo().subtract(compraDebito.getValor()));
        
        // Salva a compra em débito primeiro para obter o ID
        CompraDebito compraDebitoSalva = compraDebitoRepository.save(compraDebito);
        
        // Cria a transação associada
        var transacaoDTO = new com.tallyto.gestorfinanceiro.api.dto.TransacaoInputDTO(
            com.tallyto.gestorfinanceiro.core.domain.enums.TipoTransacao.DEBITO,
            compraDebito.getValor(),
            "Compra em débito: " + compraDebito.getNome(),
            compraDebito.getConta().getId(),
            null, // contaDestinoId
            null, // faturaId
            compraDebito.getCategoria() != null ? compraDebito.getCategoria().getId() : null,
            null, // proventoId
            null, // contaFixaId
            compraDebito.getObservacoes() != null ? compraDebito.getObservacoes() : 
                "Transação gerada automaticamente para a compra em débito #" + compraDebitoSalva.getId()
        );
        
        // Cria a transação sem atualizar o saldo (já atualizamos acima)
        transacaoService.criarTransacaoSemAtualizarSaldo(transacaoDTO);
        
        return compraDebitoSalva;
    }
    
    /**
     * Atualiza uma compra em débito existente.
     * Nota: Não permite alterar o valor, pois já foi debitado.
     */
    @org.springframework.transaction.annotation.Transactional
    public CompraDebito atualizarCompraDebito(Long id, CompraDebito compraDebitoAtualizada) {
        CompraDebito compraDebitoExistente = buscarCompraDebitoPorId(id);
        if (compraDebitoExistente == null) {
            throw new IllegalArgumentException("Compra em débito não encontrada");
        }
        
        // Não permite alterar o valor, pois já foi debitado da conta
        if (!compraDebitoExistente.getValor().equals(compraDebitoAtualizada.getValor())) {
            throw new IllegalArgumentException("Não é possível alterar o valor de uma compra já realizada");
        }
        
        compraDebitoExistente.setNome(compraDebitoAtualizada.getNome());
        compraDebitoExistente.setCategoria(compraDebitoAtualizada.getCategoria());
        compraDebitoExistente.setObservacoes(compraDebitoAtualizada.getObservacoes());
        
        return compraDebitoRepository.save(compraDebitoExistente);
    }

    public List<CompraDebito> listarCompraPorCategoria(Long categoriaId) {
        return compraDebitoRepository.findByCategoria_Id(categoriaId);
    }

    public List<CompraDebito> listarCompraPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return compraDebitoRepository.findByDataCompraBetween(dataInicio, dataFim);
    }

    public BigDecimal calcularTotalPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        BigDecimal total = compraDebitoRepository.calcularTotalPorPeriodo(dataInicio, dataFim);
        return total != null ? total : BigDecimal.ZERO;
    }

    public Page<CompraDebito> listarTodasCompras(Pageable pageable) {
        return compraDebitoRepository.findAll(pageable);
    }

    public Page<CompraDebito> listarComprasPorMesEAno(Pageable pageable, Integer mes, Integer ano) {
        return compraDebitoRepository.findByDataCompraMesEAno(pageable, mes, ano);
    }

    public CompraDebito buscarCompraDebitoPorId(Long id) {
        return compraDebitoRepository.findById(id).orElse(null);
    }

    @org.springframework.transaction.annotation.Transactional
    public void deletarCompraDebito(Long id) {
        compraDebitoRepository.deleteById(id);
    }
}
