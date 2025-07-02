package com.tallyto.gestorfinanceiro.core.application.services;

import com.tallyto.gestorfinanceiro.core.domain.entities.PasswordResetToken;
import com.tallyto.gestorfinanceiro.core.infra.repositories.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetTokenService {
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    private static final int EXPIRATION_MINUTES = 30;

    public void storeToken(String token, String email) {
        PasswordResetToken entity = new PasswordResetToken();
        entity.setToken(token);
        entity.setEmail(email);
        entity.setExpiry(java.time.LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES));
        tokenRepository.save(entity);
    }

    public String getEmailIfValid(String token) {
        return tokenRepository.findByToken(token)
            .filter(t -> t.getExpiry().isAfter(java.time.LocalDateTime.now()))
            .map(PasswordResetToken::getEmail)
            .orElse(null);
    }

    @Transactional
    public void removeToken(String token) {
        tokenRepository.deleteByToken(token);
    }
}
