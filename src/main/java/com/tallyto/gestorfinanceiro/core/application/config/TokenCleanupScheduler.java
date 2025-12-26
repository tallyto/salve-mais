package com.tallyto.gestorfinanceiro.core.application.config;

import com.tallyto.gestorfinanceiro.core.application.services.PasswordResetTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class TokenCleanupScheduler {
    private static final Logger logger = LoggerFactory.getLogger(TokenCleanupScheduler.class);
    
    @Autowired
    private PasswordResetTokenService passwordResetTokenService;

    /**
     * Limpa tokens expirados a cada 1 hora
     */
    @Scheduled(fixedRate = 3600000) // 1 hora em milissegundos
    public void cleanupExpiredTokens() {
        logger.info("Iniciando limpeza automática de tokens expirados");
        try {
            passwordResetTokenService.cleanAllExpiredTokens();
            logger.info("Limpeza automática de tokens concluída com sucesso");
        } catch (Exception e) {
            logger.error("Erro durante a limpeza automática de tokens", e);
        }
    }
}