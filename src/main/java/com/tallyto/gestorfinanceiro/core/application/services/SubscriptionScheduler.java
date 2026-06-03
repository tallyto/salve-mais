package com.tallyto.gestorfinanceiro.core.application.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionScheduler {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionScheduler.class);

    @Autowired
    private SubscriptionService subscriptionService;

    // Executa todo dia às 02:00
    @Scheduled(cron = "0 0 2 * * *", zone = "America/Sao_Paulo")
    public void expirarTrialsVencidos() {
        log.info("Verificando trials expirados...");
        try {
            subscriptionService.expirarTrialsVencidos();
            log.info("Verificação de trials concluída.");
        } catch (Exception e) {
            log.error("Erro ao expirar trials: {}", e.getMessage(), e);
        }
    }
}
