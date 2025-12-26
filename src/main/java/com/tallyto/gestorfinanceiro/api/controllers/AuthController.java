package com.tallyto.gestorfinanceiro.api.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tallyto.gestorfinanceiro.api.dto.LoginDTO;
import com.tallyto.gestorfinanceiro.api.dto.RecuperarSenhaRequestDTO;
import com.tallyto.gestorfinanceiro.api.dto.RedefinirSenhaRequestDTO;
import com.tallyto.gestorfinanceiro.api.dto.TokenDTO;
import com.tallyto.gestorfinanceiro.core.application.services.EmailService;
import com.tallyto.gestorfinanceiro.core.application.services.JwtService;
import com.tallyto.gestorfinanceiro.core.application.services.PasswordResetTokenService;
import com.tallyto.gestorfinanceiro.core.application.services.UsuarioService;

@RestController
@RequestMapping("api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private PasswordResetTokenService passwordResetTokenService;
    @Autowired
    private UsuarioService usuarioService;
    
    @Value("${app.password.reset.url}")
    private String passwordResetUrl;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getSenha()));
            // Atualiza o campo ultimoAcesso
            usuarioService.atualizarUltimoAcesso(loginDTO.getEmail());
            
            // Buscar usuário para obter o tenantId
            var usuario = usuarioService.buscarPorEmail(loginDTO.getEmail());
            String token = jwtService.gerarToken(loginDTO.getEmail(), usuario.getTenantId());
            return ResponseEntity.ok(new TokenDTO(token));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Usuário ou senha inválidos");
        }
    }

    @PostMapping("/recuperar-senha")
    public ResponseEntity<?> recuperarSenha(@RequestBody RecuperarSenhaRequestDTO dto) {
        logger.info("Solicitação de recuperação de senha para email: {}", dto.getEmail());
        
        try {
            // Buscar usuário para obter o nome
            var usuario = usuarioService.buscarPorEmail(dto.getEmail());
            
            String token = java.util.UUID.randomUUID().toString();
            logger.info("Token gerado para recuperação de senha: {} para email: {}", token, dto.getEmail());
            
            passwordResetTokenService.storeToken(token, dto.getEmail());
            String link = passwordResetUrl + "?token=" + token;
            
            // Enviar email com template HTML
            emailService.enviarEmailRecuperacaoSenha(dto.getEmail(), usuario.getNome(), link);
            logger.info("Email de recuperação de senha enviado com sucesso para: {}", dto.getEmail());
            
            return ResponseEntity.ok(java.util.Collections.singletonMap("message",
                    "Instruções de recuperação enviadas para o e-mail, se existir na base."));
        } catch (RuntimeException e) {
            logger.warn("Erro na recuperação de senha para email: {} - {}", dto.getEmail(), e.getMessage());
            // Mesmo que o usuário não exista, retornamos sucesso por segurança
            // Isso evita que alguém descubra quais emails estão cadastrados
            return ResponseEntity.ok(java.util.Collections.singletonMap("message",
                    "Instruções de recuperação enviadas para o e-mail, se existir na base."));
        }
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<?> redefinirSenha(@RequestBody RedefinirSenhaRequestDTO dto) {
        logger.info("Tentativa de redefinição de senha com token: {}", dto.getToken());
        
        String email = passwordResetTokenService.getEmailIfValid(dto.getToken());
        if (email == null) {
            logger.warn("Token inválido ou expirado para redefinição de senha: {}", dto.getToken());
            return ResponseEntity.status(400)
                    .body(java.util.Collections.singletonMap("error", "Token inválido ou expirado."));
        }
        
        logger.info("Token válido, redefinindo senha para email: {}", email);
        usuarioService.atualizarSenhaPorEmail(email, dto.getNovaSenha());
        passwordResetTokenService.removeToken(dto.getToken());
        
        logger.info("Senha redefinida com sucesso para email: {}", email);
        return ResponseEntity.ok(java.util.Collections.singletonMap("message", "Senha redefinida com sucesso."));
    }

    @GetMapping("/verificar-token")
    public ResponseEntity<?> verificarToken(@RequestParam String token) {
        logger.info("Verificando validade do token: {}", token);
        
        String email = passwordResetTokenService.getEmailIfValid(token);
        if (email == null) {
            logger.warn("Token inválido ou expirado na verificação: {}", token);
            return ResponseEntity.status(400)
                    .body(java.util.Collections.singletonMap("error", "Token inválido ou expirado."));
        }
        
        logger.info("Token válido para email: {}", email);
        return ResponseEntity.ok(java.util.Collections.singletonMap("valid", true));
    }
}
