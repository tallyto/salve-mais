package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.core.domain.entities.Conta;
import com.tallyto.gestorfinanceiro.core.infra.repositories.ContaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


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

    public Conta create(Conta acc) {
        return  contaRepository.save(acc);
    }


}
