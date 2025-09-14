package com.tallyto.gestorfinanceiro.core.application.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tallyto.gestorfinanceiro.api.dto.TransacaoInputDTO;
import com.tallyto.gestorfinanceiro.core.domain.entities.Conta;
import com.tallyto.gestorfinanceiro.core.domain.entities.Fatura;
import com.tallyto.gestorfinanceiro.core.domain.enums.TipoConta;
import com.tallyto.gestorfinanceiro.core.domain.enums.TipoTransacao;
import com.tallyto.gestorfinanceiro.core.domain.exceptions.EntityInUseException;
import com.tallyto.gestorfinanceiro.core.domain.exceptions.ResourceNotFoundException;
import com.tallyto.gestorfinanceiro.core.domain.exceptions.TransacaoException;
import com.tallyto.gestorfinanceiro.core.infra.repositories.ContaRepository;
import com.tallyto.gestorfinanceiro.core.infra.repositories.FaturaRepository;


@Service
public class ContaService {

    @Autowired
    private ContaRepository contaRepository;
    
    @Autowired
    private TransacaoService transacaoService;
    
    @Autowired
    private FaturaRepository faturaRepository;
    
    public Conta getOne(Long id) {
        return contaRepository.findById(id).orElse(null);
    }

    public Page<Conta> findAllAccounts(Pageable pageable) {
        return contaRepository.findAll(pageable);
    }
    
    public List<Conta> findByTipo(TipoConta tipo) {
        return contaRepository.findByTipo(tipo);
    }
    
    public List<Conta> findByTipoIn(List<TipoConta> tipos) {
        return contaRepository.findByTipoIn(tipos);
    }

    public Conta create(Conta acc) {
        return contaRepository.save(acc);
    }

