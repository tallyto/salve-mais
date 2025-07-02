package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.core.application.services.TransacaoService;
import com.tallyto.gestorfinanceiro.core.domain.entities.Transacao;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/transacao")
public class TransacaoController {

    private final TransacaoService  transacaoService;

    public TransacaoController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }


    @PostMapping
    public Transacao save(@RequestBody Transacao  transacao){
        return transacaoService.save(transacao);
    }

}
