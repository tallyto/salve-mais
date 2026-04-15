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
import com.tallyto.gestorfinanceiro.context.TenantContext;
import com.tallyto.gestorfinanceiro.core.application.services.EmailService;
import com.tallyto.gestorfinanceiro.core.application.services.JwtService;
import com.tallyto.gestorfinanceiro.core.application.services.PasswordResetTokenService;
import com.tallyto.gestorfinanceiro.core.application.services.UsuarioService;
import com.tallyto.gestorfinanceiro.core.domain.entities.Tenant;
import com.tallyto.gestorfinanceiro.core.domain.entities.UsuarioGlobal;
import com.tallyto.gestorfinanceiro.core.infra.repositories.TenantRepository;
import com.tallyto.gestorfinanceiro.core.infra.repositories.UsuarioGlobalRepository;

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
    @Autowired
    private UsuarioGlobalRepository usuarioGlobalRepository;
    @Autowired
    private TenantRepository tenantRepository;
    
    @Value("${app.password.reset.url}")
    private String passwordResetUrl;

    /**
     * Realiza o login do usuário
     * Nova lógica centralizada: busca usuário na tabela usuario_global (public)
     * e obtém o tenant para gerar o JWT com tenantDomain
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        try {
            // Autenticar usando UsuarioDetailsService que agora busca de usuario_global
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getSenha()));
            
            // Buscar usuário global para obter tenantId
            UsuarioGlobal usuarioGlobal = usuarioGlobalRepository.findByEmail(loginDTO.getEmail())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado na tabela centralizada"));
            
            if (!usuarioGlobal.getAtivo()) {
                logger.warn("Tentativa de login de usuário inativo: {}", loginDTO.getEmail());
                return ResponseEntity.status(401).body("Usuário inativo");
            }
            
            // Buscar tenant para obter o domain
            Tenant tenant = tenantRepository.findById(usuarioGlobal.getTenantId())
                    .orElseThrow(() -> new RuntimeException("Tenant não encontrado"));
            
            // Atualizar último acesso na tabela usuario_global
            usuarioGlobal.setAtualizadoEm(java.time.LocalDateTime.now());
            usuarioGlobalRepository.save(usuarioGlobal);
            
            // Atualizar último acesso também na tabela usuario local (para compatibilidade temporária)
            try {
                usuarioService.atualizarUltimoAcesso(loginDTO.getEmail());
            } catch (Exception e) {
                logger.debug("Não foi possível atualizar último acesso em usuário local: {}", e.getMessage());
            }
            
            // Gerar JWT com email, tenantId e tenantDomain
            String token = jwtService.gerarToken(loginDTO.getEmail(), usuarioGlobal.getTenantId(), tenant.getDomain());
            
            logger.info("Login bem-sucedido para usuário: {} do tenant: {}", loginDTO.getEmail(), tenant.getDomain());
            return ResponseEntity.ok(new TokenDTO(token));
        } catch (AuthenticationException e) {
            logger.warn("Falha na autenticação para email: {}", loginDTO.getEmail());
            return ResponseEntity.status(401).body("Usuário ou senha inválidos");
        } catch (RuntimeException e) {
            logger.error("Erro durante o login para email: {} - {}", loginDTO.getEmail(), e.getMessage());
            return ResponseEntity.status(401).body("Erro ao realizar login");
        }
    }

    @PostMapping("/recuperar-senha")
    public ResponseEntity<?> recuperarSenha(@RequestBody RecuperarSenhaRequestDTO dto) {
        logger.info("Solicitação de recuperação de senha para email: {}", dto.getEmail());
        
        try {
            // Buscar usuário global para obter tenant
            UsuarioGlobal usuarioGlobal = usuarioGlobalRepository.findByEmail(dto.getEmail())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            
            // Buscar tenant para obter o domain
            Tenant tenant = tenantRepository.findById(usuarioGlobal.getTenantId())
                    .orElseThrow(() -> new RuntimeException("Tenant não encontrado"));
            
            // Buscar usuário local para obter o nome (compatibilidade temporária)
            TenantContext.setCurrentTenant(tenant.getDomain());
            var usuario = usuarioService.buscarPorEmail(dto.getEmail());
            TenantContext.clear();
            
            String token = java.util.UUID.randomUUID().toString();
            logger.info("Token gerado para recuperação de senha: {} para email: {}", token, dto.getEmail());
            
            passwordResetTokenService.storeToken(token, dto.getEmail());
            String link = passwordResetUrl + "?token=" + token + "&domain=" + tenant.getDomain();
            logger.info("Link gerado: {}", link);
            
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
        
        try {
            // Atualizar senha na tabela usuario_global
            UsuarioGlobal usuarioGlobal = usuarioGlobalRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            
            // Criptografar nova senha
            org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = 
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
            usuarioGlobal.setSenha(encoder.encode(dto.getNovaSenha()));
            usuarioGlobal.setAtualizadoEm(java.time.LocalDateTime.now());
            usuarioGlobalRepository.save(usuarioGlobal);
            
            // Atualizar também na tabela usuario local (compatibilidade temporária)
            usuarioService.atualizarSenhaPorEmail(email, dto.getNovaSenha());
        } catch (Exception e) {
            logger.error("Erro ao atualizar senha: {}", e.getMessage());
            return ResponseEntity.status(400)
                    .body(java.util.Collections.singletonMap("error", "Erro ao atualizar senha."));
        }
        
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
