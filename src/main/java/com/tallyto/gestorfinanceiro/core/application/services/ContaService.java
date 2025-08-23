package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.core.domain.entities.Conta;
import com.tallyto.gestorfinanceiro.core.domain.enums.TipoConta;
import com.tallyto.gestorfinanceiro.core.infra.repositories.ContaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;


@Service
public class ContaService {

    @Autowired
    private ContaRepository contaRepository;

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
        
        existingConta.setSaldo(conta.getSaldo());
        existingConta.setTitular(conta.getTitular());
        existingConta.setTipo(conta.getTipo());
        existingConta.setTaxaRendimento(conta.getTaxaRendimento());
        existingConta.setDescricao(conta.getDescricao());
        
        return contaRepository.save(existingConta);
    }

    public Conta findOrFail(Long id) {
        return contaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada com ID: " + id));
    }

    @Transactional
    public void debitar(Long contaId, BigDecimal valor) {
        Conta conta = findOrFail(contaId);
        
        if (conta.getSaldo().compareTo(valor) < 0) {
            throw new RuntimeException("Saldo insuficiente na conta");
        }
        
        conta.setSaldo(conta.getSaldo().subtract(valor));
        contaRepository.save(conta);
    }

    @Transactional
    public void creditar(Long contaId, BigDecimal valor) {
        Conta conta = findOrFail(contaId);
        conta.setSaldo(conta.getSaldo().add(valor));
        contaRepository.save(conta);
    }
}


