package com.tallyto.gestorfinanceiro.core.application.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tallyto.gestorfinanceiro.api.dto.UsuarioCadastroDTO;
import com.tallyto.gestorfinanceiro.context.TenantContext;
import com.tallyto.gestorfinanceiro.core.domain.entities.Tenant;
import com.tallyto.gestorfinanceiro.core.domain.entities.Usuario;
import com.tallyto.gestorfinanceiro.core.domain.entities.UsuarioGlobal;
import com.tallyto.gestorfinanceiro.core.infra.repositories.TenantRepository;
import com.tallyto.gestorfinanceiro.core.infra.repositories.UsuarioGlobalRepository;
import com.tallyto.gestorfinanceiro.core.infra.repositories.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UsuarioService {
    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TenantRepository tenantRepository;
    
    @Autowired
    private TenantService tenantService;
    
    @Autowired
    private UsuarioGlobalRepository usuarioGlobalRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public void atualizarUltimoAcesso(String email) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            usuario.setUltimoAcesso(java.time.LocalDateTime.now());
            usuarioRepository.save(usuario);
        } catch (Exception e) {
            logger.debug("Não foi possível atualizar último acesso em usuário local: {}", e.getMessage());
        }
    }
    
    /**
     * Cria um novo usuário tanto na tabela local (tenant-specific) quanto na tabela global
     * Sincroniza automaticamente com usuario_global para permitir login centralizado
     */
    @Transactional
    public Usuario cadastrar(UsuarioCadastroDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("E-mail já cadastrado");
        }
        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setCriadoEm(java.time.LocalDateTime.now());
        
        // Obter o tenant do contexto e definir o tenantId
        String tenantDomain = TenantContext.getCurrentTenant();
        Tenant tenant = null;
        if (tenantDomain != null && !"public".equals(tenantDomain)) {
            tenant = tenantRepository.getTenantByDomain(tenantDomain);
            if (tenant != null) {
                usuario.setTenantId(tenant.getId());
                
                // Invalidar token de criação após criar o primeiro usuário
                tenantService.invalidarTokenCriarUsuario(tenant.getId());
            }
        } else {
            // Se não houver tenant no contexto, usar o tenant padrão (public)
            tenant = tenantRepository.findByDomain("public")
                    .orElseThrow(() -> new RuntimeException("Tenant padrão não encontrado"));
            usuario.setTenantId(tenant.getId());
        }
        
        usuario = usuarioRepository.save(usuario);
        
        // Sincronizar com usuario_global
        sincronizarComGlobal(usuario);
        
        return usuario;
    }

    /**
     * Sincroniza um usuário local com a tabela usuario_global
     */
    private void sincronizarComGlobal(Usuario usuario) {
        try {
            if (usuario.getTenantId() == null) {
                logger.warn("Não foi possível sincronizar usuário {} - tenantId não definido", usuario.getEmail());
                return;
            }
            
            // Verificar se já existe na tabela global
            if (!usuarioGlobalRepository.existsByEmail(usuario.getEmail())) {
                UsuarioGlobal usuarioGlobal = new UsuarioGlobal(
                    usuario.getEmail(),
                    usuario.getSenha(),
                    usuario.getTenantId()
                );
                usuarioGlobal.setAtivo(usuario.getAtivo());
                usuarioGlobal.setCriadoEm(usuario.getCriadoEm());
                usuarioGlobalRepository.save(usuarioGlobal);
                logger.debug("Usuário {} sincronizado com usuario_global", usuario.getEmail());
            }
        } catch (Exception e) {
            logger.error("Erro ao sincronizar usuário {} com usuario_global: {}", usuario.getEmail(), e.getMessage());
        }
    }

    @Transactional
    public void atualizarSenhaPorEmail(String email, String novaSenha) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
        
        // Sincronizar a nova senha com usuario_global
        try {
            usuarioGlobalRepository.findByEmail(email).ifPresent(usuarioGlobal -> {
                usuarioGlobal.setSenha(usuario.getSenha());
                usuarioGlobal.setAtualizadoEm(java.time.LocalDateTime.now());
                usuarioGlobalRepository.save(usuarioGlobal);
                logger.debug("Senha de {} sincronizada com usuario_global", email);
            });
        } catch (Exception e) {
            logger.error("Erro ao sincronizar senha de {} com usuario_global: {}", email, e.getMessage());
        }
    }

    @Transactional
    public Usuario atualizarNomePorEmail(String email, String novoNome) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        usuario.setNome(novoNome);
        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    /**
     * Atualiza a senha do usuário, validando a senha atual.
     * Sincroniza automaticamente com usuario_global
     * 
     * @param email      email do usuário
     * @param senhaAtual senha atual informada
     * @param novaSenha  nova senha desejada
     * @throws RuntimeException se usuário não encontrado ou senha atual incorreta
     */
    @Transactional
    public void atualizarSenhaComValidacao(String email, String senhaAtual, String novaSenha) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(senhaAtual, usuario.getSenha())) {
            throw new RuntimeException("Senha atual incorreta");
        }
        usuario.setSenha(encoder.encode(novaSenha));
        usuarioRepository.save(usuario);
        
        // Sincronizar a nova senha com usuario_global
        try {
            usuarioGlobalRepository.findByEmail(email).ifPresent(usuarioGlobal -> {
                usuarioGlobal.setSenha(usuario.getSenha());
                usuarioGlobal.setAtualizadoEm(java.time.LocalDateTime.now());
                usuarioGlobalRepository.save(usuarioGlobal);
            });
        } catch (Exception e) {
            logger.error("Erro ao sincronizar senha de {} com usuario_global: {}", email, e.getMessage());
        }
    }

    // Métodos de Administração
    @Transactional(readOnly = true)
    public java.util.List<Usuario> listarTodosUsuarios() {
        return usuarioRepository.findAll();
    }

    @Transactional
    public void deletarUsuario(Long id, String emailUsuarioLogado) {
        Usuario usuarioParaDeletar = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        // Impedir que o usuário delete a si mesmo
        if (usuarioParaDeletar.getEmail().equals(emailUsuarioLogado)) {
            throw new RuntimeException("Você não pode deletar sua própria conta");
        }
        
        usuarioRepository.deleteById(id);
    }
}
