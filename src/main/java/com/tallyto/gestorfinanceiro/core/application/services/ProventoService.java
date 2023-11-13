package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.core.domain.entities.Provento;
import com.tallyto.gestorfinanceiro.core.domain.repositories.ProventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProventoService {

    private final ProventoRepository proventoRepository;

    @Autowired
    public ProventoService(ProventoRepository proventoRepository) {
        this.proventoRepository = proventoRepository;
    }

    public Provento salvarProvento(Provento provento) {
        // Lógica de negócios, validações, etc.
        return proventoRepository.save(provento);
    }

    public List<Provento> listarProventos() {
        // Lógica de negócios, se necessário
        return proventoRepository.findAll();
    }

    // Outros métodos relacionados a proventos
}