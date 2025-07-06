package com.tallyto.gestorfinanceiro.core.application.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;

    public void enviarEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            logger.info("Email enviado para: {}, assunto: {}", to, subject);
        } catch (Exception e) {
            // Em ambiente de desenvolvimento, apenas logamos o erro e não deixamos a aplicação falhar
            logger.error("Erro ao enviar email para: {}, assunto: {}", to, subject, e);
            logger.info("Conteúdo do email que seria enviado: {}", text);
        }
    }
}
