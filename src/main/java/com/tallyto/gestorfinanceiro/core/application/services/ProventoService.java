package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.core.domain.entities.Provento;
import com.tallyto.gestorfinanceiro.core.infra.repositories.ProventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class ProventoService {

    @Autowired
    private ProventoRepository proventoRepository;

    @Autowired
    private ContaService contaService;

    public Provento getOne(Long id) {
        return proventoRepository.findById(id).orElse(null);
    }

    public Provento salvarProvento(Provento provento) {
        var account = contaService.getOne(provento.getConta().getId());
        account.setSaldo(account.getSaldo().add(provento.getValor()));
        // TODO: criar transação
        return proventoRepository.save(provento);
    }

    public Page<Provento> listarProventos(Pageable pageable) {
        // Lógica de negócios, se necessário
        return proventoRepository.findAll(pageable);
    }

    public Provento atualizarProvento(Provento provento) {
        Provento existente = proventoRepository.findById(provento.getId())
                .orElseThrow(() -> new IllegalArgumentException("Provento não encontrado"));
        existente.setDescricao(provento.getDescricao());
        existente.setValor(provento.getValor());
        existente.setData(provento.getData());
        existente.setConta(provento.getConta());
        // Aqui pode ser necessário ajustar saldo da conta, se a lógica exigir
        return proventoRepository.save(existente);
    }

    public void excluirProvento(Long id) {
        Provento provento = proventoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Provento não encontrado"));
        
        // Reduzir o saldo da conta ao excluir o provento
        var conta = contaService.getOne(provento.getConta().getId());
        conta.setSaldo(conta.getSaldo().subtract(provento.getValor()));
        
        proventoRepository.deleteById(id);
    }
}