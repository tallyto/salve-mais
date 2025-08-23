package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.core.domain.entities.Conta;
import com.tallyto.gestorfinanceiro.core.domain.enums.TipoConta;
import com.tallyto.gestorfinanceiro.core.infra.repositories.ContaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class RendimentoService {

    @Autowired
    private ContaRepository contaRepository;
    
    /**
     * Calcula o rendimento mensal das contas de investimento e reserva de emergência
     * Este método é executado automaticamente no dia 1 de cada mês
     */
    @Scheduled(cron = "0 0 0 1 * ?") // Executa às 00:00:00 do primeiro dia de cada mês
    public void calcularRendimentoMensal() {
        List<Conta> contas = contaRepository.findByTipoIn(
            List.of(TipoConta.INVESTIMENTO, TipoConta.RESERVA_EMERGENCIA)
        );
        
        for (Conta conta : contas) {
            if (conta.getTaxaRendimento() != null && conta.getTaxaRendimento().compareTo(BigDecimal.ZERO) > 0) {
                // Calcula o rendimento mensal (taxa anual / 12)
                BigDecimal taxaMensal = conta.getTaxaRendimento().divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);
                BigDecimal rendimento = conta.getSaldo().multiply(taxaMensal).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                
                // Atualiza o saldo da conta
                conta.setSaldo(conta.getSaldo().add(rendimento));
                contaRepository.save(conta);
            }
        }
    }
    
    /**
     * Calcula o rendimento para uma determinada conta de acordo com sua taxa configurada
     * @param conta A conta para calcular rendimento
     * @param meses Número de meses para projeção
     * @return Valor projetado após o período
     */
    public BigDecimal projetarRendimento(Conta conta, int meses) {
        if (conta.getTaxaRendimento() == null || conta.getTaxaRendimento().compareTo(BigDecimal.ZERO) <= 0) {
            return conta.getSaldo();
        }
        
        BigDecimal taxaMensal = conta.getTaxaRendimento().divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP)
                                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                                .add(BigDecimal.ONE);
        
        // Fórmula: saldo * (1 + taxa)^meses
        BigDecimal valorFinal = conta.getSaldo();
        for (int i = 0; i < meses; i++) {
            valorFinal = valorFinal.multiply(taxaMensal).setScale(2, RoundingMode.HALF_UP);
        }
        
        return valorFinal;
    }
}
