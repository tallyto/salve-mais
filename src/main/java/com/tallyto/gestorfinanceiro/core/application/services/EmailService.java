package com.tallyto.gestorfinanceiro.core.application.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${app.mail.from}")
    private String mailFrom;
    
    @Value("${app.mail.from.name}")
    private String mailFromName;

    public void enviarEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFromName + " <" + mailFrom + ">");
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
    
    public void enviarEmailHtml(String to, String subject, String templateName, String tenantName, String confirmationLink) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            // Ler template HTML
            ClassPathResource resource = new ClassPathResource("templates/" + templateName);
            String htmlTemplate = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            
            // Substituir placeholders
            String htmlContent = htmlTemplate
                .replace("{{TENANT_NAME}}", tenantName)
                .replace("{{CONFIRMATION_LINK}}", confirmationLink);
            
            helper.setFrom(mailFrom, mailFromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(mimeMessage);
            logger.info("Email HTML enviado para: {}, assunto: {}", to, subject);
        } catch (Exception e) {
            // Em ambiente de desenvolvimento, apenas logamos o erro e não deixamos a aplicação falhar
            logger.error("Erro ao enviar email HTML para: {}, assunto: {}", to, subject, e);
            logger.info("Link de confirmação que seria enviado: {}", confirmationLink);
        }
    }
    
    public void enviarEmailRecuperacaoSenha(String to, String userName, String resetLink) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            // Ler template HTML
            ClassPathResource resource = new ClassPathResource("templates/recuperacao-senha.html");
            String htmlTemplate = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            
            // Substituir placeholders
            String htmlContent = htmlTemplate
                .replace("{{USER_NAME}}", userName)
                .replace("{{RESET_LINK}}", resetLink);
            
            helper.setFrom(mailFrom, mailFromName);
            helper.setTo(to);
            helper.setSubject("Recuperação de Senha - Salve Mais");
            helper.setText(htmlContent, true);
            
            mailSender.send(mimeMessage);
            logger.info("Email de recuperação de senha enviado para: {}", to);
        } catch (Exception e) {
            // Em ambiente de desenvolvimento, apenas logamos o erro e não deixamos a aplicação falhar
            logger.error("Erro ao enviar email de recuperação de senha para: {}", to, e);
            logger.info("Link de recuperação que seria enviado: {}", resetLink);
        }
    }
}