    public Conta update(Long id, Conta conta) {
        Conta existingConta = contaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));
        
        // Não atualiza o saldo - o saldo só deve ser alterado via transações, transferências, etc.
        existingConta.setTitular(conta.getTitular());
        existingConta.setTipo(conta.getTipo());
        existingConta.setTaxaRendimento(conta.getTaxaRendimento());
        existingConta.setDescricao(conta.getDescricao());
        
        return contaRepository.save(existingConta);
    }

    public Conta findOrFail(Long id) {
        return contaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada com ID: " + id));
    }

    @Transactional
    public void excluirConta(Long id) {
        Conta conta = findOrFail(id);
        
        try {
            contaRepository.delete(conta);
        } catch (DataIntegrityViolationException e) {
            throw new EntityInUseException("conta", id, "transações, reservas de emergência ou outras operações");
        }
    }

    /**
     * Debita um valor da conta e registra a transação
     */
    @Transactional
    public void debitar(Long contaId, BigDecimal valor) {
        debitar(contaId, valor, null, null, null, "Débito manual");
    }
    
    /**
     * Debita um valor da conta e registra a transação com informações adicionais
     */
    @Transactional
    public void debitar(Long contaId, BigDecimal valor, Long categoriaId, Long contaFixaId, String observacoes, String descricao) {
        Conta conta = findOrFail(contaId);
        
        if (conta.getSaldo().compareTo(valor) < 0) {
            throw new RuntimeException("Saldo insuficiente na conta");
        }
        
        // Atualiza o saldo da conta
        atualizarSaldoDebito(conta, valor);
        
        // Registra a transação
        TransacaoInputDTO transacaoDTO = new TransacaoInputDTO(
                TipoTransacao.DEBITO,
                valor,
                descricao,
                contaId,
                null,
                null,
                categoriaId,
                null,
                contaFixaId,
                observacoes
        );
        
        transacaoService.criarTransacaoSemAtualizarSaldo(transacaoDTO);
    }

    /**
     * Credita um valor na conta e registra a transação
     */
    @Transactional
    public void creditar(Long contaId, BigDecimal valor) {
        creditar(contaId, valor, null, null, null, "Crédito manual");
    }
    
    /**
     * Credita um valor na conta e registra a transação com informações adicionais
     */
    @Transactional
    public void creditar(Long contaId, BigDecimal valor, Long categoriaId, Long proventoId, String observacoes, String descricao) {
        Conta conta = findOrFail(contaId);
        
        // Atualiza o saldo da conta
        atualizarSaldoCredito(conta, valor);
        
        // Registra a transação
        TransacaoInputDTO transacaoDTO = new TransacaoInputDTO(
                TipoTransacao.CREDITO,
                valor,
                descricao,
                contaId,
                null,
                null,
                categoriaId,
                proventoId,
                null,
                observacoes
        );
        
        transacaoService.criarTransacaoSemAtualizarSaldo(transacaoDTO);
    }
    
    /**
     * Transfere um valor entre contas e registra as transações
     */
    @Transactional
    public void transferir(Long contaOrigemId, Long contaDestinoId, BigDecimal valor) {
        transferir(contaOrigemId, contaDestinoId, valor, null, "Transferência entre contas", null);
    }
    
    /**
     * Transfere um valor entre contas e registra as transações com informações adicionais
     */
    @Transactional
    public void transferir(Long contaOrigemId, Long contaDestinoId, BigDecimal valor, 
                          Long categoriaId, String descricao, String observacoes) {
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da transferência deve ser maior que zero");
        }
        
        Conta contaOrigem = findOrFail(contaOrigemId);
        Conta contaDestino = findOrFail(contaDestinoId);
        
        if (contaOrigem.getSaldo().compareTo(valor) < 0) {
            throw new TransacaoException("Saldo insuficiente na conta de origem");
        }
        
        // Atualiza os saldos
        atualizarSaldoDebito(contaOrigem, valor);
        atualizarSaldoCredito(contaDestino, valor);
        
        // Registra as transações
        TransacaoInputDTO transacaoSaidaDTO = new TransacaoInputDTO(
                TipoTransacao.TRANSFERENCIA_SAIDA,
                valor,
                descricao,
                contaOrigemId,
                contaDestinoId,
                null,
                categoriaId,
                null,
                null,
                observacoes
        );
        
        TransacaoInputDTO transacaoEntradaDTO = new TransacaoInputDTO(
                TipoTransacao.TRANSFERENCIA_ENTRADA,
                valor,
                "Transferência recebida de " + contaOrigem.getTitular(),
                contaDestinoId,
                contaOrigemId,
                null,
                categoriaId,
                null,
                null,
                observacoes
        );
        
        transacaoService.criarTransacaoSemAtualizarSaldo(transacaoSaidaDTO);
        transacaoService.criarTransacaoSemAtualizarSaldo(transacaoEntradaDTO);
    }
    
    /**
     * Adiciona saldo inicial a uma conta
     */
    @Transactional
    public void adicionarSaldoInicial(Long contaId, BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do saldo inicial deve ser maior que zero");
        }
        
        Conta conta = findOrFail(contaId);
        
        // Atualiza o saldo
        atualizarSaldoCredito(conta, valor);
        
        // Registra a transação
        TransacaoInputDTO transacaoDTO = new TransacaoInputDTO(
                TipoTransacao.CREDITO,
                valor,
                "Saldo inicial",
                contaId,
                null,
                null,
                null,
                null,
                null,
                "Saldo inicial da conta"
        );
        
        transacaoService.criarTransacaoSemAtualizarSaldo(transacaoDTO);
    }
    
    /**
     * Registra um pagamento de fatura
     */
    @Transactional
    public void pagarFatura(Long contaId, Long faturaId, BigDecimal valor, String observacoes) {
        Conta conta = findOrFail(contaId);
        Fatura fatura = faturaRepository.findById(faturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Fatura não encontrada com ID: " + faturaId));
        
        if (fatura.isPago()) {
            throw new TransacaoException("Esta fatura já foi paga");
        }
        
        if (conta.getSaldo().compareTo(valor) < 0) {
            throw new TransacaoException("Saldo insuficiente para pagar a fatura");
        }
        
        // Atualiza o saldo da conta
        atualizarSaldoDebito(conta, valor);
        
        // Atualiza a fatura
        fatura.setPago(true);
        fatura.setDataPagamento(LocalDate.now());
        fatura.setContaPagamento(conta);
        faturaRepository.save(fatura);
        
        // Registra a transação
        TransacaoInputDTO transacaoDTO = new TransacaoInputDTO(
                TipoTransacao.PAGAMENTO_FATURA,
                valor,
                "Pagamento de fatura " + fatura.getId(),
                contaId,
                null,
                faturaId,
                null,
                null,
                null,
                observacoes
        );
        
        transacaoService.criarTransacaoSemAtualizarSaldo(transacaoDTO);
    }
    
 
    
    // Métodos auxiliares para atualização de saldo
    
    /**
     * Atualiza o saldo de uma conta adicionando um valor (crédito)
     */
    private void atualizarSaldoCredito(Conta conta, BigDecimal valor) {
        conta.setSaldo(conta.getSaldo().add(valor));
        contaRepository.save(conta);
    }
    
    /**
     * Atualiza o saldo de uma conta subtraindo um valor (débito)
     */
    private void atualizarSaldoDebito(Conta conta, BigDecimal valor) {
        if (conta.getSaldo().compareTo(valor) < 0) {
            throw new TransacaoException("Saldo insuficiente na conta");
        }
        conta.setSaldo(conta.getSaldo().subtract(valor));
        contaRepository.save(conta);
    }
}