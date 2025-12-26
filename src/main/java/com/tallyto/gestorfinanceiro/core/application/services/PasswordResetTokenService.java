package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.core.domain.entities.PasswordResetToken;
import com.tallyto.gestorfinanceiro.core.infra.repositories.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PasswordResetTokenService {
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetTokenService.class);
    
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    private static final int EXPIRATION_MINUTES = 120; // Aumentado de 30 para 120 minutos (2 horas)

    public void storeToken(String token, String email) {
        // Limpar tokens antigos do email antes de criar um novo
        cleanExpiredTokensForEmail(email);
        
        PasswordResetToken entity = new PasswordResetToken();
        entity.setToken(token);
        entity.setEmail(email);
        entity.setExpiry(java.time.LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES));
        tokenRepository.save(entity);
        
        logger.info("Token de reset de senha criado para email: {} com expiração: {}", email, entity.getExpiry());
    }

    public String getEmailIfValid(String token) {
        logger.info("Verificando token de reset: {}", token);
        
        return tokenRepository.findByToken(token)
            .filter(t -> {
                boolean isValid = t.getExpiry().isAfter(java.time.LocalDateTime.now());
                logger.info("Token encontrado para email: {}, expira em: {}, é válido: {}", 
                    t.getEmail(), t.getExpiry(), isValid);
                return isValid;
            })
            .map(PasswordResetToken::getEmail)
            .orElse(null);
    }

    @Transactional
    public void removeToken(String token) {
        logger.info("Removendo token: {}", token);
        tokenRepository.deleteByToken(token);
    }
    
    @Transactional
    public void cleanExpiredTokensForEmail(String email) {
        logger.info("Limpando tokens expirados para email: {}", email);
        tokenRepository.deleteByEmailAndExpiryBefore(email, java.time.LocalDateTime.now());
    }
    
    @Transactional
    public void cleanAllExpiredTokens() {
        logger.info("Limpando todos os tokens expirados");
        int deletedCount = tokenRepository.deleteByExpiryBefore(java.time.LocalDateTime.now());
        logger.info("Tokens expirados removidos: {}", deletedCount);
    }
}
