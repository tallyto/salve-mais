package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.core.domain.entities.Transacao;
import com.tallyto.gestorfinanceiro.core.infra.repositories.TransacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class TransacaoService {

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private ContaService contaService;


    @Autowired
    private ProventoService proventoService;


    @Transactional
    public Transacao save(Transacao transacao) {

        var account = contaService.getOne(transacao.getConta().getId());

        var provento = proventoService.getOne(transacao.getProvento().getId());

        transacao.setValor(provento.getValor());

        account.setSaldo(account.getSaldo().add(provento.getValor()));

        transacao.setData(LocalDate.now());


        return transacaoRepository.save(transacao);
    }


}
