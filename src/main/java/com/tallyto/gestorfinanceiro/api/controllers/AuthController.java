package com.tallyto.gestorfinanceiro.api.controllers;

import com.tallyto.gestorfinanceiro.api.dto.LoginDTO;
import com.tallyto.gestorfinanceiro.api.dto.RecuperarSenhaRequestDTO;
import com.tallyto.gestorfinanceiro.api.dto.RedefinirSenhaRequestDTO;
import com.tallyto.gestorfinanceiro.api.dto.TokenDTO;
import com.tallyto.gestorfinanceiro.core.application.services.EmailService;
import com.tallyto.gestorfinanceiro.core.application.services.JwtService;
import com.tallyto.gestorfinanceiro.core.application.services.PasswordResetTokenService;
import com.tallyto.gestorfinanceiro.core.application.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
public class AuthController {
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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getSenha()));
            String token = jwtService.gerarToken(loginDTO.getEmail());
            return ResponseEntity.ok(new TokenDTO(token));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Usuário ou senha inválidos");
        }
    }

    @PostMapping("/recuperar-senha")
    public ResponseEntity<?> recuperarSenha(@RequestBody RecuperarSenhaRequestDTO dto) {
        String token = java.util.UUID.randomUUID().toString();
        passwordResetTokenService.storeToken(token, dto.getEmail());
        String link = "http://localhost:4200/redefinir-senha?token=" + token;
        String mensagem = "Clique no link para redefinir sua senha: " + link;
        emailService.enviarEmail(dto.getEmail(), "Recuperação de Senha", mensagem);
        return ResponseEntity.ok(java.util.Collections.singletonMap("message",
                "Instruções de recuperação enviadas para o e-mail, se existir na base."));
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<?> redefinirSenha(@RequestBody RedefinirSenhaRequestDTO dto) {
        String email = passwordResetTokenService.getEmailIfValid(dto.getToken());
        if (email == null) {
            return ResponseEntity.status(400)
                    .body(java.util.Collections.singletonMap("error", "Token inválido ou expirado."));
        }
        usuarioService.atualizarSenhaPorEmail(email, dto.getNovaSenha());
        passwordResetTokenService.removeToken(dto.getToken());
        return ResponseEntity.ok(java.util.Collections.singletonMap("message", "Senha redefinida com sucesso."));
    }
}
