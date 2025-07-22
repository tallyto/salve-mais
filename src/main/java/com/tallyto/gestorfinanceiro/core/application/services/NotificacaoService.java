package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.core.domain.entities.ContaFixa;
import com.tallyto.gestorfinanceiro.core.domain.entities.Fatura;
import com.tallyto.gestorfinanceiro.api.dto.NotificacaoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificacaoService {

    @Autowired
    private ContaFixaService contaFixaService;

    @Autowired
    private FaturaService faturaService;

    /**
     * Obtém todas as notificações de contas em atraso e próximas do vencimento
     */
    public List<NotificacaoDTO> obterNotificacoes() {
        List<NotificacaoDTO> notificacoes = new ArrayList<>();
        
        // Adicionar notificações de contas fixas atrasadas
        notificacoes.addAll(obterNotificacaoContasAtrasadas());
        
        // Adicionar notificações de contas próximas do vencimento
        notificacoes.addAll(obterNotificacaoContasProximasVencimento());
        
        // Adicionar notificações de faturas atrasadas
        notificacoes.addAll(obterNotificacaoFaturasAtrasadas());
        
        return notificacoes;
    }

    /**
     * Obtém notificações de contas fixas em atraso
     */
    public List<NotificacaoDTO> obterNotificacaoContasAtrasadas() {
        List<ContaFixa> contasAtrasadas = contaFixaService.listarContasFixasVencidasNaoPagas();
        
        return contasAtrasadas.stream()
                .map(conta -> {
                    long diasAtraso = ChronoUnit.DAYS.between(conta.getVencimento(), LocalDate.now());
                    
                    return new NotificacaoDTO(
                            "CONTA_ATRASADA",
                            NotificacaoDTO.Prioridade.ALTA,
                            "Conta em Atraso",
                            String.format("%s está atrasada há %d dia(s)", conta.getNome(), diasAtraso),
                            conta.getId(),
                            "CONTA_FIXA",
                            diasAtraso
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtém notificações de contas próximas do vencimento (próximos 7 dias)
     */
    public List<NotificacaoDTO> obterNotificacaoContasProximasVencimento() {
        LocalDate hoje = LocalDate.now();
        LocalDate limiteFuturo = hoje.plusDays(7);
        
        List<ContaFixa> contasProximas = contaFixaService.listarContaFixaPorPeriodo(hoje, limiteFuturo)
                .stream()
                .filter(conta -> !conta.isPago())
                .collect(Collectors.toList());
        
        return contasProximas.stream()
                .map(conta -> {
                    long diasRestantes = ChronoUnit.DAYS.between(hoje, conta.getVencimento());
                    NotificacaoDTO.Prioridade prioridade = diasRestantes <= 2 ? 
                            NotificacaoDTO.Prioridade.ALTA : NotificacaoDTO.Prioridade.MEDIA;
                    
                    String titulo = diasRestantes == 0 ? "Vence Hoje" : 
                                  diasRestantes == 1 ? "Vence Amanhã" : 
                                  String.format("Vence em %d dias", diasRestantes);
                    
                    return new NotificacaoDTO(
                            "CONTA_PROXIMA_VENCIMENTO",
                            prioridade,
                            titulo,
                            String.format("%s vence em %s", conta.getNome(), 
                                    conta.getVencimento().toString()),
                            conta.getId(),
                            "CONTA_FIXA",
                            diasRestantes
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtém notificações de faturas em atraso
     */
    public List<NotificacaoDTO> obterNotificacaoFaturasAtrasadas() {
        List<Fatura> faturas = faturaService.listar();
        LocalDate hoje = LocalDate.now();
        
        List<Fatura> faturasAtrasadas = faturas.stream()
                .filter(fatura -> !fatura.isPago() && fatura.getDataVencimento().isBefore(hoje))
                .collect(Collectors.toList());
        
        return faturasAtrasadas.stream()
                .map(fatura -> {
                    long diasAtraso = ChronoUnit.DAYS.between(fatura.getDataVencimento(), hoje);
                    
                    return new NotificacaoDTO(
                            "FATURA_ATRASADA",
                            NotificacaoDTO.Prioridade.ALTA,
                            "Fatura em Atraso",
                            String.format("Fatura do cartão %s está atrasada há %d dia(s)", 
                                    fatura.getCartaoCredito().getNome(), diasAtraso),
                            fatura.getId(),
                            "FATURA",
                            diasAtraso
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtém o total de notificações não lidas/ativas
     */
    public long obterTotalNotificacoes() {
        return obterNotificacoes().size();
    }
}
