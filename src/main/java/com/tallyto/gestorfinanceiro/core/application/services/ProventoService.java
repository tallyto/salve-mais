package com.tallyto.gestorfinanceiro.core.application.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tallyto.gestorfinanceiro.core.domain.entities.Provento;
import com.tallyto.gestorfinanceiro.core.domain.entities.Transacao;
import com.tallyto.gestorfinanceiro.core.domain.enums.TipoTransacao;
import com.tallyto.gestorfinanceiro.core.infra.repositories.ProventoRepository;

@Service
public class ProventoService {

    @Autowired
    private ProventoRepository proventoRepository;

    @Autowired
    private ContaService contaService;

    @Autowired
    private TransacaoService transacaoService;

    public Provento getOne(Long id) {
        return proventoRepository.findById(id).orElse(null);
    }

    @Transactional
    public Provento salvarProvento(Provento provento) {
        var account = contaService.getOne(provento.getConta().getId());
        account.setSaldo(account.getSaldo().add(provento.getValor()));

        // Salva o provento primeiro para obter o ID
        Provento proventoSalvo = proventoRepository.save(provento);

        // Cria a transação associada ao provento
        var transacaoDTO = new com.tallyto.gestorfinanceiro.api.dto.TransacaoInputDTO(
                TipoTransacao.CREDITO,
                provento.getValor(),
                provento.getDescricao(),
                provento.getConta().getId(),
                null, // contaDestinoId
                null, // faturaId
                null, // categoriaId
                proventoSalvo.getId(), // proventoId
                null, // contaFixaId
                "Transação gerada automaticamente para o provento #" + proventoSalvo.getId());

        // Cria a transação sem atualizar o saldo (o saldo já foi atualizado acima)
        transacaoService.criarTransacaoSemAtualizarSaldo(transacaoDTO);

        return proventoSalvo;
    }

    public Page<Provento> listarProventos(Pageable pageable) {
        // Lógica de negócios, se necessário
        return proventoRepository.findAll(pageable);
    }

    @Transactional
    public Provento atualizarProvento(Provento provento) {
        Provento existente = proventoRepository.findById(provento.getId())
                .orElseThrow(() -> new IllegalArgumentException("Provento não encontrado"));

        // Verifica se houve alteração de valor para ajustar o saldo
        var diferencaValor = provento.getValor().subtract(existente.getValor());

        // Ajusta o saldo da conta caso haja diferença no valor
        if (diferencaValor.signum() != 0) {
            var conta = contaService.getOne(provento.getConta().getId());
            conta.setSaldo(conta.getSaldo().add(diferencaValor));

            // Busca transações relacionadas para atualizá-las ou criar ajustes
            List<Transacao> transacoesRelacionadas = transacaoService.findByProventoId(provento.getId());

            // Se existe transação associada, cria uma transação de ajuste
            if (!transacoesRelacionadas.isEmpty()) {
                // Cria transação de ajuste se o valor foi alterado
                var tipoAjuste = diferencaValor.signum() > 0 ? TipoTransacao.CREDITO : TipoTransacao.DEBITO;
                var valorAjuste = diferencaValor.abs(); // Valor absoluto da diferença

                // Se houve alteração de valor, cria transação de ajuste
                var transacaoDTO = new com.tallyto.gestorfinanceiro.api.dto.TransacaoInputDTO(
                        tipoAjuste,
                        valorAjuste,
                        "Ajuste de valor do provento #" + provento.getId(),
                        provento.getConta().getId(),
                        null, // contaDestinoId
                        null, // faturaId
                        null, // categoriaId
                        provento.getId(), // proventoId
                        null, // contaFixaId
                        "Ajuste automático devido à alteração do valor do provento");

                // Cria a transação sem atualizar o saldo (o saldo já foi atualizado acima)
                transacaoService.criarTransacaoSemAtualizarSaldo(transacaoDTO);
            }
        }

        // Atualiza os dados do provento
        existente.setDescricao(provento.getDescricao());
        existente.setValor(provento.getValor());
        existente.setData(provento.getData());
        existente.setConta(provento.getConta());

        return proventoRepository.save(existente);
    }

    @Transactional
    public void excluirProvento(Long id) {
        Provento provento = proventoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Provento não encontrado"));

        // Reduzir o saldo da conta ao excluir o provento
        var conta = contaService.getOne(provento.getConta().getId());
        conta.setSaldo(conta.getSaldo().subtract(provento.getValor()));

        proventoRepository.deleteById(id);
    }
}