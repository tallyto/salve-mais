package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.core.domain.entities.ContaFixa;
import com.tallyto.gestorfinanceiro.core.infra.repositories.ContaFixaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ContaFixaService {

    private final ContaFixaRepository contaFixaRepository;

    @Autowired
    public ContaFixaService(ContaFixaRepository contaFixaRepository) {
        this.contaFixaRepository = contaFixaRepository;
    }

    public ContaFixa salvarContaFixa(ContaFixa contaFixa) {
        return contaFixaRepository.save(contaFixa);
    }

    public BigDecimal calcularTotalContasFixasNaoPagas() {
        LocalDate hoje = LocalDate.now();
        return contaFixaRepository.calcularTotalContasFixasNaoPagas(hoje);
    }

    public List<ContaFixa> listarContaFixaPorCategoria(Long categoriaId) {
        return contaFixaRepository.findByCategoria_Id(categoriaId);
    }

    public List<ContaFixa> listarContasFixasVencidasNaoPagas() {
        LocalDate hoje = LocalDate.now();
        return contaFixaRepository.findByVencimentoBeforeAndPagoIsFalse(hoje);
    }

    // Outros métodos relacionados a contas fixas

    public Page<ContaFixa> listarTodasContasFixas(Pageable pageable) {
        return contaFixaRepository.findAll(pageable);
    }

    public ContaFixa buscarContaFixaPorId(Long id) {
        return contaFixaRepository.findById(id).orElse(null);
    }

    public void deletarContaFixa(Long id) {
        contaFixaRepository.deleteById(id);
    }

    // Outros métodos, se necessário
}
